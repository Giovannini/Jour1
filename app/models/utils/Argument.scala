package models.utils

/**
 * Created by giovannini on 2/10/15.
 */
case class Argument(reference: String, _type: String){
  require(reference.matches("^[a-z][A-Za-z0-9_]*$"))
}

object Argument {

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
