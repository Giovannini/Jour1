package actors

import actors.communication.{StopComputing, EndOfTurn}
import akka.actor.Actor

/**
 * Listener actor that allows the main thread to print in the console
 * Used in {@link controllers.map.Evolution}
 * @author Thomas Giovannini
 */
class Listener extends Actor {

  def receive = {
    case EndOfTurn(time) =>
      Console.println(s"\tAll actions executed in: $time")
    case StopComputing =>
      context.stop(self)
  }
}
