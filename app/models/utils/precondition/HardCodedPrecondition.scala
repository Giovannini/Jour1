package models.utils.precondition

import models.graph.ontology.{Instance, Property}
import models.map.WorldMap

/**
 * List of hard codded preconditions
 */
object HardCodedPrecondition {

  /**
   * Precondition to check id an instance is next to an other
   * @author Thomas GIOVANNINI
   * @param args an array containing the two instances ids
   * @param map of the world
   * @return true if the two instances are next to each others
   *         false else
   */
  def isNextTo(args: Array[Any], map: WorldMap):Boolean = {
    val instance1 = map.getInstanceById(args(0).asInstanceOf[Int])
    val instance2 = map.getInstanceById(args(1).asInstanceOf[Int])
    val result = instance1.coordinates.isNextTo(instance2.coordinates)
    result
  }

  /**
   * Precondition to check if an instance is at walking distance of an other one
   * @author Thomas GIOVANNINI
   * @param args an array containing the two instances ids
   * @param map of the world
   * @return true if the first instance can reach the second one by walking
   *         false else
   */
  def isAtWalkingDistance(args: Array[Any], map: WorldMap): Boolean = {
    def retrieveWalkingDistanceValue(instance: Instance) = {
      val propertyWalkingDistance   = Property("WalkingDistance", "Int", 5)
      instance.properties
        .find(_.property == propertyWalkingDistance)
        .getOrElse(propertyWalkingDistance.defaultValuedProperty)
        .value
        .asInstanceOf[Int]
    }

    val sourceInstance      = map.getInstanceById(args(0).asInstanceOf[Int])
    val destinationInstance = map.getInstanceById(args(1).asInstanceOf[Int])
    val desiredDistance     = retrieveWalkingDistanceValue(sourceInstance)
    val distance = sourceInstance.coordinates.getDistanceWith(destinationInstance.coordinates)
    distance <= desiredDistance
  }

  /**
   * Precondition to check whether an instance has a property or not.
   * @param args array containing the instance id and the property name
   * @param map of the world
   * @return true if the instance has the desired property
   *         false else
   */
  def hasProperty(args: Array[Any], map: WorldMap): Boolean = {
    val sourceInstance = map.getInstanceById(args(0).asInstanceOf[Int])
    val propertyName = args(1).asInstanceOf[String]
    /** TODO => this is not clean. */
    sourceInstance.concept
      .properties
      .map(_.label)
      .contains(propertyName)
  }

}
