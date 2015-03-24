package forms.graph.ontology.concept.need

import forms.graph.ontology.concept.consequence.ConsequenceStepForm
import forms.graph.ontology.property.PropertyForm
import forms.instance_action.action.InstanceActionForm
import models.graph.ontology.concept.consequence.ConsequenceStep
import models.graph.ontology.concept.need.Need
import models.graph.ontology.property.Property
import models.instance_action.action.InstanceAction
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats

object NeedForm {
  val form = Form(mapping(
    "id" -> longNumber,
    "label" -> text,
    "affectedProperty" -> PropertyForm.form.mapping,
    "priority" -> of(Formats.doubleFormat),
    "consequencesSteps" -> list(ConsequenceStepForm.form.mapping),
    "meansOfSatisfaction" -> list(InstanceActionForm.form.mapping)
  )(applyForm)(unapplyForm))

  def applyForm(id: Long, label: String, affectedProperty: Property, priority: Double, consequencesSteps: List[ConsequenceStep], meansOfSatisfaction: List[InstanceAction]) = {
    Need.apply(id, label, affectedProperty, priority, consequencesSteps, meansOfSatisfaction)
  }

  def unapplyForm(need: Need): Option[(Long, String, Property, Double, List[ConsequenceStep], List[InstanceAction])] = {
    Need.unapply(need)
  }
}
