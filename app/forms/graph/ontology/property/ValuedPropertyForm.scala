package forms.graph.ontology.property

import models.graph.ontology.ValuedProperty
import models.graph.ontology.property.Property
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

/**
 * Created by vlynn on 24/03/15.
 */
object ValuedPropertyForm {
  val form: Form[ValuedProperty] = Form(mapping(
    "property" -> PropertyForm.form.mapping,
    "value" -> of(Formats.doubleFormat)
  )(applyValueForm)(unapplyValueForm))

  def applyValueForm(property: Property, value: Double): ValuedProperty = {
    ValuedProperty(property, value)
  }

  def unapplyValueForm(valuedProperty: ValuedProperty): Option[(Property, Double)] = {
    Some(valuedProperty.property, valuedProperty.value)
  }
}
