package models.instance_action.precondition

import controllers.Application
import models.graph.ontology.Instance
import models.graph.ontology.property.{Property, PropertyDAO}

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
  def isNextTo(args: Array[Any]): Boolean = {
    val instance1 = map.getInstanceById(args(0).asInstanceOf[Long])
    val instance2 = map.getInstanceById(args(1).asInstanceOf[Long])
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
  def isOnSameTile(args: Array[Any]): Boolean = {
    val instance1 = map.getInstanceById(args(0).asInstanceOf[Long])
    val instance2 = map.getInstanceById(args(1).asInstanceOf[Long])
    val result = instance1.coordinates == instance2.coordinates
    result
  }

  /**
   * Check whether an instance is at walking distance of an other one or not
   * @author Thomas GIOVANNINI
   * @param args an array containing the two instances ids
   * @return true if the first instance can reach the second one by walking
   *         false else
   */
  def isAtWalkingDistance(args: Array[Any]): Boolean = {
    val propertyWalkingDistance = PropertyDAO.getByName("WalkingDistance")

    def retrieveWalkingDistanceValue(instance: Instance) = {
      instance.properties
        .find(_.property == propertyWalkingDistance)
        .getOrElse(propertyWalkingDistance.defaultValuedProperty)
        .value
        .asInstanceOf[Int]
    }

    val sourceInstance      = map.getInstanceById(args(0).asInstanceOf[Long])
    val destinationInstance = map.getInstanceById(args(1).asInstanceOf[Long])
    val desiredDistance     = retrieveWalkingDistanceValue(sourceInstance)
    val distance = sourceInstance.coordinates.getDistanceWith(destinationInstance.coordinates)
    distance <= desiredDistance
  }

  /**
   * Check whether an instance has a property or not.
   * @param args array containing the instance id and the property name
   * @return true if the instance has the desired property
   *         false else
   */
  def hasProperty(args: Array[Any]): Boolean = {
    val sourceInstance = map.getInstanceById(args(0).asInstanceOf[Long])
    val property = Property.parseString(args(1).asInstanceOf[String])

    sourceInstance.properties
      .map(_.property)
      .contains(property)
  }

  /**
   * Check whether a property is a number or not
   * @author Thomas GIOVANNINI
   * @param args array containing the property name
   * @return true if the property's value is a number
   *         false else
   */
  def isANumberProperty(args: Array[Any]): Boolean = {
    val property = Property.parseString(args(0).asInstanceOf[String])
    property.valueType == "Int" || property.valueType == "Double"
  }

  def isHigherThan(args: Array[Any]): Boolean = {
    /**
     * Get value for an instance of a given property
     * @author Thomas GIOVANNINI
     * @param instance from which the property is taken
     * @param property to look for
     * @return the desired value
     */
    def getValueOfProperty(property: Property, value: Any): Double = {
      if (property.valueType == "Int") value.asInstanceOf[Int]
      else value.asInstanceOf[Double]
    }
    val sourceInstance = map.getInstanceById(args(0).asInstanceOf[Long])
    val property = Property.parseString(args(1).asInstanceOf[String])
    val value = getValueOfProperty(property, args(2))
    val instanceValue = getValueOfProperty(property, sourceInstance.getValueForProperty(property))
    instanceValue > value
  }

}
