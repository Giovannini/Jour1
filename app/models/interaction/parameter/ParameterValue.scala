package models.interaction.parameter

import models.graph.ontology.Instance
import models.graph.ontology.property.PropertyDAO
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}


case class ParameterValue(value: Any, _type: String) extends Parameter {

  override def toString: String = "ParameterValue(value= " + value.toString + ", _type = " + _type + ")"

  override def toJson: JsValue = Json.obj(
    "type" -> JsString(_type),
    "value" -> jsonValue
  )

  def toDBString = "__val" + value + ": " + _type

  def jsonValue: JsValue = {
    _type match {
      case "Int" => JsNumber(value.toString.toInt)
      case "Long" => value.asInstanceOf[Instance].toJson
      case "Property" => PropertyDAO.getByName(value.asInstanceOf[String]).toJson
    }
  }
}

object ParameterValue {
  val error = ParameterValue(0, "error")
}
