package forms.instance_action.parameter

import models.graph.ontology.property.{PropertyDAO, Property}
import models.instance_action.parameter._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

object ParameterForm {

  def referenceForm = Form(
    mapping(
      "reference" -> nonEmptyText,
      "type" -> nonEmptyText
    )(ParameterReference.apply)(ParameterReference.unapply)
  )

  def valueForm = Form(
    mapping(
      "value" -> of(ValueFormatter),
      "type" -> nonEmptyText
    )(ParameterValue.apply)(ParameterValue.unapply)
  )

  implicit def ValueFormatter: Formatter[Any] = new Formatter[Any] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Any] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, "error.noValue")))
        case Some(v) =>
          Try(v.toLong) match {
            case Success(value) => Right(value)
            case Failure(ex) =>
              val property = PropertyDAO.getByName(v)
              if(property == Property.error) {
                Left(Seq(FormError(key, "error.wrongValueType")))
              } else {
                Right(property.label)
              }
          }
      }
    }

    override def unbind(key: String, value: Any): Map[String, String] = {
      val string = value match {
          case Property(label, _, _) => label
          case any => any.toString
      }

      Map(key -> string)
    }
  }

  /**
   * Formatter used to parse a parameter given in json
   * parsed as ParameterValue if { "isParam": true, "value": {"type": "(Int|Long|Property)", "value":...}}
   * parsed as ParameterReference if { "isParam": true, "value": {"type": "(Int|Long|Property)", "reference":...}}
   * @return Formatter that can be used with of(ParameterFormatter) in a Form
   */
  implicit def ParameterFormatter: Formatter[Parameter] = new Formatter[Parameter] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Parameter] = {
      println(data)
      val isParam = data.get(key+".isParam")

      data.get(key+".isParam") match {
        case None => Left(Seq(FormError(key, "error.isParamUndefined")))
        case Some(v) =>
          if(v.toBoolean) {
            val reference = data.get(key+".value.reference")
            val valueType = data.get(key+".value.type")
            (reference, valueType) match {
              case (None, _) => Left(Seq(FormError(key, "error.noReference")))
              case (_, None) => Left(Seq(FormError(key, "error.noType")))
              case (Some(_reference), Some(_type)) =>
                val result = referenceForm.bind(Json.obj(
                  "reference" -> _reference,
                  "type" -> _type
                ))
                result.fold(
                  hasErrors = form => Left(form.errors),
                  success = reference => Right(reference)
                )
            }
          } else {
            val value = data.get(key+".value.value")
            val valueType = data.get(key+".value.type")
            (value, valueType) match {
              case (None, _) => Left(Seq(FormError(key, "error.noValue")))
              case (_, None) => Left(Seq(FormError(key, "error.noType")))
              case (Some(_value), Some(_type)) =>
                val result = valueForm.bind(Json.obj(
                  "value" -> _value,
                  "type" -> _type
                ))
                result.fold(
                  hasErrors = form => Left(form.errors),
                  success = value => Right(value)
                )
            }
          }
      }
    }

    override def unbind(key: String, parameter: Parameter): Map[String, String] = {
      parameter match {
        case ParameterReference(reference, valueType) =>
          Map(
            key+".isParam" -> true.toString,
            key+".value.reference" -> reference,
            key+".value.type" -> valueType.toString
          )
        case ParameterValue(value, valueType) =>
          Map(
            key+".isParam" -> true.toString,
            key+".value.value" -> value.toString,
            key+".value.type" -> valueType.toString
          )
      }
    }
  }

  val form = Form(
    of(ParameterFormatter)
  )
}