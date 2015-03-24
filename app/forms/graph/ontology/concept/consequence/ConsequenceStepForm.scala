package forms.graph.ontology.concept.consequence

import models.graph.ontology.concept.consequence.ConsequenceStep
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats


object ConsequenceStepForm {
  val form = Form(mapping(
    "value" -> of(Formats.doubleFormat),
    "consequence" -> ConsequenceForm.form.mapping
  )(ConsequenceStep.apply)(ConsequenceStep.unapply))
}
