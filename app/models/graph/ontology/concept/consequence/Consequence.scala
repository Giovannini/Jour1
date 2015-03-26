package models.graph.ontology.concept.consequence

import models.instance_action.action.InstanceAction
import play.api.libs.json.Json

/**
 * Class to define the effects from a need
 */
case class Consequence(severity: Double, effects: List[InstanceAction]){
  def toDB: String = severity + " - " + effects.map(_.id).mkString(", ")

  def toJson = Json.obj(
    "severity" -> severity,
    "effects" -> effects.map(_.id)
  )
}

object Consequence {
  def parseString(stringToParse: String) = {
    val splitted = stringToParse.split(" - ")
    val severity = splitted(0).toDouble
    val effects = splitted(1).split(", ").map(idToParse => InstanceAction.getById(idToParse.toLong)).toList
    Consequence(severity, effects)
  }

  val error = Consequence(0, List(InstanceAction.error))

}
