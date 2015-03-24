package actors

import akka.actor.{ActorSystem, Props, Actor, ActorRef}
import akka.routing.RoundRobinPool
import controllers.Application
import models.graph.ontology.property.PropertyDAO
import models.graph.ontology.{ValuedProperty, Instance}
import models.instance_action.action.ActionParser

/**
 * Actors to deal with parallelization of instance action computation.
 */
object Intelligence {

  def calculate(nrOfWorkers: Int) {
    // Create an Akka system
    val system = ActorSystem("IntelligenceSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    val master = system.actorOf(Props(new World(
      nrOfWorkers, listener)),
      name = "master")

    // start the calculation
    master ! Calculate
  }

  class InstanceIntelligence extends Actor {

    def getActionFor(instance: Instance, sensedInstances: List[Instance]): Boolean = {
      val (action, destination) = instance.selectAction(sensedInstances)
      ActionParser.parseAction(action.id, List(instance.id, destination.id)) // TODO change that with log
      //action.label + " " + instance.id + " " + destination.id
    }

    override def receive: Receive = {
      case ComputeAction(instance, sensedInstances) =>
        sender ! ResultAction(getActionFor(instance, sensedInstances))
    }
  }

  class World(nrOfWorkers: Int, listener: ActorRef) extends Actor {

    var logs: List[String] = _
    var nrOfResults: Int = _
    var nrOfInstances: Int = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
      Props[InstanceIntelligence].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

    override def receive: Actor.Receive = {
      case Calculate ⇒
        val propertyInstanciable = PropertyDAO.getByName("Instanciable")
        val instanciableInstances = Application.map.getInstances
          .filter(_.concept.rules.contains(ValuedProperty(propertyInstanciable, 1)))
        nrOfInstances = instanciableInstances.length
        println("Number of instances to compute: " + nrOfInstances)
        for (instance <- instanciableInstances)
          workerRouter ! ComputeAction(instance, instance.getSensedInstances.flatMap(Application.map.getInstancesAt))
      case ResultAction(log) =>
        nrOfResults += 1
        println((nrOfInstances - nrOfResults) + " remaining.")
        if (nrOfResults == nrOfInstances) {
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