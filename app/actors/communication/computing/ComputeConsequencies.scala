package actors.communication.computing

import actors.Computing
import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/30/15.
 */
case class ComputeConsequencies(instance: Instance) extends Computing {

  override val message: String = "Computing consequencies for "
}
