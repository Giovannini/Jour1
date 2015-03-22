package models.graph.ontology.concept.consequence

import forms.InstanceActionForm
import models.instance_action.action.InstanceAction
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

/**
 * Class to define the effects from a need
 */
case class Consequence(severity: Double, effects: List[InstanceAction]){
  def toDB: String = severity + " - " + effects.map(_.id).mkString(", ")
}

object Consequence {

  def parseString(stringToParse: String) = {
    val splitted = stringToParse.split(" - ")
    val severity = splitted(0).toDouble
    val effects = splitted(1).split(", ").map(idToParse => InstanceAction.getById(idToParse.toLong)).toList
    Consequence(severity, effects)
  }

  val form = Form(mapping(
    "severity" -> of[Double],
    "effects" -> list(InstanceActionForm.form.mapping)
  )(Consequence.apply)(Consequence.unapply))

  val error = Consequence(0, List(InstanceAction.error))

}
