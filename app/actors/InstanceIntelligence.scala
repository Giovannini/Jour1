package actors

import actors.communication.ResultAction
import actors.communication.computing.{ComputeAction, ComputePropertiesUpdate}
import akka.actor.Actor
import models.graph.ontology.Instance
import models.graph.ontology.concept.Concept
import models.graph.ontology.relation.Relation
import models.interaction.LogInteraction
import models.interaction.action.InstanceActionParser

class InstanceIntelligence extends Actor {

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
      moodRelation.label  //MOOD_SCARED
        .drop(5)          // SCARED
        .toLowerCase      // scared
        .capitalize       // Scared
    }

    /**
     * Create a LogInteraction making a property modification from a relation by sensing the environment of smart instance
     * @author Thomas GIOVANNINI
     * @param tuple containing the relation and the destination concept
     * @return a LogInteraction
     */
    def logModification(tuple: (Relation, Concept)): LogInteraction = {
      val instanceId = instance.id
      val propertyName = getPropertyNameFromMood(tuple._1)
      val propertyValue = countMoodSource(tuple._2)
      println("Creating log for relation: " + instanceId + " - " + propertyName + ": " + propertyValue)
      LogInteraction.createModifyLog(instanceId, propertyName, propertyValue)
    }

    instance.concept
      .getMoodRelations
      .map(logModification)
  }


  def getActionFor(instance: Instance, sensedInstances: List[Instance]): List[LogInteraction] = {
    val (action, destination) = instance.selectAction(sensedInstances)
    if (!action.isError) {
      //        println(instance.label + instance.id + " is doing " + action.label)
      InstanceActionParser.parseActionForLog(action.id, List(instance.id, destination.id))
    } else {
      List()
    }
  }

  override def receive: Receive = {
    case ComputePropertiesUpdate(instance, sensedInstances) =>
      val logs = instance.applyConsequencies() ++ updatePropertiesFromEnvironment(instance, sensedInstances)
      sender ! ResultAction(logs)
    case ComputeAction(instance, sensedInstances) =>
      sender ! ResultAction(getActionFor(instance, sensedInstances))
  }
}
