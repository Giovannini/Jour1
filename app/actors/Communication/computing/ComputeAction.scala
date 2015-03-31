package actors.Communication.computing

import actors.Computing
import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/30/15.
 */
case class ComputeAction(instance: Instance, sensedInstances: List[Instance]) extends Computing {

  override val message: String = "Computing actions for "
}
