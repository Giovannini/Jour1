package models.graph.ontology.property

import models.graph.ontology.ValuedProperty
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(id: Long, label: String, valueType: String, defaultValue: Any) {
  require(label.matches("^[A-Z][A-Za-z0-9]*$"))

  override def toString = label + ": " + valueType + " = " + defaultValue

  def toJson : JsValue = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label),
    "valueType" -> JsString(valueType),
    "defaultValue" -> jsonDefaultValue
  )

  private val jsonDefaultValue: JsValue = valueType match {
    case "Int" => JsNumber(defaultValue.toString.toInt)
    case "Double" => JsNumber(defaultValue.toString.toDouble)
    case "String" => JsString(defaultValue.toString)
    case "Boolean" => JsBoolean(defaultValue.toString.toBoolean)
    case _ => JsString(defaultValue.toString)
  }

  def defaultValuedProperty: ValuedProperty = {
    ValuedProperty(this, defaultValue)
  }
}

object Property {

  val error = Property(-1L, "Error", "error", "error")

  val form = Form(mapping(
      "id" -> longNumber,
      "label" -> nonEmptyText,
      "valueType" -> nonEmptyText,
      "defaultValue" -> text
    )(Property.apply)(Property.unapplyForm))

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param property property to unapply
   * @return tuple of id, label, valueType and defaultValue
   */
  def unapplyForm(property: Property): Option[(Long, String, String, String)] = {
    Some((property.id, property.label, property.valueType, property.jsonDefaultValue.toString()))
  }

  def parse(id: Long, label: String, valueType: String, defaultValueToParse: String): Property = {
    val defaultValue = valueType match {
      case "Int" => defaultValueToParse.toInt
      case "Double" => defaultValueToParse.toDouble
      case "Boolean" => defaultValueToParse.toBoolean
      case _ => defaultValueToParse
    }
    if(id == -1L) Property.error
    else Property(id, label, valueType, defaultValue)
  }

  def parseString(stringProperty: String): Property = {
    val firstSplit = stringProperty.split(": ")
    val label = firstSplit(0)
    PropertyDAO.getByName(label)
  }

}