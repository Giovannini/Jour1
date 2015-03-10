package models.rules.precondition

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology.{Instance, Property, ValuedProperty}


object InstancesThatFillPrecondition {

  def allInstances = Application.map.getInstances

  def isNextTo(source: Instance): List[Instance] = {
    val result = allInstances.filter(_.coordinates.isNextTo(source.coordinates))
    println("result = " + result.length)
    result
  }

  def isAtWalkingDistance(source: Instance): List[Instance] = {
    println("Yo")
    val propertyWalkingDistance = Property("WalkingDistance", "Int", 3)

    def getNear(source: Instance, remainingDistance: Int, coordinatesList: List[Coordinates]): List[Instance] = {
      println("Bim: " + remainingDistance)
      if (remainingDistance < 1) List()
      else {
        source :: isNextTo(source)
          .filter(instance => ! coordinatesList.contains(instance.coordinates))
          .filter(_.concept.label == "Earth")
          .flatMap { newSource =>
          getNear(newSource, remainingDistance - 1, source.coordinates :: coordinatesList)
        }
      }
    }
    getNear(source,
      source.properties
        .find(_.property == propertyWalkingDistance)
        .getOrElse(ValuedProperty(propertyWalkingDistance, 0))
        .value
        .asInstanceOf[Int],
      List())
  }
}
