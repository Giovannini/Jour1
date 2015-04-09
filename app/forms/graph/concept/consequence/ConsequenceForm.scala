package forms.graph.concept.consequence

import forms.instance_action.effect.EffectForm
import models.intelligence.consequence.Consequence
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

/**
 * Form object for consequences
 */
object ConsequenceForm {
  val form = Form(mapping(
    "severity" -> of(Formats.doubleFormat),
    "effects" -> EffectForm.form.mapping
  )(Consequence.apply)(Consequence.unapply))
}
