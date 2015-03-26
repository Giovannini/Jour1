package forms.instance_action.action

import forms.instance_action.parameter.ParameterForm
import forms.instance_action.precondition.PreconditionForm
import models.instance_action.action.InstanceAction
import models.instance_action.parameter.{Parameter, ParameterReference}
import models.instance_action.precondition.Precondition
import play.api.data.Form
import play.api.data.Forms._

object InstanceActionForm {
  lazy val subactionForm : Form[(InstanceAction, Map[ParameterReference, Parameter])] = Form(
    mapping(
      "id" -> longNumber,
      "parameters" -> list(ParameterForm.form.mapping)
    )(applySubaction)(unapplySubaction)
  )

  val form: Form[InstanceAction] = Form(
    mapping(
      "label" -> text,
      "preconditions" -> list(PreconditionForm.subconditionForm.mapping),
      "subActions" -> list(subactionForm.mapping),
      "parameters" -> list(ParameterForm.referenceForm.mapping)
    )(applyForm)(unapplyForm)
  )

  private def applyForm(
                        label: String,
                        preconditions: List[(Precondition, Map[ParameterReference, Parameter])],
                        subactions: List[(InstanceAction, Map[ParameterReference, Parameter])],
                        parameters: List[ParameterReference]): InstanceAction = {
    InstanceAction(0, label, preconditions, subactions, parameters)
  }

  private def unapplyForm(ia: InstanceAction) = {
    Some((ia.label, ia.preconditions, ia.subActions, ia.parameters))
  }

  def applySubaction(id: Long, params: List[Parameter]): (InstanceAction, Map[ParameterReference, Parameter]) = {
    val action = InstanceAction.getById(id)
    val parameters = Parameter.linkParameterToReference(action.parameters, params)
    (action, parameters)
  }

  def unapplySubaction(form: (InstanceAction, Map[ParameterReference, Parameter])): Option[(Long, List[Parameter])] = {
    val precondition = form._1
    val parameters = form._2

    Some(
      precondition.id,
      parameters.unzip._2.toList
    )
  }
}
