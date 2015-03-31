package actors

import models.graph.ontology.Instance

/**
 * Created by giovannini on 3/23/15.
 */
trait WorldCommunication
trait Computing extends WorldCommunication {
  val message: String
}
trait Launcher extends WorldCommunication {
  val message: String
  def computation(instance: Instance, environment: List[Instance]): Computing
}




