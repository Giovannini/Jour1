package models.graph.ontology

import models.graph.custom_types.Coordinates
import org.anormcypher.CypherResultRow
import play.api.libs.json._


/**
 * Model for an instance of the ontology
 * @author Thomas GIOVANNINI
 * @param label of the instance
 */
case class Instance(label:          String,
                    coordinates:    Coordinates,
                    properties:     List[ValuedProperty],
                    concept:        Concept) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$") && isValid)

  /**
   * Parse an Instance to Json
   * @author Thomas GIOVANNINI
   * @return the Json form of the instance
   */
  def toJson: JsValue = Json.obj(
    "label" -> label,
    "coordinates" -> Json.obj("x" -> JsNumber(coordinates.x), "y" -> JsNumber(coordinates.y)),
    "properties" -> properties.map(vp => vp.toJson),
    "concept" -> JsNumber(concept.hashCode())
  )

  override def hashCode = label.hashCode() + coordinates.hashCode() + properties.hashCode() + concept.hashCode()

  /**
   * Parse an Instance for it to be used in a Cypher statement
   * @author Thomas GIOVANNINI
   * @return a cypher statement compatible string representing the instance
   */
  def toNodeString = "(" + label.toLowerCase +
    " { label: \"" + label + "\","+
    " properties: [" + properties.map(vp => "\"" + vp.toString + "\"").mkString(", ") + "],"+
    " coordinate_x: " + coordinates.x + ","+
    " coordinate_y: " + coordinates.y + ","+
    " concept: " + concept.hashCode() + ","+
    " type: \"INSTANCE\","+
    " id: " + hashCode() + "})"

  /**
   * Method to see if the instance matches its concept properties
   * @author Thomas GIOVANNINI
   * @return true if it does
   *         false else
   */
  def isValid = {
    concept.properties.toSeq == properties.map(vp => vp.property).toSeq
  }
}

object Instance {

  /**
   * Transform a json representing an instance into the Instance it represents
   * @author Thomas GIOVANNINI
   * @param jsonInstance the instance to parse
   * @return the represented instance
   */
  def parseJson(jsonInstance: JsValue): Instance = {
    val label = (jsonInstance \ "label").as[String]
    val x_coordinate = (jsonInstance \ "coordinates" \ "x").as[Int]
    val y_coordinate = (jsonInstance \ "coordinates" \ "y").as[Int]
    val coordinates = Coordinates(x_coordinate, y_coordinate)
    val properties = (jsonInstance \ "properties").as[List[JsValue]].map(ValuedProperty.parseJson)
    val conceptId = (jsonInstance \ "concept").as[Int]
    Instance(label, coordinates, properties, Concept.getById(conceptId))
  }

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRowGivenConcept(row: CypherResultRow, conceptId: Int): Instance = {
    val label = row[String]("inst_label")
    val coordinates = Coordinates(row[Int]("inst_coordx"),row[Int]("inst_coordy"))
    val properties = ValuedProperty.rowToPropertiesList(row)
    Instance(label, coordinates, properties, Concept.getById(conceptId))
  }

}

