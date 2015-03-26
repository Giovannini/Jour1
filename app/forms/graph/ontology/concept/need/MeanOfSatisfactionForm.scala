package forms.graph.ontology.concept.need

import forms.graph.ontology.concept.ConceptForm
import forms.instance_action.action.InstanceActionForm
import models.graph.ontology.concept.Concept
import models.graph.ontology.concept.need.MeanOfSatisfaction
import models.instance_action.action.InstanceAction
import play.api.data.Form
import play.api.data.Forms._

object MeanOfSatisfactionForm {
  val form: Form[MeanOfSatisfaction] = Form(mapping(
    "action" -> InstanceActionForm.form.mapping,
    "concepts" -> list(ConceptForm.form.mapping)
  )(applyForm)(unapplyForm))

  def applyForm(action: InstanceAction, concepts: List[Concept]): MeanOfSatisfaction = {
    MeanOfSatisfaction(action, concepts)
  }

  def unapplyForm(mean: MeanOfSatisfaction): Option[(InstanceAction, List[Concept])] = {
    MeanOfSatisfaction.unapply(mean)
  }
}
