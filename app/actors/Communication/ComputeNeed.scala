package actors.Communication

import actors.WorldCommunication
import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/30/15.
 */
case class ComputeNeed(instance: Instance, sensedInstances: List[Instance]) extends WorldCommunication
