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
    "id" -> optional[Long](longNumber),
    "label" -> text,
    "affectedProperty" -> PropertyForm.labelForm.mapping.verifying("unfound property", property => property != Property.error),
    "priority" -> of[Double](Formats.doubleFormat),
    "consequenceSteps" -> list(ConsequenceStepForm.form.mapping),
    "meansOfSatisfaction" -> list(MeanOfSatisfactionForm.form.mapping)
  )(applyForm)(unapplyForm))

  def applyForm(id: Option[Long], label: String, affectedProperty: Property, priority: Double, consequencesSteps: List[ConsequenceStep], meansOfSatisfaction: List[MeanOfSatisfaction]): Need = {
    id match {
      case None =>
        Need.apply(0, label, affectedProperty, priority, consequencesSteps, meansOfSatisfaction)
      case Some(formerId) =>
        Need.apply(formerId, label, affectedProperty, priority, consequencesSteps, meansOfSatisfaction)
    }
  }

  def unapplyForm(need: Need): Option[(Option[Long], String, Property, Double, List[ConsequenceStep], List[MeanOfSatisfaction])] = {
    need.id match {
      case 0 =>
        Some((None, need.label, need.affectedProperty, need.priority, need.consequencesSteps, need.meansOfSatisfaction))
      case _ =>
        Some((Some(need.id), need.label, need.affectedProperty, need.priority, need.consequencesSteps, need.meansOfSatisfaction))
    }
  }
}
