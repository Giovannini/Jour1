package models.graph.ontology

import org.anormcypher.CypherResultRow
import play.api.libs.json.{Json, JsValue}

/**
 * Model class to represent a valued property
 * @param property the property which is valued
 * @param value the value for the property
 */
case class ValuedProperty(property: Property, value: String){
  /**
   * Parse a ValuedProperty to Json
   * @author Thomas GIOVANNINI
   * @return the parsed valued property to json format
   */
  def toJson : JsValue = Json.obj("property" -> property.label, "value" -> value)

  /**
   * The toString method
   * @author Thomas GIOVANNINI
   * @return the string value for a ValuedProperty
   */
  override def toString = property.label + " -> " + value
}

object ValuedProperty {

  /**
   * Method to parse a json to a ValuedProperty
   * @author Thomas GIOVANNINI
   * @param jsonVP the json value to parse
   * @return a Valued property
   */
  def parseJson(jsonVP: JsValue): ValuedProperty = {
    val property = (jsonVP \ "property").as[String]
    val value = (jsonVP \ "value").as[String]
    ValuedProperty(Property(property), value)
  }

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def rowToPropertiesList(row: CypherResultRow): List[ValuedProperty] = {
    row[Seq[String]]("inst_prop") // get the properties sequence from the row
      .toList
      .map(string => parse(string))
  }

  /**
   * Method to parse a string ("string1 -> string2") to a ValuedProperty
   * @author Thomas GIOVANNINI
   * @param string to parse
   * @return the proper ValuedProperty
   */
  def parse(string: String): ValuedProperty = {
    val vpArray = string.split(" -> ")
    val prop = Property(vpArray(0))
    val value = vpArray(1)
    ValuedProperty(prop, value)
  }
}
