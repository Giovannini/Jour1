package actors

import actors.Communication._
import actors.Communication.computing.{ComputeAction, ComputeConsequencies, ComputeNeed}
import actors.Communication.launcher.{EvaluateNeeds, NewTurn}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import controllers.Application
import models.graph.ontology.Instance
import models.graph.ontology.concept.Concept
import models.graph.ontology.relation.Relation
import models.interaction.LogInteraction
import models.interaction.action.InstanceActionParser

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

    master ! EvaluateNeeds
    // start the calculation
    master ! NewTurn

  }

  class InstanceIntelligence extends Actor {

    def getActionFor(instance: Instance, sensedInstances: List[Instance]): List[LogInteraction] = {
      val (action, destination) = instance.selectAction(sensedInstances)
      if (!action.isError) {
        println(instance.label + instance.id + " is doing " + action.label)
        InstanceActionParser.parseActionForLog(action.id, List(instance.id, destination.id))
      } else {
        List()
      }
    }

    def updatePropertiesFromEnvironment(instance: Instance, environment: List[Instance]): List[LogInteraction] = {
      /**
       * Count the number of element of a concept in the environment of the instance to update
       * @author Thomas GIOVANNINI
       * @param sourceOfMood concept to look for
       * @return the number of instance that has the desired type in the given environment
       */
      def countHumorSource(sourceOfMood: Concept): Int = {
        environment.count(_.concept.isSubConceptOf(sourceOfMood))
      }

      /**
       * Get the name of the property associated with the mood relation
       * @author Thomas GIOVANNINI
       * @param moodRelation relation from which the property name is desired
       * @return the label of the property
       */
      def getPropertyNameFromMood(moodRelation: Relation): String = {
        moodRelation.label
          .drop(5) //MOOD_ has length
          .toLowerCase
          .capitalize
      }

      instance.concept
        .getMoodRelations
        .map { tuple =>
          val instanceId = instance.id
          val propertyName = getPropertyNameFromMood(tuple._1)
          val propertyValue = countHumorSource(tuple._2)
          LogInteraction.createModifyLog(instanceId, propertyName, propertyValue)
        }
    }

    override def receive: Receive = {
      case ComputeAction(instance, sensedInstances) =>
        sender ! ResultAction(getActionFor(instance, sensedInstances))
      case ComputeConsequencies(instance) =>
        sender ! ResultAction(instance.applyConsequencies())
      case ComputeNeed(instance, sensedInstances) => //TODO
        sender ! ResultAction(updatePropertiesFromEnvironment(instance, sensedInstances))
    }
  }

  class World(nrOfWorkers: Int, listener: ActorRef) extends Actor {

    var logs: List[LogInteraction] = List()
    var nrOfResults: Int = _
    var nrOfInstances: Int = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
      Props[InstanceIntelligence].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

    override def receive: Actor.Receive = {
      case launcher: Launcher =>
        val instancesWithNeeds = Application.map.getInstances
          .filter(_.concept.needs.nonEmpty)
        nrOfInstances = instancesWithNeeds.length
        println(launcher.message + nrOfInstances + " instances.")
        for (instance <- instancesWithNeeds) {
          val environment = instance.getSensedInstances.flatMap(Application.map.getInstancesAt)
          workerRouter ! launcher.computation(instance, environment)
        }
      case ResultAction(logList) =>
        nrOfResults += 1
        logs = logList ::: logs
        if (nrOfResults == nrOfInstances) {
          logs.foreach(_.execute())
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