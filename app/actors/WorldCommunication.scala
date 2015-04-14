package actors

import models.graph.Instance

/**
 * Messages that can be send to the World actor
 * @author Thomas GIOVANNINI
 */
trait WorldCommunication

// TODO DOC
trait Computing extends WorldCommunication {
  val message: String
}

// TODO DOC
trait Launcher extends WorldCommunication {
  val message: String
  def computation(instance: Instance, environment: List[Instance]): Computing
}




