package models.graph.ontology.property

import models.graph.ontology.ValuedProperty
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(label: String, defaultValue: Double) {
  require(label.matches("^[A-Z][A-Za-z0-9]*$"))

  override def toString = label + " = " + defaultValue

  def toJson : JsValue = Json.obj(
    "label" -> JsString(label),
    "defaultValue" -> JsNumber(defaultValue)
  )

  def defaultValuedProperty: ValuedProperty = {
    ValuedProperty(this, defaultValue)
  }

  def save: Property = {
    PropertyDAO.save(this)
  }
}

object Property {
  val error = Property("Error", 0)

  val form = Form(mapping(
    "property" -> nonEmptyText
  )(Property.parseString)(Property.unapplyForm))

  private def unapplyForm(property: Property) = {
    Some(property.toString)
  }

  def parseString(stringProperty: String): Property = {
    val splitted = stringProperty.split(" = ")
    val label = splitted(0)
    val value = splitted(1).toDouble
    Property(label, value)
  }

  def parseJson(json: JsValue): Property = {
    val label = (json \ "label").as[String]
    val defaultValue = (json \ "defaultValue").as[Double]
    Property(label, defaultValue)
  }

}