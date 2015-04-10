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

class World(nrOfWorkers: Int, listener: ActorRef) extends Actor {

  var logs: List[List[LogInteraction]] = List()
  var nrOfResults: Int = _
  var nrOfInstances: Int = _
  val start: Long = System.currentTimeMillis
  var ongoing: Boolean = false
  var looping: Boolean = false

  val workerRouter = context.actorOf(
    Props[SmartInstance].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

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
      self ! NewTurn
    case StopLoop =>
      looping = false
    case NewTurn =>
      println("Launching new turn computation...")
      launchComputation(NewTurn)
    case ResultAction(logList) =>
      nrOfResults += 1
      logs = logList :: logs
      if (nrOfResults == nrOfInstances) {
        updateMap(logs)
        endComputation()
      }
  }

  private def endComputation(): Unit = {
    val end: Long = System.currentTimeMillis()
    for(worker <- 1 to nrOfWorkers) workerRouter ! StopComputing
    listener ! EndOfTurn(end - start)
    ongoing = false

    if(looping) {
      self ! NewTurn
    } else {
      listener ! StopComputing
      context.stop(self)
    }
  }

  private def updateMap(logs: List[List[LogInteraction]]): Unit = {
    val logList = logs.flatten.sortBy(_.priority)
    val resultInstances = logList.map(_.execute())
    val json = getMapModificationJson(logList, resultInstances)
    MapController.mapSocketActor ! UpdateMap(json)
  }

  /**
   * Launch the computation implied by a given launcher
   * @author Thomas GIOVANNINI
   * @param launcher containing the information needed to do the computation
   */
  private def launchComputation(launcher: Launcher): Unit = {
    if(!ongoing) {
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

  private def getMapModificationJson(logs: List[LogInteraction], instances: List[Instance]): JsValue = {
    val (adds, dels) = logs.zip(instances)
      .filter {
      case (log, instance) => log.isAddOrRemove && instance != Instance.error
    }
      .partition(_._1.value.startsWith("ADD"))
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
