package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import controllers.Application
import models.graph.ontology.Instance
import models.instance_action.action.{InstanceAction, LogAction, ActionParser}

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

  class InstanceIntelligence extends Actor {

    def getActionFor(instance: Instance, sensedInstances: List[Instance]): List[LogAction] = {
      val (action, destination) = instance.selectAction(sensedInstances)
      if (action != InstanceAction.error) {
        val logs = ActionParser.parseActionForLog(action.id, List(instance.id, destination.id))
        logs.foreach(log => println(instance.id + " - " + log.value))
        logs
      } else {
        List()
      }
    }

    override def receive: Receive = {
      case ComputeAction(instance, sensedInstances) =>
        sender ! ResultAction(getActionFor(instance, sensedInstances))
    }
  }

  class World(nrOfWorkers: Int, listener: ActorRef) extends Actor {

    var logs: List[LogAction] = List()
    var nrOfResults: Int = _
    var nrOfInstances: Int = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
      Props[InstanceIntelligence].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

    override def receive: Actor.Receive = {
      case NewTurn ⇒
        val instancesWithNeeds = Application.map.getInstances
          .filter(_.concept.needs.nonEmpty)
        nrOfInstances = instancesWithNeeds.length
        println("Number of instances to compute: " + nrOfInstances)
        for (instance <- instancesWithNeeds)
          workerRouter ! ComputeAction(instance, instance.getSensedInstances.flatMap(Application.map.getInstancesAt))
      case ResultAction(logList) =>
        nrOfResults += 1
        logs = logList ::: logs
        if (nrOfResults == nrOfInstances) {
          logs /*.sortBy(_.value)*/ .foreach(_.execute)
          val end: Long = System.currentTimeMillis()
          listener ! EndOfTurn(end - start)
          context.stop(self)
        }
    }
  }

  class Listener extends Actor {

    def receive = {
      case EndOfTurn(time) ⇒
        println("\n\tAll actions executed in: \t\t%s"
          .format(time))
        context.system.shutdown()
    }
  }

}