package models.graph.ontology

import models.graph.custom_types.Label
import org.anormcypher.CypherResultRow
import play.api.libs.json.{JsValue, Json}

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(label: String) {
  require(label.matches("^[A-Z][A-Za-z]*$"))
  override def toString = "\"" + label + "\""

  def toJson : JsValue = Json.parse(label)
}

object Property {

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def rowToPropertiesList(row: CypherResultRow): List[Property] =
    row[Seq[String]]("prop") // get the properties sequence from the row
    .toList
    .map(p => new Property(p))

}

/*/**
 * Model for properties which values are String
 * @author Thomas GIOVANNINI
 * @param value of the property
 */
case class StringProperty(label: Label, value: String) extends Property(label) {
  override def toJson: JsValue = Json.obj("label" -> label.content, "value" -> Json.toJson(value))

  def toNodeString = "{label:\"" + label.content + "\", value:\"" + value + "\"}"
}

/**
 * Model for properties which values are Int
 * @author Thomas GIOVANNINI
 * @param label of the property
 * @param value of the property
 */
case class IntProperty(label: Label, value: Int) extends Property(label) {
  override def toJson: JsValue = Json.obj("label" -> label.content, "value" -> Json.toJson(value))

  def toNodeString = "{label:\"" + label.content + "\", value:" + value + "}"
}*/