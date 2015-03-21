package forms

import models.instance_action.parameter._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

object ParameterForm {

  def referenceForm = Form(
    mapping(
      "type" -> nonEmptyText,
      "reference" -> nonEmptyText
    )(ParameterReference.apply)(ParameterReference.unapply)
  )

  def valueForm = Form(
    mapping(
      "type" -> nonEmptyText,
      "value" -> nonEmptyText
    )(ParameterReference.apply)(ParameterReference.unapply)
  )

  /**
   * Formatter used to parse a parameter given in json
   * parsed as ParameterValue if { "isParam": true, "value": {"type": "(Int|Long|Property)", "value":...}}
   * parsed as ParameterReference if { "isParam": true, "value": {"type": "(Int|Long|Property)", "reference":...}}
   * @return Formatter that can be used with of(ParameterFormatter) in a Form
   */
  implicit def ParameterFormatter: Formatter[Parameter] = new Formatter[Parameter] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Parameter] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, "error.required")))
        case Some(v) =>
          val json = Json.parse(v)
          Try((json \ "isParam").as[Boolean]) match {
            case Failure(ex) => Left(Seq(FormError(key, "error.isParamUndefined")))
            case Success(isParam) =>
              if (isParam) {
                Try(json \ "value") match {
                  case Failure(ex) => Left(Seq(FormError(key, "error.noValue")))
                  case Success(value) =>
                    val result = referenceForm.bind(value)
                    result.fold(
                      hasErrors = form => Left(form.errors),
                      success = reference => Right(reference)
                    )
                }
              } else {
                Try(json \ "value") match {
                  case Failure(ex) => Left(Seq(FormError(key, "error.noValue")))
                  case Success(value) =>
                    val result = valueForm.bind(value)
                    result.fold(
                      hasErrors = form => Left(form.errors),
                      success = value => Right(value)
                    )
                }
              }
          }
      }
    }

    override def unbind(key: String, parameter: Parameter): Map[String, String] = {
      parameter match {
        case ParameterReference(reference, valueType) =>
          Map(
            key -> Json.obj(
              "isParam" -> true,
              "value" -> parameter.toJson
            ).toString()
          )
        case ParameterValue(value, valueType) =>
          Map(
            key -> Json.obj(
              "isParam" -> true,
              "value" -> parameter.toJson
            ).toString()
          )
      }
    }
  }

  val form = Form(
    of(ParameterFormatter)
  )
}