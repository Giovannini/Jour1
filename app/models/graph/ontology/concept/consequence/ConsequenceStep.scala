package models.graph.ontology.concept.consequence

import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

/**
 * Step for a consequence
 */
case class ConsequenceStep(value: Double, consequence: Consequence) {
  def toDB = value + " -> " + consequence.toDB

  def toJson = Json.obj(
    "value" -> value,
    "consequence" -> consequence.toJson
  )
}

object ConsequenceStep{
  val error = ConsequenceStep(-1L, Consequence.error)

  /**
   * Parse a string to a consequence step
   * @author Thomas GIOVANNINI
   * @param string to parse
   * @return the consequence step
   */
  def parseString(string: String): ConsequenceStep = {
    Try {
      val splittedString = string.split(" -> ")
      val value = splittedString(0).toDouble
      val consequence = Consequence.parseString(splittedString(1))

      ConsequenceStep(value, consequence)
    } match {
      case Success(cs) => cs
      case Failure(e) =>
        println("Exception while parsing a ConsequenceStep from the string " + string)
        println(e)
        error
    }
  }

  /**
   * Parse a string to a list of consequence steps
   * @author Thomas GIOVANNINI
   * @param string to parse
   * @return the consequence steps list
   */
  def parseList(string: String): List[ConsequenceStep] = {
    Try {
      string.split(";").map(parseString).toList
    } match {
      case Success(list) => list
      case Failure(e) =>
        println("Exception while parsing a ConsequenceStep from the string " + string)
        println(e)
        List()
    }
  }

}
