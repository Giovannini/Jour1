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

  override def hashCode = label.hashCode + concept.hashCode()

  /**
   * Parse an Instance for it to be used in a Cypher statement
   * @author Thomas GIOVANNINI
   * @return a cypher statement compatible string representing the instance
   */
  def toNodeString = "(" + label.toLowerCase +
    " { label: \"" + label + "\","+
    " coordinate_x: " + coordinates.x + ", coordinate_y: " + coordinates.y + ","+
    " concept: " + concept.hashCode() + ","+
    " type: \"INSTANCE\","+
    " id: " + hashCode() + "," +
    " properties: ["+properties.mkString(", ") + "]})"

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

  val error = Instance("XXX", Coordinates(0,0), List(), Concept("XXX", List()))

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
    Concept.getById(conceptId) match {
      case Some(concept) => Instance(label, coordinates, properties, concept)
      case _ => Instance("XXX", coordinates, List(), Concept("XXX", List()))
    }
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
    println(row)
    val label = row[String]("inst_label")
    val coordinates = Coordinates(row[Int]("inst_coordx"),row[Int]("inst_coordy"))
    val properties = ValuedProperty.rowToPropertiesList(row)
    println(Concept.getById(conceptId))
    Concept.getById(conceptId) match {
      case Some(concept) => Instance(label, coordinates, properties, concept)
      case _ => Instance("XXX", coordinates, List(), Concept("XXX", List()))
    }
  }

  /**
   * Create an instance of a certain concept with random attributes
   * @author Thomas GIOVANNINI
   * @param concept the concept of which the instance is desired
   * @return a new instance
   */
  def createRandomInstanceOf(concept: Concept): Instance = {
    Instance(concept.label + (math.random * 1000000).toInt,
              Coordinates(0,0),
              concept.properties.map(prop => ValuedProperty(prop, (math.random * 1000000).toInt.toString)),
              concept)
  }

  /**
   * Update the property of an instance
   * @author Thomas GIOVANNINI
   * @param instance to update
   * @param newValuedProperty the property to update
   * @return an instance similar to the input one with updated value
   */
  def update(instance: Instance, newValuedProperty: ValuedProperty): Instance = {
    Instance(instance.label,
              instance.coordinates,
              ValuedProperty.updateList(instance.properties, newValuedProperty),
              instance.concept)
  }
}

