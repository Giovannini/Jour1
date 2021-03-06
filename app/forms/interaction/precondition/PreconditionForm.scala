package forms.interaction.precondition

import forms.interaction.parameter.ParameterForm
import models.interaction.parameter.{Parameter, ParameterReference}
import models.interaction.precondition.{Precondition, PreconditionDAO}
import play.api.data.Form
import play.api.data.Forms._

import scala.collection.immutable

object PreconditionForm {

  lazy val subconditionForm: Form[(Precondition, immutable.Map[ParameterReference, Parameter])] = Form(mapping(
    ("id", longNumber),
    ("parameters", list(ParameterForm.form.mapping))
  )(applySubcondition)(unapplySubcondition))

  val form: Form[Precondition] = Form(mapping(
    ("id", longNumber),
    ("label", nonEmptyText),
    ("preconditions", list(subconditionForm.mapping)),
    ("parameters", list(ParameterForm.referenceForm.mapping))
  )(Precondition.apply)(Precondition.unapply))

  def applySubcondition(id: Long, params: List[Parameter]): (Precondition, immutable.Map[ParameterReference, Parameter]) = {
    val precondition = PreconditionDAO.getById(id)
    val parameters = Parameter.linkParameterToReference(precondition.parameters, params)
    (precondition, parameters)
  }

  def unapplySubcondition(form: (Precondition, immutable.Map[ParameterReference, Parameter])): Option[(Long, List[Parameter])] = {
    val preconditionId: Long = form._1.id
    val parameters: List[Parameter] = form._2.values.toList

    Some((
      preconditionId,
      parameters
      ))
  }

}
