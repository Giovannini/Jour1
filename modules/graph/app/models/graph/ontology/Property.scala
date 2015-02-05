package models.graph.ontology

import org.anormcypher.CypherResultRow
import play.api.libs.json.{JsString, JsValue}

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(label: String) {
  require(label.matches("^[A-Z][A-Za-z]*$"))
  override def toString = "\"" + label + "\""

  def toJson : JsValue = JsString(label)
}

object Property {

  def parseJson(jsonProperty: JsValue): Property = {
    val label = jsonProperty.as[String]
    Property(label)
  }

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def rowToPropertiesList(row: CypherResultRow): List[Property] =
    row[Seq[String]]("concept_prop") // get the properties sequence from the row
    .toList
    .map(p => Property(p))

}