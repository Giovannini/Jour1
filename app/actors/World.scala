package actors

import actors.communication.{EndOfTurn, ResultAction}
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool
import controllers.Application
import models.graph.ontology.Instance
import models.interaction.LogInteraction

class World(nrOfWorkers: Int, listener: ActorRef) extends Actor {

  var logs: List[List[LogInteraction]] = List()
  var nrOfResults: Int = _
  var nrOfInstances: Int = _
  val start: Long = System.currentTimeMillis

  val workerRouter = context.actorOf(
    Props[SmartInstance].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

  /**
   * Retrieve all instances that have needs from the application map
   * @author Thomas GIOVANNINI
   * @return a list of instances that have needs
   */
  def getInstancesWithNeeds: List[Instance] = {
//    val t1 = System.currentTimeMillis()
    val result = Application.map
      .getInstances
      .filter(_.concept.needs.nonEmpty)
//    val t2 = System.currentTimeMillis()
//    println("getInstancesWithNeeds: " + (t2 - t1))
    result
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
      .flatMap(Application.map.getInstancesAt)
  }

  /**
   * Receive method for the actor to receive messages from other actors
   * @author Thomas GIOVANNINI
   */
  override def receive: Actor.Receive = {
    case launcher: Launcher =>
      println("Launching new turn computation...")
      launchComputation(launcher)
    case ResultAction(logList) =>
      nrOfResults += 1
      logs = logList :: logs
      print(nrOfInstances - nrOfResults + " ")
      if (nrOfResults == nrOfInstances) {
        logs.flatten.foreach(_.execute())
        val end: Long = System.currentTimeMillis()
        listener ! EndOfTurn(end - start)
        //context.stop(self)
      }
  }

  /**
   * Launch the computation implied by a given launcher
   * @author Thomas GIOVANNINI
   * @param launcher containing the information needed to do the computation
   */
  private def launchComputation(launcher: Launcher): Unit = {
    val instancesWithNeeds = getInstancesWithNeeds
    setNumberOfInstancesToCompute(instancesWithNeeds.length)
//    println(launcher.message + nrOfInstances + " instances.")
    for (instance <- instancesWithNeeds) {
      val environment = getEnvironmentOf(instance)
      workerRouter ! launcher.computation(instance, environment)
    }
  }
}
