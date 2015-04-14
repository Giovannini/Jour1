package forms.interaction.effect

import forms.interaction.parameter.ParameterForm
import models.interaction.effect.{Effect, EffectDAO}
import models.interaction.parameter.{Parameter, ParameterReference}
import play.api.data.Form
import play.api.data.Forms._

object EffectForm {
  lazy val subEffectForm : Form[(Effect, Map[ParameterReference, Parameter])] = Form(
    mapping(
      "id" -> longNumber,
      "parameters" -> list(ParameterForm.form.mapping)
    )(applySubEffect)(unapplySubaction)
  )

  val form: Form[Effect] = Form(
    mapping(
      "label" -> text,
      "subActions" -> list(subEffectForm.mapping),
      "parameters" -> list(ParameterForm.referenceForm.mapping)
    )(applyForm)(unapplyForm)
  )

  val idForm: Form[Effect] = Form(
    mapping(
      "id" -> longNumber
    )(applyIdForm)(unapplyIdForm)
  )

  private def applyForm(label: String,
                        subactions: List[(Effect, Map[ParameterReference, Parameter])],
                        parameters: List[ParameterReference]): Effect = {
    Effect(0, label, subactions, parameters)
  }

  private def unapplyForm(ia: Effect) = {
    Some((ia.label, ia.subInteractions, ia.parameters))
  }

  def applySubEffect(id: Long, params: List[Parameter]): (Effect, Map[ParameterReference, Parameter]) = {
    val effect = EffectDAO.getById(id)
    val parameters = Parameter.linkParameterToReference(effect.parameters, params)
    (effect, parameters)
  }

  def unapplySubaction(form: (Effect, Map[ParameterReference, Parameter])): Option[(Long, List[Parameter])] = {
    val preconditionId = form._1.id
    val parameters = form._2.values.toList

    Some((
      preconditionId,
      parameters
    ))
  }

  def applyIdForm(id: Long): Effect = {
    EffectDAO.getById(id)
  }

  def unapplyIdForm(effect: Effect): Option[(Long)] = {
    if(effect == Effect.error) None
    else Some(effect.id)
  }
}
