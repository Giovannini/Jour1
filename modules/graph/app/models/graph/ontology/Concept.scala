package models.graph.ontology

import models.graph.custom_types.Label
import org.anormcypher.CypherResultRow
import play.api.libs.json.{JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author
 * @param label for the concept
 * @param properties of this concept
 */
case class Concept(label: Label,
                   properties: List[Property]) {

  override def hashCode() = label.content.hashCode + properties.hashCode()

  def toJson: JsValue = Json.obj( "label" -> label.content, "properties" -> properties.map(_.toString),
    "type" -> "CONCEPT", "id" -> hashCode())

  def toNodeString = "(" + label.content.toLowerCase +
    " { label: \"" + label.content + "\","+
    " properties: [" + properties.mkString(",") + "],"+
    " type: \"CONCEPT\","+
    " id:" + hashCode() + "})"

}

object Concept {

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRow(row: CypherResultRow): Concept = {
    val label = Label(row[String]("label"))
    val properties = Property.rowToPropertiesList(row)
    Concept(label, properties)
  }

  /**
   * Method to add a property to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param property to be added
   * @return the concept with the given property added
   */
  def addPropertyToConcept(concept: Concept, property: Property): Concept =
    Concept(concept.label, property :: concept.properties)

}