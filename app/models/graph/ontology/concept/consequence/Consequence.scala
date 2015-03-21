package models.graph.ontology.concept.consequence

import models.instance_action.action.InstanceAction
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

/**
 * Class to define the effects from a need
 */
case class Consequence(id: Long, label: String, severity: Double, effects: List[InstanceAction])

object Consequence {
  val form = Form(mapping(
    "id" -> longNumber,
    "label" -> text,
    "severity" -> of[Double],
    "effects" -> list(InstanceAction.form.mapping)
  )(Consequence.apply)(Consequence.unapply))

  val error = Consequence(0L, "error", 0, List(InstanceAction.error))

}
