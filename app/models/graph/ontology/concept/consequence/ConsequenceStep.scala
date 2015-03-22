package models.graph.ontology.concept.consequence

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

import scala.util.{Failure, Success, Try}

/**
 * Created by giovannini on 3/18/15.
 */
case class ConsequenceStep(value: Double, consequence: Consequence) {

  def toDB = value + " -> " + consequence.toDB

}

object ConsequenceStep{
  val form = Form(mapping(
     "value" -> of[Double],
     "consequence" -> Consequence.form.mapping
   )(ConsequenceStep.apply)(ConsequenceStep.unapply))
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
