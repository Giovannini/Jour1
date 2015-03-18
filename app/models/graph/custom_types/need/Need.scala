package models.graph.custom_types.need

import models.graph.custom_types.consequence.ConsequenceStep

/**
 * Object used by instances to decide the action they'd better do.
 */
case class Need(id: Long, label: String, consequencesSteps: List[ConsequenceStep])

object Need {

  val error = Need(-1L, "error", List())

}
