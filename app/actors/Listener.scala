package actors

import actors.communication.EndOfTurn
import akka.actor.Actor

class Listener extends Actor {

  def receive = {
    case EndOfTurn(time) ⇒
      println("\n\tAll actions executed in: \t\t%s"
        .format(time))
      context.system.shutdown()
  }
}
