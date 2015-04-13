package forms.graph.property

import models.graph.property.{Property, PropertyType}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

object PropertyForm {
  val form: Form[Property] = Form(mapping(
    "id" -> longNumber,
    "label" -> nonEmptyText .verifying("Label has to begin with a capital", label => label.matches("^[A-Z][A-Za-z0-9]*$")),
    "propertyType" -> nonEmptyText,
    "defaultValue" -> of(Formats.doubleFormat)
  )(applyForm)(unapplyForm))

  def applyForm(id: Long, label: String, propertyType: String, defaultValue: Double): Property = {
    if(PropertyType.parse(propertyType) == PropertyType.Error) {
      Property.error
    } else {
      Property(id, label, PropertyType.parse(propertyType), defaultValue)
    }
  }

  def unapplyForm(property: Property): Option[(Long, String, String, Double)] = {
    if(property == Property.error) {
      None
    } else {
      Some((property.id, property.label, property.propertyType.toString, property.defaultValue))
    }
  }
}
