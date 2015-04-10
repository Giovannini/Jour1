package actors

import actors.communication.{StopComputing, EndOfTurn}
import akka.actor.Actor

class Listener extends Actor {

  def receive = {
    case EndOfTurn(time) =>
      println(s"\n\tAll actions executed in: $time")
    case StopComputing =>
      context.stop(self)
  }
}
