package actors.Communication

import actors.WorldCommunication
import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/30/15.
 */
case class Sense(instance: Instance) extends WorldCommunication
