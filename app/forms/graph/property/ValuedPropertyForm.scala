package forms.graph.property

import models.graph.property.{ValuedProperty, Property}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats


object ValuedPropertyForm {

  val conceptForm: Form[ValuedProperty] = Form(mapping(
    ("property", of(PropertyForm.PropertyLabelFormat)),
    ("value", of(Formats.doubleFormat))
  )(applyValueForm)(unapplyValueForm))

  val form: Form[ValuedProperty] = Form(mapping(
    ("property", PropertyForm.form.mapping),
    ("value", of(Formats.doubleFormat))
  )(applyValueForm)(unapplyValueForm))

  def applyValueForm(property: Property, value: Double): ValuedProperty = {
    ValuedProperty(property, value)
  }

  def unapplyValueForm(valuedProperty: ValuedProperty): Option[(Property, Double)] = {
    Some((valuedProperty.property, valuedProperty.value))
  }
}
