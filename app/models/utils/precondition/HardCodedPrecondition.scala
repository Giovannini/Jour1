package models.utils.precondition

import models.graph.ontology.Property
import models.map.WorldMap

/**
 * List of hard codded preconditions
 */
object HardCodedPrecondition {

  def isNextTo(args: Array[Any], map: WorldMap):Boolean = {
    val instance1 = map.getInstanceById(args(0).asInstanceOf[Int])
    val instance2 = map.getInstanceById(args(1).asInstanceOf[Int])
    val result = instance1.coordinates.isNextTo(instance2.coordinates)
    //println("Is " + instance1.coordinates + " next to " + instance2.coordinates + ": " + result)
    result
  }

  def isAtWalkingDistance(args: Array[Any], map: WorldMap): Boolean = {
    val propertyWalkingDistance   = Property("WalkingDistance", "Int", 5)
    val sourceInstance      = map.getInstanceById(args(0).asInstanceOf[Int])
    val destinationInstance = map.getInstanceById(args(1).asInstanceOf[Int])
    val desiredDistance     = sourceInstance.properties
      .find(_.property == propertyWalkingDistance)
      .getOrElse(propertyWalkingDistance.defaultValuedProperty)
      .value
      .asInstanceOf[Int]
    val distance = sourceInstance.coordinates.getDistanceWith(destinationInstance.coordinates)
    //println(distance + " < " + desiredDistance)
    distance < desiredDistance
  }

}
