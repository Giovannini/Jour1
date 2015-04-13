package actors

import actors.communication.{StopComputing, ResultAction}
import actors.communication.computing.ComputeAction
import akka.actor.Actor
import models.graph.Instance
import models.graph.concept.Concept
import models.graph.relation.Relation
import models.interaction.LogInteraction
import models.interaction.action.InstanceActionParser

class SmartInstance extends Actor {

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
      LogInteraction.createModifyLog(instanceId, propertyName, propertyValue)
    }

    instance.concept
      .getMoodRelations //TODO see if improvements are possible here
      .map(logModification)
  }


  def getActionFor(instance: Instance, sensedInstances: List[Instance]): List[LogInteraction] = {
    val (action, destination) = instance.selectAction(sensedInstances)
    if (!action.isError) {
      InstanceActionParser.parseActionForLog(action, List(instance.id, destination.id))
    } else {
      List()
    }
  }

  override def receive: Receive = {
    case ComputeAction(instance, sensedInstances) =>
      val consequencies = instance.applyConsequencies()
      val properties = updatePropertiesFromEnvironment(instance, sensedInstances) //TODO time consuming
      val actions = getActionFor(instance, sensedInstances)
      val logs = consequencies ++ properties ++ actions
      sender ! ResultAction(logs)
    case StopComputing =>
      context.stop(self)
  }
}
