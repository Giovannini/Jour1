package models.graph.ontology.property

import models.graph.ontology.ValuedProperty
import play.api.libs.json._

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(id: Long, label: String, valueType: String, defaultValue: Any) {
  require(label.matches("^[A-Z][A-Za-z0-9]*$"))

  override def toString = label + ": " + valueType + " = " + defaultValue

  def toJson : JsValue = Json.obj(
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

  def parse(maybeId: Option[Long], label: String, valueType: String, defaultValueToParse: String): Property = {
    val id = maybeId.getOrElse(-1L)
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
    val secondSplit = firstSplit(1).split(" = ")
    val label = firstSplit(0)
    val valueType = secondSplit(0)
    val defaultValue = valueType match{
      case "Int" => secondSplit(1).toInt
      case "Double" => secondSplit(1).toDouble
      case "Boolean" => secondSplit(1).toBoolean
      case _ => secondSplit(1)
    }
    Property(0L, label, valueType, defaultValue)
  }

}