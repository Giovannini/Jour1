package models.graph.custom_types

import play.api.libs.json.{JsValue, JsNumber, JsString, Json}


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

  def apply() = new DisplayProperty()
  def apply(color: String) = new DisplayProperty(color)
  def apply(zIndex: Int) =new DisplayProperty(zIndex)

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
