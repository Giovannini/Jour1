package actors

import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/23/15.
 */
trait WorldCommunication
case object NewTurn extends WorldCommunication
case class ComputeAction(instance: Instance, sensedInstances: List[Instance]) extends WorldCommunication
case class ResultAction(actionLog: Boolean) extends WorldCommunication // TODO change that
case class Sense(instance: Instance) extends WorldCommunication
case class EndOfTurn(time: Double) extends WorldCommunication