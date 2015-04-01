package actors.communication.launcher

import actors.communication.computing.ComputeNeed
import actors.{Computing, Launcher}
import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/30/15.
 */
case object EvaluateNeeds extends Launcher {

  override val message: String = "Evaluating needs of "

  override def computation(instance: Instance, environment: List[Instance]): Computing = {
    ComputeNeed(instance, environment)
  }
}
