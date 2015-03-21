package models.instance_action.parameter

import models.graph.ontology.Instance
import models.graph.ontology.property.Property
import play.api.libs.json.{JsNumber, JsString, Json, JsValue}


case class ParameterValue(value: Any, _type: String) extends Parameter {
  override def toJson: JsValue = Json.obj(
    "type" -> JsString(_type),
    "value" -> jsonValue
  )

  def toDBString = "__val"+value + ": " + _type

  def jsonValue: JsValue = _type match {
    case "Int" => JsNumber(value.asInstanceOf[Int])
    case "Long" => value.asInstanceOf[Instance].toJson
    case "Property" => value.asInstanceOf[Property].toJson
  }
}
