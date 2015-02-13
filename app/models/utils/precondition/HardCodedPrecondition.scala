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
    instance1.coordinates.isNextTo(instance2.coordinates)
  }

  def isAtDistance(args: Array[Any], map: WorldMap): Boolean = {
    val distanceProperty = Property("Distance", "Int", 1)
    val instance1 = map.getInstanceById(args(0).asInstanceOf[Int])
    val instance2 = map.getInstanceById(args(1).asInstanceOf[Int])
    val desiredDistance = instance1.concept
      .rules
      .find(_.property == distanceProperty)
      .getOrElse(distanceProperty.defaultValuedProperty)
      .value
      .asInstanceOf[Int]
    val distance = instance1.coordinates.getDistanceWith(instance2.coordinates)
    distance < desiredDistance
  }

}
