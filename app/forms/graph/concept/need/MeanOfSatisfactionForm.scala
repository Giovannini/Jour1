package forms.graph.concept.need

import forms.graph.concept.ConceptForm
import forms.interaction.action.InstanceActionForm
import models.graph.concept.Concept
import models.intelligence.MeanOfSatisfaction
import models.interaction.action.InstanceAction
import play.api.data.Form
import play.api.data.Forms._

object MeanOfSatisfactionForm {

  val form: Form[MeanOfSatisfaction] = Form(mapping(
    "action" -> InstanceActionForm.idForm.mapping.verifying("unfound action", action => action != InstanceAction.error),
    "concept" -> ConceptForm.idForm.mapping.verifying("unfound concept", concept => concept != Concept.error)
  )(MeanOfSatisfaction.apply)(MeanOfSatisfaction.unapply))

}
