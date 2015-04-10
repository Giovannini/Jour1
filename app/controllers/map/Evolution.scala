package controllers.map

import actors.communication.{StartLoop, StopLoop}
import actors.communication.launcher.NewTurn
import actors.{World, Listener}
import akka.actor.{Props, ActorSystem}
import play.api.mvc._

object Evolution extends Controller {
  private val nrOfWorkers = 4

  private def getMaster = {
    // Create an Akka system
    val system = ActorSystem("IntelligenceSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    system.actorOf(Props(new World(nrOfWorkers, listener)),
      name = "master")
  }

  def next() = Action {
    val master = getMaster

    // start the calculation
    master ! NewTurn
    Ok("OK")
  }

  def pause() = Action {
    val master = getMaster

    println("stopLoop")
    master ! StopLoop
    Ok("OK")
  }

  def resume() = Action {
    val master = getMaster

    println("startLoop")
    master ! StartLoop
    Ok("OK")
  }

}
