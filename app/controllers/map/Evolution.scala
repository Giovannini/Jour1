package controllers.map

import actors.communication.{StartLoop, StopLoop}
import actors.communication.launcher.NewTurn
import actors.{World, Listener}
import akka.actor.{ActorRef, Props, ActorSystem}
import play.api.mvc._

object Evolution extends Controller {
  private val nrOfWorkers = 4

  private val master = {
    // Create an Akka system
    val system = ActorSystem("IntelligenceSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    system.actorOf(Props(new World(nrOfWorkers, listener)), name = "master")
  }

  def next() = Action {
    master ! NewTurn
    Ok("OK")
  }

  def pause() = Action {
    master ! StopLoop
    Ok("OK")
  }

  def resume() = Action {
    master ! StartLoop
    Ok("OK")
  }

}
