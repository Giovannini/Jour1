package models.instance_action

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsString, Json}


case class Parameter(reference: String, _type: String){
  require(reference.matches("[A-Za-z0-9_]*"))

  def toJson = Json.obj(
    "reference" -> JsString(reference),
    "type" -> JsString(_type)
  )

  override def toString = reference + ": " + _type
}

object Parameter {

  val form: Form[Parameter] = Form(mapping(
     "reference" -> text,
     "_type" -> text
   )(Parameter.apply)(Parameter.unapply))

  val error = Parameter("error", "error")

  def parseArgument(argumentToString: String): Parameter = {
    val splittedString = argumentToString.split(": ")
    Parameter(splittedString(0), splittedString(1))
  }

  def parseArgumentList(argumentListToString: String): List[Parameter] = {
    if (argumentListToString != ""){
      argumentListToString.split(", ")
        .map(parseArgument)
        .toList
    }else List()
  }
}
