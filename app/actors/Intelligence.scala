package actors

import actors.Communication._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import controllers.Application
import models.graph.ontology.{ValuedProperty, Instance}
import models.graph.ontology.concept.Concept
import models.graph.ontology.concept.need.Need
import models.graph.ontology.property.{PropertyDAO, Property}
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

    //master ! EvaluateNeeds
    // start the calculation
    master ! NewTurn

  }

  class InstanceIntelligence extends Actor {

    def getActionFor(instance: Instance, sensedInstances: List[Instance]): List[LogInteraction] = {
      val (action, destination) = instance.selectAction(sensedInstances)
      if (! action.isError) {
        println(instance.label + instance.id + " is doing " + action.label)
        InstanceActionParser.parseActionForLog(action.id, List(instance.id, destination.id))
      } else {
        List()
      }
    }

    //TODO
    /*
     * Idea for FEAR property:
     * Count number of elements related with relation SCARE which is not an action
     * If property FEAR value is higher than this number, reduce property by one
     * Else set property value FEAR to this sum
     */
    def updateFromSenses(instance: Instance, sensedInstances: List[Instance]): List[LogInteraction] = {
      def recursiveUpdate(needs: List[Need], instance: Instance): List[LogInteraction] = needs match {
        case head::tail =>
          val property = head.affectedProperty
          val propertyValue = instance.getValueForProperty(property)
          val steps = head.consequencesSteps
          val logs = steps.filter(cs => cs.value <= propertyValue).lastOption match {
            case Some(consequenceStep) => consequenceStep.consequence.effect.logOn(instance)
            case _ => List()
          }
          logs ::: recursiveUpdate(tail, instance)
        case _ => List()
      }

      def updatePropertiesFromEnvironment(instance: Instance, environment: List[Instance]): Instance = {
        def countHumorSource(sourceOfHumor: Concept) = {
          environment.count(_.concept.isSubConceptOf(sourceOfHumor))
        }

        def getPropertyFromHumor(humorRelation: Relation): Property = {
          val propertyName = humorRelation.label
            .drop(6) //HUMOR_ has length 6
            .toLowerCase
            .capitalize
          PropertyDAO.getByName(propertyName)
        }

        val newHumorProperties = instance.concept
          .getHumorRelations
          .map(tuple => ValuedProperty(getPropertyFromHumor(tuple._1), countHumorSource(tuple._2)))
        instance.updateProperties(newHumorProperties)
        instance
      }

      recursiveUpdate(instance.concept.needs, instance)
    }

    override def receive: Receive = {
      case ComputeAction(instance, sensedInstances) =>
        sender ! ResultAction(getActionFor(instance, sensedInstances))
      case ComputeNeed(instance, sensedInstances) => //TODO
        sender ! ResultAction(updateFromSenses(instance, sensedInstances))
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
      case NewTurn ⇒
        val instancesWithNeeds = Application.map.getInstances
          .filter(_.concept.needs.nonEmpty)
        nrOfInstances = instancesWithNeeds.length
        println("Computing actions for " + nrOfInstances + " instances.")
        for (instance <- instancesWithNeeds)
          workerRouter ! ComputeAction(instance, instance.getSensedInstances.flatMap(Application.map.getInstancesAt))
      case EvaluateNeeds =>
        val instancesWithNeeds = Application.map.getInstances
          .filter(_.concept.needs.nonEmpty)
        nrOfInstances = instancesWithNeeds.length
        println("Evaluating needs of " + nrOfInstances + " instances.")
        for (instance <- instancesWithNeeds)
          workerRouter ! ComputeNeed(instance, instance.getSensedInstances.flatMap(Application.map.getInstancesAt))
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