package models.rules

import play.api.libs.json.JsString


case class Argument(reference: String, _type: String){
  require(reference.matches("^[a-z][A-Za-z0-9_]*$"))

  def toJson = JsString(reference)
}

object Argument {

  val error = Argument("error", "error")

  def parseArgument(argumentToString: String): Argument = {
    val splittedString = argumentToString.split(": ")
    Argument(splittedString(0), splittedString(1))
  }

  def parseArgumentList(argumentListToString: String): List[Argument] = {
    argumentListToString.split(", ")
      .map(parseArgument)
      .toList
  }
}
