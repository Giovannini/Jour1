package models.interaction.parameter

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsString, Json}


case class ParameterReference(reference: String, _type: String) extends Parameter{
  require(reference.matches("[A-Za-z0-9_]*"))

  def toJson = Json.obj(
    "reference" -> JsString(reference),
    "type" -> JsString(_type)
  )

  override def toString = reference + ": " + _type

  def toDBString = toString
}

object ParameterReference {
  val error = ParameterReference("error", "error")

  def parseArgument(argumentToString: String): ParameterReference = {
    val splittedString = argumentToString.split(": ")
    ParameterReference(splittedString(0), splittedString(1))
  }

  def parseArgumentList(argumentListToString: String): List[ParameterReference] = {
    if (argumentListToString != ""){
      argumentListToString.split(", ")
        .map(parseArgument)
        .toList
    }else List()
  }
}
