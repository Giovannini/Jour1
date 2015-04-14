package actors

import models.graph.Instance

/**
 * Messages that can be send to the World actor
 * @author Thomas GIOVANNINI
 */
trait WorldCommunication

/**
 * Trait to characterize messages asking for a computation
 * @author Thomas GIOVANNINI
 */
trait Computing extends WorldCommunication {
  val message: String
}

/**
 * Trait to characterize messages launching a turn
 * @author Thomas GIOVANNINI
 */
trait Launcher extends WorldCommunication {
  val message: String
  def computation(instance: Instance, environment: List[Instance]): Computing
}




