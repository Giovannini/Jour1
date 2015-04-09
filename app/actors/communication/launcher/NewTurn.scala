package actors.communication.launcher

import actors.communication.computing.ComputeAction
import actors.{Computing, Launcher}
import models.graph.Instance


case object NewTurn extends Launcher {

  override val message: String = "Computing actions for "

  override def computation(instance: Instance, environment: List[Instance]): Computing = {
    ComputeAction(instance, environment)
  }
}
