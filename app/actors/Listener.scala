package actors

import actors.communication.EndOfTurn
import akka.actor.Actor

class Listener extends Actor {

  def receive = {
    case EndOfTurn(time) ⇒
      println(s"\n\tAll actions executed in: $time")
      context.system.shutdown()
  }
}
