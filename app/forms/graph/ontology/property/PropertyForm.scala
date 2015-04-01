package forms.graph.ontology.property

import models.graph.ontology.property.{Property, PropertyType}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

/**
 * Created by vlynn on 23/03/15.
 */
object PropertyForm {
  val form: Form[Property] = Form(mapping(
    "label" -> nonEmptyText,
    "propertyType" -> nonEmptyText,
    "defaultValue" -> of(Formats.doubleFormat)
  )(applyForm)(unapplyForm))

  def applyForm(label: String, propertyType: String, defaultValue: Double): Property = {
    Property(label, PropertyType.parse(propertyType), defaultValue)
  }

  def unapplyForm(property: Property): Option[(String, String, Double)] = {
    Some((property.label, property.propertyType.toString, property.defaultValue))
  }
}
