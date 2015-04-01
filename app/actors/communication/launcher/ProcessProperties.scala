package actors.communication.launcher

import actors.communication.computing.ComputePropertiesUpdate
import actors.{Computing, Launcher}
import models.graph.ontology.Instance


case object ProcessProperties extends Launcher {

  override val message: String = "Evaluating needs of "

  override def computation(instance: Instance, environment: List[Instance]): Computing = {
    ComputePropertiesUpdate(instance, environment)
  }
}
