package forms.graph.concept.need

import forms.graph.concept.ConceptForm
import forms.interaction.action.InstanceActionForm
import models.intelligence.MeanOfSatisfaction
import play.api.data.Form
import play.api.data.Forms._

object MeanOfSatisfactionForm {

  val form: Form[MeanOfSatisfaction] = Form(mapping(
    "action" -> InstanceActionForm.form.mapping,
    "concepts" -> ConceptForm.idForm.mapping
  )(MeanOfSatisfaction.apply)(MeanOfSatisfaction.unapply))

}
