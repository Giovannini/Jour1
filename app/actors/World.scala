package actors

import actors.communication.launcher.NewTurn
import actors.communication._
import actors.socket.UpdateMap
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool
import controllers.Application
import controllers.map.MapController
import models.graph.Instance
import models.interaction.LogInteraction
import play.api.libs.json.{JsNumber, Json, JsValue}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

/**
 * Master actor that manages the whole world movements
 * @param nrOfWorkers nr of workers used for the pool of instance actors (basically it's the number of cores available if it's not distributed
 * @param listener listener actor that allows to send pack information to the main thread of the app
 */
class World(nrOfWorkers: Int, listener: ActorRef) extends Actor {

  var logs: (List[LogInteraction], List[LogInteraction], List[LogInteraction]) = (Nil, Nil, Nil) /* Logs for a single turn */
  var nrOfResults: Int = _ /* Number of results received */
  var nrOfInstances: Int = _ /* Number of instances that needs to be calculated */
  var start: Long = System.currentTimeMillis /* Starting point of the turn */
  var ongoing: Boolean = false /* Boolean that says if a turn is running */
  var looping: Boolean = false /* Boolean that says if a loop is running */

  /* Pool of workers that will calculate for each instances */
  val workerRouter = context.actorOf(
    Props[SmartInstance].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

  /**
   * Reinitilize the informations for the beginning of a turn
   * @author Thomas GIOVANNINI
   */
  def reinitialization() = {
    nrOfResults = 0
    nrOfInstances = 0
    start = System.currentTimeMillis()
  }

  /**
   * Retrieve all instances that have needs from the application map
   * @author Thomas GIOVANNINI
   * @return a list of instances that have needs
   */
  def getInstancesWithNeeds: List[Instance] = {
    Application.map.getInstancesWithNeeds
  }

  /**
   * Setter for the attribute "nrOfInstances" of this class
   * @author Thomas GIOVANNINI
   * @param number value to set
   */
  def setNumberOfInstancesToCompute(number: Int): Unit = {
    nrOfInstances = number
  }

  /**
   * Retrieve all the instances that a given instance can sense
   * @author Thomas GIOVANNINI
   * @param instance that sense its environment
   * @return the list of instances sensed by the instance
   */
  def getEnvironmentOf(instance: Instance): List[Instance] = {
    instance.getSensedInstances
  }

  /**
   * Receive method for the actor to receive messages from other actors
   * @author Thomas GIOVANNINI
   */
  override def receive: Actor.Receive = {
    case StartLoop =>
      looping = true
      println("Starting loop: ongoing = " + ongoing)
      self ! NewTurn
    case StopLoop =>
      println("Stopping loop")
      looping = false
      println("Looping = " + looping)
    case NewTurn =>
      launchComputation(NewTurn)
    case ResultAction(logList) =>
      nrOfResults += 1
      logs = mergeLogs(logList, logs)
      if (nrOfResults == nrOfInstances) {
        updateMap(logs)
        endComputation()
      }
  }

  def mergeLogs(triplet1: (List[LogInteraction], List[LogInteraction], List[LogInteraction]),
    triplet2: (List[LogInteraction], List[LogInteraction], List[LogInteraction]))
  : (List[LogInteraction], List[LogInteraction], List[LogInteraction]) = {
    (triplet1._1 ++ triplet2._1, triplet1._2 ++ triplet2._2, triplet1._3 ++ triplet2._3)
  }

  /**
   * End a computation at the end of a turn
   * If it needs to launch a new turn because it's looping, it sends a new message to trigger a new one
   * @author Thomas GIOVANNINI
   */
  private def endComputation(): Unit = {
    val end: Long = System.currentTimeMillis()
    listener ! EndOfTurn(end - start)
    ongoing = false

    if (looping) {
      reinitialization()
      context.system.scheduler.scheduleOnce(500 milliseconds, self, NewTurn)
      //self ! NewTurn
    } else {
      listener ! StopComputing
      for (worker <- 1 to nrOfWorkers) workerRouter ! StopComputing
      context.stop(self)
    }
  }

  /**
   * Update the current map from the list of logs the world gets at the end of computation
   * @author Thomas GIOVANNINI
   * @param logs logs about all the actions triggered durring a turn
   */
  private def updateMap(logs: (List[LogInteraction], List[LogInteraction], List[LogInteraction])): Unit = {
    val logList = logs match {
      case (consequencies, properties, actions) =>
        consequencies.sortBy(_.priority) ++
        properties.sortBy(_.priority) ++
        actions.sortBy(_.priority)
    }
    val resultInstances = logList.map(_.execute())
    val json = getMapModificationJson(logList, resultInstances)
//    println("End of a turn, updating the map.")
    MapController.mapSocketActor ! UpdateMap(json)
  }

  /**
   * Launch the computation implied by a given launcher
   * @author Thomas GIOVANNINI
   * @param launcher containing the information needed to do the computation
   */
  private def launchComputation(launcher: Launcher): Unit = {
    if (!ongoing) {
      logs = (Nil, Nil, Nil)
      ongoing = true
      val instancesWithNeeds = getInstancesWithNeeds
      setNumberOfInstancesToCompute(instancesWithNeeds.length)
      println("Launching computation for " + nrOfInstances + " instances.")
      for (instance <- instancesWithNeeds) {
        val environment = getEnvironmentOf(instance)
        workerRouter ! launcher.computation(instance, environment)
      }
    }
  }

  /**
   * Create a json that is readable by the client in order to update the view of the map
   * @author Thomas GIOVANNINI
   * @param logs of the actions done during the turn
   * @param instances all the instances of the map
   * @return Json Object that contains "add" -> all the new instances, and "remove" all the ids of the removed instances
   */
  private def getMapModificationJson(logs: List[LogInteraction], instances: List[Instance]): JsValue = {
    val (adds, dels) = logs.zip(instances)
      .filter { case (log, instance) =>
      log.isAddOrRemove && instance != Instance.error
    }.partition(_._1.value.startsWith("ADD"))
    val jsonAdds = adds.map(_._2.toJson)
    val jsonDels = dels.map {
      case (log, instance) => JsNumber(instance.id)
    }
    Json.obj(
      "add" -> jsonAdds,
      "remove" -> jsonDels
    )
  }
}
