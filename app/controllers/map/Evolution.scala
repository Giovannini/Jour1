package controllers.map

import actors.communication.{StartLoop, StopLoop}
import actors.communication.launcher.NewTurn
import actors.{World, Listener}
import akka.actor.{ActorRef, Props, ActorSystem}
import play.api.mvc._

/**
 * Managing the automated evolution of the world
 */
object Evolution extends Controller {
  private val nrOfWorkers = 4 /* Number of workers available (most of the time it's the number of cpu/cores available) */

  var master: ActorRef = _ /* World Actor */
  var ongoing: Boolean = false /* Boolean that says if the loop is ongoing or not */

  /**
   * Create a master actor that will take care of the evolution
   */
  private def getMaster() = {
    // Create an Akka system
    val system = ActorSystem("IntelligenceSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    master = system.actorOf(Props(new World(nrOfWorkers, listener)),
      name = "master")
  }

  /**
   * Create a master that will do only one iteration of the evolution
   * @return Success of failure of the operation
   */
  def next() = Action {
    if(!ongoing) {
      getMaster()
      master ! NewTurn
      Ok("OK")
    } else {
      InternalServerError("Loop is already ongoing")
    }
  }

  /**
   * Tells the ongoing master that it can end the loop
   * @return Success of failure of the operation
   */
  def pause() = Action {
    if(ongoing) {
      ongoing = false
      master ! StopLoop
      Ok("OK")
    } else {
      InternalServerError("No loop is running")
    }
  }

  /**
   * Create a new master that launches a new loop
   * @return Success of failure of the operation
   */
  def resume() = Action {
    if(!ongoing) {
      ongoing = true
      getMaster()
      master ! StartLoop
      Ok("OK")
    } else {
      InternalServerError("Loop is already ongoing")
    }
  }

}
