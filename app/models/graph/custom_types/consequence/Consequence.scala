package models.graph.custom_types.consequence

import models.instance_action.action.InstanceAction

/**
 * Class to define the effects from a need
 */
case class Consequence(id: Long, label: String, severity: Double, effect: InstanceAction)

object Consequence {

  val error = Consequence(0L, "error", 0, InstanceAction.error)

}
