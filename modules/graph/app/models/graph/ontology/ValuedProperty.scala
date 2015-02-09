package models.graph.ontology

import org.anormcypher.CypherResultRow
import play.api.libs.json._

/**
 * Model class to represent a valued property
 * @param property the property which is valued
 * @param value the value for the property
 */
case class ValuedProperty(property: Property, value: Any){
  /**
   * Parse a ValuedProperty to Json
   * @author Thomas GIOVANNINI
   * @return the parsed valued property to json format
   */
  def toJson : JsValue = Json.obj("property" -> property.label, "value" -> toJson(value))

  /**
   * Convert a value with an unknown type to the correct JsValue
   * @author Thomas GIOVANNINI
   * @param value the value to convert
   * @return a Json value representing the input one
   */
  private def toJson(value: Any): JsValue = {
    value match {
      case number: Int => JsNumber(number)
      case number: Double => JsNumber(number)
      case string: String => JsString(string)
      case boolean: Boolean => JsBoolean(boolean)
      case list: List[Any] => JsArray(list.map(toJson(_)))
      case _ => JsString(value.toString)
    }
  }

  /**
   * The toString method
   * @author Thomas GIOVANNINI
   * @return the string value for a ValuedProperty
   */
  override def toString = "\"" + property.label + ": " + value + "\""
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
    val value = parseValue(jsonVP \ "value")
    ValuedProperty(Property(property), value)
  }

  /**
   * Method to convert a json value of an unknown type to its real type
   * @param jsonValue to convert
   * @return the value in its correct type
   */
  private def parseValue(jsonValue: JsValue): Any = {
    jsonValue match {
      case jsonNumber: JsNumber => jsonValue.as[Double]
      case jsonString: JsString => jsonValue.as[String]
      case jsonBoolean: JsBoolean => jsonValue.as[Boolean]
      case jsonArray: JsArray => jsonValue.as[List[JsValue]].map(parseValue)
      case _ => jsonValue.as[String]
    }
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
    val vpArray = string.split(": ")
    val prop = Property(vpArray(0))
    val value = vpArray(1)
    ValuedProperty(prop, value)
  }

  def updateList(vpList: List[ValuedProperty], vp: ValuedProperty): List[ValuedProperty] = {
    vpList match{
      case h::t if h.property == vp.property => vp :: t
      case h::t => h :: updateList(t, vp)
      case _ => List()
    }
  }
}
