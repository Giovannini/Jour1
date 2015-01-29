package models.graph.ontology

import models.graph.custom_types.{Coordinates, Label}
import org.anormcypher.CypherResultRow
import play.api.libs.json._


/**
 * Model for an instance of the ontology
 * @author Thomas GIOVANNINI
 * @param label of the instance
 */
case class Instance(label:          Label,
                    coordinates:    Coordinates,
                    properties:     List[ValuedProperty],
                    concept:        Concept) {

  def toJson: JsValue = Json.obj(
    "label" -> label.content,
    "coordinates" -> Json.obj("x" -> coordinates.x, "y" -> coordinates.y),
    "properties" -> Json.arr(properties.map(vp => vp.toJson)),
    "concept" -> concept.toJson
  )

  override def hashCode = label.hashCode() + coordinates.hashCode() + properties.hashCode() + concept.hashCode()

  def toNodeString = "(" + label.content.toLowerCase +
    " { label: \"" + label.content + "\","+
    " properties: [" + properties.map(vp => "\"" + vp.toString + "\"").mkString(", ") + "],"+
    " coordinate_x: " + coordinates.x + ","+
    " coordinate_y: " + coordinates.y + ","+
    " concept: " + concept.hashCode() + ","+
    " type: \"INSTANCE\","+
    " id: " + hashCode() + "})"

  def isValid = {
    concept.properties.toSeq == properties.map(vp => vp.property).toSeq
  }
}

object Instance {
  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRowGivenConcept(row: CypherResultRow, concept: Concept): Instance = {
    val label = Label(row[String]("inst_label"))
    val coordinates = Coordinates(row[Int]("inst_coordx"),row[Int]("inst_coordy"))
    val properties = ValuedProperty.rowToPropertiesList(row)
    Instance(label, coordinates, properties, concept)
  }

}

