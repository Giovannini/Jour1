package forms.graph.ontology.concept.need

import forms.graph.ontology.concept.consequence.ConsequenceStepForm
import forms.graph.ontology.property.PropertyForm
import models.graph.ontology.concept.consequence.ConsequenceStep
import models.graph.ontology.concept.need.{MeanOfSatisfaction, Need}
import models.graph.ontology.property.Property
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

object NeedForm {
  val form = Form(mapping(
    "label" -> text,
    "affectedProperty" -> PropertyForm.form.mapping,
    "priority" -> of(Formats.doubleFormat),
    "consequencesSteps" -> list(ConsequenceStepForm.form.mapping),
    "meansOfSatisfaction" -> list(MeanOfSatisfactionForm.form.mapping)
  )(applyForm)(unapplyForm))

  def applyForm(label: String, affectedProperty: Property, priority: Double, consequencesSteps: List[ConsequenceStep], meansOfSatisfaction: List[MeanOfSatisfaction]): Need = {
    Need.apply(0, label, affectedProperty, priority, consequencesSteps, meansOfSatisfaction)
  }

  def unapplyForm(need: Need): Option[(String, Property, Double, List[ConsequenceStep], List[MeanOfSatisfaction])] = {
    Some(need.label, need.affectedProperty, need.priority, need.consequencesSteps, need.meansOfSatisfaction)
  }
}
