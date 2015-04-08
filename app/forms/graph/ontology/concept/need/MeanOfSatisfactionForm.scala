package forms.graph.ontology.concept.need

import forms.graph.ontology.concept.ConceptForm
import forms.instance_action.action.InstanceActionForm
import models.graph.ontology.concept.need.MeanOfSatisfaction
import play.api.data.Form
import play.api.data.Forms._

object MeanOfSatisfactionForm {

  val form: Form[MeanOfSatisfaction] = Form(mapping(
    "action" -> InstanceActionForm.form.mapping,
    "concepts" -> ConceptForm.idForm.mapping
  )(MeanOfSatisfaction.apply)(MeanOfSatisfaction.unapply))

}
