package actors.communication.computing

import actors.Computing
import models.graph.Instance

/**
 * Created by giovannini on 3/30/15.
 */
case class ComputeAction(instance: Instance, sensedInstances: List[Instance]) extends Computing {
  override val message: String = "Computing actions for "
}
