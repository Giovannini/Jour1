package forms.interaction.action

import forms.interaction.parameter.ParameterForm
import forms.interaction.precondition.PreconditionForm
import models.interaction.action.{InstanceAction, InstanceActionDAO}
import models.interaction.parameter.{Parameter, ParameterReference}
import models.interaction.precondition.Precondition
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import play.api.data.format.Formatter

import scala.collection.immutable

object InstanceActionForm {

  lazy val subactionForm: Form[(InstanceAction, immutable.Map[ParameterReference, Parameter])] = Form(
    mapping(
      ("id", longNumber),
      ("parameters", list(ParameterForm.form.mapping))
    )(applySubaction)(unapplySubaction)
  )

  val form: Form[InstanceAction] = Form(
    mapping(
      ("label", text),
      ("preconditions", list(PreconditionForm.subconditionForm.mapping)),
      ("subActions", list(subactionForm.mapping)),
      ("parameters", list(ParameterForm.referenceForm.mapping).verifying("empty parameter list", parameters => parameters.nonEmpty))
    )(applyForm)(unapplyForm)
      .verifying("is Recursive", instanceAction => instanceAction.isValid)
  )

  private def applyForm(
    label: String,
    preconditions: List[(Precondition, immutable.Map[ParameterReference, Parameter])],
    subactions: List[(InstanceAction, immutable.Map[ParameterReference, Parameter])],
    parameters: List[ParameterReference]): InstanceAction = {
    InstanceAction(0, label, preconditions, subactions, parameters)
  }

  private def unapplyForm(ia: InstanceAction) = {
    Some((ia.label, ia.preconditions, ia.subInteractions, ia.parameters))
  }

  def applySubaction(id: Long, params: List[Parameter]): (InstanceAction, immutable.Map[ParameterReference, Parameter]) = {
    val action = InstanceActionDAO.getById(id)
    val parameters = Parameter.linkParameterToReference(action.parameters, params)
    (action, parameters)
  }

  def unapplySubaction(form: (InstanceAction, immutable.Map[ParameterReference, Parameter])): Option[(Long, List[Parameter])] = {
    val precondition = form._1
    val parameters = form._2.values.toList

    Some((
      precondition.id,
      parameters
      ))
  }

  def InstanceActionIdFormat: Formatter[InstanceAction] = new Formatter[InstanceAction] {
    override def bind(key: String, data: immutable.Map[String, String]): Either[Seq[FormError], InstanceAction] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, "error.required")))
        case Some(id) =>
          InstanceActionDAO.getById(id.toLong) match {
            case InstanceAction.error => Left(Seq(FormError(key, "notFound")))
            case action => Right(action)
          }
      }
    }

    override def unbind(key: String, value: InstanceAction): Map[String, String] = {
      Map(key -> value.id.toString)
    }
  }
}
