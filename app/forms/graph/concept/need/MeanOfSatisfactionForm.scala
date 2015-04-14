package forms.graph.concept.need

import forms.graph.concept.ConceptForm
import forms.interaction.action.InstanceActionForm
import models.intelligence.MeanOfSatisfaction
import play.api.data.Form
import play.api.data.Forms._

object MeanOfSatisfactionForm {

  val form: Form[MeanOfSatisfaction] = Form(mapping(
    "action" -> of(InstanceActionForm.InstanceActionIdFormat),
    "concept" -> of(ConceptForm.ConceptIdFormat)
  )(MeanOfSatisfaction.apply)(MeanOfSatisfaction.unapply))

}
