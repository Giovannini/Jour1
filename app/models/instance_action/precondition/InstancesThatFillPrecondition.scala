package models.instance_action.precondition

import controllers.Application
import models.WorldMap
import models.graph.custom_types.Coordinates
import models.graph.ontology.property.{Property, PropertyDAO}
import models.graph.ontology.{Instance, ValuedProperty}


object InstancesThatFillPrecondition {

  def allInstances = Application.map.getInstances

  def isNextTo(source: Instance): List[Instance] = {
    val result = allInstances.filter(_.coordinates.isNextTo(source.coordinates))
    result
  }

  def isOnSameTile(source: Instance): List[Instance] = {
    val result = allInstances.filter(_.coordinates == source.coordinates)
    result
  }

  def isAtWalkingDistance(source: Instance): List[Instance] = {
    val propertyWalkingDistance = PropertyDAO.getByName("WalkingDistance")

    def getNear(source: Instance, remainingDistance: Int, coordinatesList: List[Coordinates]): List[Instance] = {
      if (remainingDistance < 1) List()
      else {
        source :: isNextTo(source)
          .filter(instance => instance.concept.label == "Earth" && ! coordinatesList.contains(instance.coordinates))
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

  /**
   * Precondition to check whether an instance has a property or not.
   * @param args array containing the instance id and the property name
   * @param map of the world
   * @return true if the instance has the desired property
   *         false else
   */
  def hasProperty(args: Array[Any], map: WorldMap): Boolean = {
    val sourceInstance = map.getInstanceById(args(0).asInstanceOf[Long])
    val property = Property.parseString(args(1).asInstanceOf[String])

    sourceInstance.concept
      .properties
      .contains(property)
  }
}
