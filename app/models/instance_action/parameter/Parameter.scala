package models.instance_action.parameter

import play.api.libs.json.{JsValue, Json}

trait Parameter {
  def toJson: JsValue

  def toDBString: String
}

object Parameter {

  def parse(parameterToParse: String): Parameter = {
    val paramTypes = List("Int", "Long", "Property")
    val split = parameterToParse.split(": ")
    val res = if(split.length > 1) {
      val value = split(0)
      val _type = split(1)
      if(!paramTypes.contains(_type)) {
        ParameterError("Incorrect Type Param : "+_type)
      } else {
        // It's a Value parameter
        if(value.startsWith("__val")) {
          ParameterValue(value.substring(5), _type)
        } else {
          ParameterReference(value, _type)
        }
      }
    } else {
      ParameterError("Incorrect Param")
    }
    res match {
      case error if error.isInstanceOf[ParameterError] => throw new Exception(error.toString)
      case param => param
    }
  }

  def parseParameters(parametersToParse: String): List[ParameterReference] = {
    parametersToParse.split(",").map(ParameterReference.parseArgument).toList
  }

  def linkParameterToReference(objectParameters: List[ParameterReference], params: List[Parameter]): Map[ParameterReference, Parameter] = {
    objectParameters.zip(params).toMap
  }

  def toJsonWithIsParam(ref: ParameterReference, parameter: Parameter): JsValue = parameter match {
    case reference if reference.isInstanceOf[ParameterReference] => Json.obj(
      "isParam" -> true,
      "reference" -> ref.reference,
      "value" -> reference.toJson
    )
    case value if value.isInstanceOf[ParameterValue] => Json.obj(
      "isParam" -> false,
      "reference" -> ref.reference,
      "value" -> value.toJson
    )
    case error => Json.obj(
      "isParam" -> false,
      "reference" -> ref.reference,
      "value" -> error.toJson
    )
  }
}

case class ParameterError(error: String) extends Parameter {
  def toJson: JsValue = {
    Json.obj(
      "type" -> "error",
      "value" -> error
    )
  }

  def toDBString = "ERROR"
}