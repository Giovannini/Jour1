package forms

import models.instance_action.parameter.{ParameterError, ParameterReference, Parameter}
import models.instance_action.precondition.{PreconditionDAO, Precondition}
import play.api.data.Form
import play.api.data.Forms._

import scala.collection.mutable

object PreconditionForm {
  lazy val subconditionForm: Form[(Precondition, Map[ParameterReference, Parameter])] = Form(mapping(
    "id" -> longNumber,
    "parameters" -> list(ParameterForm.form.mapping)
  )(applySubcondition)(unapplySubcondition))

  val form: Form[Precondition] = Form(mapping(
    "id" -> longNumber,
    "label" -> text,
    "preconditions" -> list(subconditionForm.mapping),
    "parameters" -> list(ParameterForm.referenceForm.mapping)
  )(Precondition.apply)(Precondition.unapply))

  def applySubcondition(id: Long, params: List[Parameter]): (Precondition, Map[ParameterReference, Parameter]) = {
    val precondition = PreconditionDAO.getById(id)
    val parameters = Parameter.linkParameterToReference(precondition.parameters, params)
    (precondition, parameters)
  }

  def unapplySubcondition(form: (Precondition, Map[ParameterReference, Parameter])): Option[(Long, List[Parameter])] = {
    val precondition = form._1
    val parameters = form._2

    Some(
      precondition.id,
      parameters.unzip._2.toList
    )
  }

}
