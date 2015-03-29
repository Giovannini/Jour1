package models.graph.ontology.concept.consequence

import models.interaction.action.{InstanceActionDAO, InstanceAction}
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

/**
 * Class to define the effects from a need
 */
case class Consequence(severity: Double, effects: List[InstanceAction]){
  def toDB: String = {
    severity + " - " + effects.map {effect =>
      println(effect.label + ": " + effect.id)
      effect.id
    }.mkString(", ")
  }

  def toJson = Json.obj(
    "severity" -> severity,
    "effects" -> effects.map(_.id)
  )
}

object Consequence {
  def parseString(stringToParse: String) = {
    Try {
      val splitted = stringToParse.split(" - ")
      val severity = splitted(0).toDouble
      val effects = splitted(1).split(", ").map(idToParse => InstanceActionDAO.getById(idToParse.toLong)).toList
      Consequence(severity, effects)
    } match {
      case Success(c) => c
      case Failure(e) =>
        println("Error while parsing a consequence from string: " + stringToParse)
        println(e.getStackTrace)
        error
    }
  }

  val error = Consequence(0, List(InstanceAction.error))

}
