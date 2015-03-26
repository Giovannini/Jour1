package actors

import models.graph.ontology.Instance
import models.instance_action.action.LogAction

/**
 * Created by giovannini on 3/23/15.
 */
trait WorldCommunication
case object NewTurn extends WorldCommunication
case class ComputeAction(instance: Instance, sensedInstances: List[Instance]) extends WorldCommunication
case class ResultAction(actionLog: List[LogAction]) extends WorldCommunication
case class Sense(instance: Instance) extends WorldCommunication
case class EndOfTurn(time: Double) extends WorldCommunication