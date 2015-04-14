package models.graph

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}


case class DisplayProperty(color: String, zIndex: Int) {
  color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")

  def this(color: String) = this(color, 1)
  
  def this(zIndex: Int) = this("#aaaaaa", zIndex)

  def this() = this("#aaaaaa", 1)

  def toJson: JsValue = {
    Json.obj("color" -> JsString(color), "zindex" -> JsNumber(zIndex))
  }

  override def toString = "color:" + color + ", zindex:"+zIndex

}

object DisplayProperty {
  val form = Form(mapping(
                           "color" -> text,
                           "zindex" -> number
                         )(DisplayProperty.applyForm)(DisplayProperty.unapply))

  def apply() = new DisplayProperty()
  def apply(color: String) = new DisplayProperty(color)
  def apply(zIndex: Int) =new DisplayProperty(zIndex)
  def applyForm(color: String, zIndex: Int) = new DisplayProperty({ if(color.isEmpty()) "#aaaaaa" else color }, zIndex)

  def parseString(string: String): DisplayProperty = {
    val splittedString = string.split(", ").map(_.split(":"))
    val color = splittedString(0)(1)
    val zIndex = splittedString(1)(1).toInt
    DisplayProperty(color, zIndex)
  }

  def parseJson(json: JsValue): DisplayProperty = {
    val color = (json \ "color").as[String]
    val zIndex = (json \ "zindex").as[Int]
    DisplayProperty(color, zIndex)
  }
}
