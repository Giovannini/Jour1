package forms.graph.concept.need

import forms.graph.concept.consequence.ConsequenceStepForm
import forms.graph.property.PropertyForm
import models.intelligence.MeanOfSatisfaction
import models.intelligence.consequence.ConsequenceStep
import models.intelligence.need.Need
import models.graph.property.Property
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
    Some((need.label, need.affectedProperty, need.priority, need.consequencesSteps, need.meansOfSatisfaction))
  }
}
