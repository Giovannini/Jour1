package actors

import actors.communication.ResultAction
import actors.communication.computing.{ComputeNeed, ComputeConsequencies, ComputeAction}
import akka.actor.Actor
import models.graph.ontology.Instance
import models.graph.ontology.concept.Concept
import models.graph.ontology.relation.Relation
import models.interaction.LogInteraction
import models.interaction.action.InstanceActionParser

class InstanceIntelligence extends Actor {

  def getActionFor(instance: Instance, sensedInstances: List[Instance]): List[LogInteraction] = {
    val (action, destination) = instance.selectAction(sensedInstances)
    if (!action.isError) {
      //        println(instance.label + instance.id + " is doing " + action.label)
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
    def countMoodSource(sourceOfMood: Concept): Int = {
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
      val propertyValue = countMoodSource(tuple._2)
      LogInteraction.createModifyLog(instanceId, propertyName, propertyValue)
    }
  }

  override def receive: Receive = {
    case ComputeConsequencies(instance) =>
      sender ! ResultAction(instance.applyConsequencies())
    case ComputeNeed(instance, sensedInstances) => //TODO
      sender ! ResultAction(updatePropertiesFromEnvironment(instance, sensedInstances))
    case ComputeAction(instance, sensedInstances) =>
      sender ! ResultAction(getActionFor(instance, sensedInstances))
  }
}
