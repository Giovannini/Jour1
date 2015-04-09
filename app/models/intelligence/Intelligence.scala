package models.intelligence

import actors.communication.launcher.NewTurn
import actors.{Listener, World}
import akka.actor.{ActorSystem, Props}

/**
 * Actors to deal with parallelization of instance action computation.
 */

object Intelligence {

  def calculate(nrOfWorkers: Int): Unit = {
    // Create an Akka system
    val system = ActorSystem("IntelligenceSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    val master = system.actorOf(Props(new World(nrOfWorkers, listener)),
      name = "master")

    // start the calculation
    master ! NewTurn

  }
}