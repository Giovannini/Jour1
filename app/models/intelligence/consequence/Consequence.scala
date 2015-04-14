package models.intelligence.consequence

import models.interaction.effect.{Effect, EffectDAO}
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

/**
 * Class to define the effects from a need
 */
case class Consequence(severity: Double, effect: Effect){
  def toDB: String = {
    severity + " - " + effect.id
  }

  def toJson = Json.obj(
    "severity" -> severity,
    "effect" -> effect.id
  )
}

object Consequence {
  def parseString(stringToParse: String) = {
    Try {
      val splitted = stringToParse.split(" - ")
      val severity = splitted(0).toDouble
      val effect = EffectDAO.getById(splitted(1).toLong)
      Consequence(severity, effect)
    } match {
      case Success(c) => c
      case Failure(e) =>
        println("Error while parsing a consequence from string: " + stringToParse)
        println(e.getStackTrace)
        error
    }
  }

  val error = Consequence(0, Effect.error)

}
