package models.graph.ontology

import models.graph.ontology.property.{PropertyDAO, Property}
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
  def toJson : JsValue = Json.obj(
    "property" -> JsNumber(property.id),
    "value" -> jsonValue
  )

  /**
   * Convert a value with an unknown type to the correct JsValue
   * @author Thomas GIOVANNINI
   * @return a Json value representing the input one
   */
  private def jsonValue: JsValue = {
    property.valueType match {
      case "Int" => JsNumber(value.toString.toInt)
      case "Double" => JsNumber(value.toString.toDouble)
      case "String" => JsString(value.toString)
      case "Boolean" => JsBoolean(value.toString.toBoolean)
      //case list: List[Any] => JsArray(list.map(toJson(_)))
      case _ => JsString(value.toString)
    }
  }

  /**
   * The toString method
   * @author Thomas GIOVANNINI
   * @return the string value for a ValuedProperty
   */
  override def toString = property.id + " -> " + value
}

object ValuedProperty {

  def apply(id: Long, value: Any): ValuedProperty = {
    ValuedProperty(PropertyDAO.getById(id), value)
  }

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param property of the valued property
   * @param value of the valued property
   * @return valued property object
   */
  def applyForm(property: Property, value: Any): ValuedProperty = {
    ValuedProperty(property, value)
  }

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param valuedProperty valued property object
   * @return tuple of property and value
   */
  def unapplyForm(valuedProperty: ValuedProperty): Option[(Property, String)] = {
    Some((
      valuedProperty.property, valuedProperty.jsonValue.toString()
      ))
  }

  /**
   * Method to parse a json to a ValuedProperty
   * @author Thomas GIOVANNINI
   * @param jsonVP the json value to parse
   * @return a Valued property
   */
  def parseJson(jsonVP: JsValue): ValuedProperty = {
    val property = PropertyDAO.getById((jsonVP \ "property").as[Long])
    val value = parseValue(jsonVP \ "value", property.valueType)
    ValuedProperty(property, value)
  }

  /**
   * Method to convert a json value of an unknown type to its real type
   * @param jsonValue to convert
   * @return the value in its correct type
   */
  private def parseValue(jsonValue: JsValue, valueType: String): Any = {
    valueType match {
      case "Int" => jsonValue.as[Int]
      case "Double" => jsonValue.as[Double]
      case "String" => jsonValue.as[String]
      case "Boolean" => jsonValue.as[Boolean]
      //case "Array" => jsonValue.as[List[JsValue]].map(parseValue(_))
      case _ => jsonValue.as[String]
    }
  }

  /**
   * Method to convert a string value of an unknown type to its real type
   * @param stringValue to convert
   * @return the value in its correct type
   */
  def parseValue(property: Property, stringValue: String): ValuedProperty = {
    val value = property.valueType match {
      case "Int" => stringValue.toInt
      case "Double" => stringValue.toDouble
      case "String" => stringValue
      case "Boolean" => stringValue.toBoolean
      case _ => stringValue
    }
    ValuedProperty(property, value)
  }


  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def rowToPropertiesList(row: CypherResultRow, string: String): List[ValuedProperty] = {
    row[Seq[String]](string) // get the properties sequence from the row
      .toList
      .map(string => parse(string))
  }

  /**
   * Method to parse a string ("property%value") to a ValuedProperty
   * @author Thomas GIOVANNINI
   * @param string to parse
   * @return the proper ValuedProperty
   */
  def parse(string: String): ValuedProperty = {
    val vpArray = string.split(" -> ")
    val prop = PropertyDAO.getById(vpArray(0).toLong)
    val value = prop.valueType match{
      case "Int" => vpArray(1).toInt
      case "Double" => vpArray(1).toDouble
      case "String" => vpArray(1)
      case "Boolean" => vpArray(1).toBoolean
      case _ => vpArray(1)
    }
    ValuedProperty(prop, value)
  }

  //TODO: cheum
  def updateList(vpList: List[ValuedProperty], vp: ValuedProperty): List[ValuedProperty] = {
    vpList match{
      case h::t if h.property == vp.property => vp :: t
      case h::t => h :: updateList(t, vp)
      case _ => List()
    }
  }

  /**
   * Remove from a long list of rules the recurrent ones, keeping the first coming.
   * @author Thomas GIOVANNINI
   * @param rulesList the list to reduce
   * @param matchedProperties the already matched properties that shouldn't be added
   * @return a shorter rules list
   */
  def keepHighestLevelRules(rulesList: List[ValuedProperty], matchedProperties: List[Property]): List[ValuedProperty] = {
    rulesList match {
      case head :: tail if matchedProperties.contains(head.property) => keepHighestLevelRules(tail, matchedProperties)
      case head :: tail => head :: keepHighestLevelRules(tail, head.property :: matchedProperties)
      case _ => List()
    }
  }
}
