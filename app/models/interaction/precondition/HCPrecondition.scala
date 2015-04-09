package models.interaction.precondition

import controllers.Application
import models.graph.property.PropertyDAO
import models.interaction.parameter.{ParameterReference, ParameterValue}

/**
 * List of hard codded preconditions
 */
object HCPrecondition {

  val map = Application.map

  /**
   * Check whether an instance is next to an other or not
   * @author Thomas GIOVANNINI
   * @param args an array containing the two instances ids
   * @return true if the two instances are next to each others
   *         false else
   */
  def isNextTo(args: Map[ParameterReference, ParameterValue]): Boolean = {
    val instance1ID = args(ParameterReference("instance1ID", "Long")).value.asInstanceOf[Long]
    val instance2ID = args(ParameterReference("instance2ID", "Long")).value.asInstanceOf[Long]

    val instance1 = map.getInstanceById(instance1ID)
    val instance2 = map.getInstanceById(instance2ID)

    val result = instance1.coordinates.isNextTo(instance2.coordinates)
    result
  }

  /**
   * Precondition to check if an instance is on same tile as an other
   * @author Thomas GIOVANNINI
   * @param args an array containing the two instances ids
   * @return true if the two instances are on same tile
   *         false else
   */
  def isOnSameTile(args: Map[ParameterReference, ParameterValue]): Boolean = {
    val instance1ID = args(ParameterReference("instance1ID", "Long")).value.asInstanceOf[Long]
    val instance2ID = args(ParameterReference("instance2ID", "Long")).value.asInstanceOf[Long]

    val instance1 = map.getInstanceById(instance1ID)
    val instance2 = map.getInstanceById(instance2ID)

    instance1.coordinates == instance2.coordinates
  }

  /**
   * Check whether an instance is at walking distance of an other one or not
   * @author Thomas GIOVANNINI
   * @param args an array containing the two instances ids
   * @return true if the first instance can reach the second one by walking
   *         false else
   */
  def isAtWalkingDistance(args: Map[ParameterReference, ParameterValue]): Boolean = {
    val propertyWalkingDistance = PropertyDAO.getByName("WalkingDistance")

    val instance1ID = args(ParameterReference("instance1ID", "Long")).value.asInstanceOf[Long]
    val instance2ID = args(ParameterReference("instance2ID", "Long")).value.asInstanceOf[Long]

    val sourceInstance      = map.getInstanceById(instance1ID)
    val destinationInstance = map.getInstanceById(instance2ID)
    val desiredDistance     = sourceInstance.getValueForProperty(propertyWalkingDistance)
    val distance = sourceInstance.coordinates.getDistanceWith(destinationInstance.coordinates)
    distance <= desiredDistance
  }

  /**
   * Check whether an instance has a property or not.
   * @param args array containing the instance id and the property name
   * @return true if the instance has the desired property
   *         false else
   */
  def hasProperty(args: Map[ParameterReference, ParameterValue]): Boolean = {
    val instanceId = args(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("property", "Property")).value.asInstanceOf[String]
    val sourceInstance = map.getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    sourceInstance.properties
      .map(_.property)
      .contains(property)
  }

  def isHigherThan(args: Map[ParameterReference, ParameterValue]): Boolean = {
    val instanceId = args(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("property", "Property")).value.asInstanceOf[String]
    val sourceInstance = map.getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    val value = args(ParameterReference("value", "Int")).value.asInstanceOf[String].toDouble
    val instanceValue = sourceInstance.getValueForProperty(property)
    instanceValue > value
  }

  def hasInstanceOfConcept(args: Map[ParameterReference, ParameterValue]):Boolean={
    val instanceId = args(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
    val conceptId = args(ParameterReference("ConceptID", "Long")).value.asInstanceOf[Long]

    val instance = map.getInstanceById(instanceId)
    val listInstance = map.getInstancesAt(instance.coordinates)
    listInstance.exists(p=>p.concept.id == conceptId)
  }

  def isSelf(args: Map[ParameterReference, ParameterValue]):Boolean={
    val instance1ID = args(ParameterReference("instance1ID", "Long")).value.asInstanceOf[Long]
    val instance2ID = args(ParameterReference("instance2ID", "Long")).value.asInstanceOf[Long]
    instance1ID == instance2ID
  }

  def notSelf(args: Map[ParameterReference, ParameterValue]):Boolean={
    ! isSelf(args)
  }

  def isDifferentConcept(args: Map[ParameterReference, ParameterValue]):Boolean={
    val instance1ID = args(ParameterReference("instance1ID", "Long")).value.asInstanceOf[Long]
    val instance2ID = args(ParameterReference("instance2ID", "Long")).value.asInstanceOf[Long]
    Application.map.getInstanceById(instance1ID).concept != Application.map.getInstanceById(instance2ID).concept
  }

}
