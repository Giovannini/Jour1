package models.graph.property

import models.graph.property.PropertyType.PropertyType
import play.api.libs.json._

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(id: Long, label: String, propertyType: PropertyType, defaultValue: Double) {
  require(label.matches("^[A-Z][A-Za-z0-9]*$"))

  override def toString = label + ":" + propertyType + ":" + defaultValue

  def toJson : JsValue = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label),
    "propertyType" -> JsString(propertyType.toString),
    "defaultValue" -> JsNumber(defaultValue)
  )

  def defaultValuedProperty: ValuedProperty = {
    ValuedProperty(this, defaultValue)
  }

  def save: Property = {
    PropertyDAO.save(this)
    this
  }
}

object Property {
  val error = Property(-1, "Error", PropertyType.Error, 0)

  def apply(label: String, propertyType: PropertyType, defaultValue: Double): Property = {
    Property(0, label, propertyType, defaultValue)
  }

  def parseString(stringProperty: String): Property = {
    val splitted = stringProperty.split(":")
    val label = splitted(0)
    val propertyType = PropertyType.parse(splitted(1))
    val value = splitted(2).toDouble
    Property(label, propertyType, value)
  }

  def parseJson(json: JsValue): Property = {
    val label = (json \ "label").as[String]
    val propertyType = PropertyType.parse((json \ "propertyType").as[String])
    val defaultValue = (json \ "defaultValue").as[Double]
    Property(label, propertyType, defaultValue)
  }

}