package forms.graph.ontology.concept.consequence

import forms.instance_action.action.InstanceActionForm
import models.graph.ontology.concept.consequence.Consequence
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

/**
 * Created by vlynn on 23/03/15.
 */
object ConsequenceForm {
  val form = Form(mapping(
    "severity" -> of(Formats.doubleFormat),
    "effects" -> list(InstanceActionForm.form.mapping)
  )(Consequence.apply)(Consequence.unapply))
}
