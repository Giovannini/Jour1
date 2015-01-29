package models.graph.ontology

import org.anormcypher.CypherResultRow
import play.api.libs.json.{Json, JsValue}

/**
 * Model class to represent a valued property
 * @param property the property which is valued
 * @param value the value for the property
 */
case class ValuedProperty(property: Property, value: String){
  def toJson : JsValue = Json.obj(property.label -> value)

  override def toString = property.label + "%" + value
}

object ValuedProperty {
  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def rowToPropertiesList(row: CypherResultRow): List[ValuedProperty] =
    row[Seq[String]]("prop") // get the properties sequence from the row
      .toList
      .map(string =>extractValuedPropertyFromString(string))

  def extractValuedPropertyFromString(vp: String): ValuedProperty = {
    val vpArray = vp.split("%")
    val prop = Property(vpArray(0))
    val value = vpArray(1)
    ValuedProperty(prop, value)
  }
}
