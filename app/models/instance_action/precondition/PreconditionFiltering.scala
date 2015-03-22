package models.instance_action.precondition

import controllers.Application
import models.WorldMap
import models.graph.ontology.Instance
import models.graph.ontology.property.{Property, PropertyDAO}


object PreconditionFiltering {

  /**
   * Retrieve all instance from the world map
   * @author Thomas GIOVANNINI
   * @return all the instances from the world map
   */
  def allInstances: List[Instance] = Application.map.getInstances

  /**
   * Get all the instances that are next to the given source
   * OOO
   * OXO
   * OOO
   * @author Thomas GIOVANNINI
   * @param source the source instance
   * @return a list of instances that are next to the source instance
   */
  def isNextTo(source: Instance, instancesList: List[Instance]): List[Instance] = {
    val result = instancesList.filter(_.coordinates.isNextTo(source.coordinates))
    result
  }

  /**
   * Get all the instances that have the same coordinates a given source instance has
   * @author Thomas GIOVANNINI
   * @param source the source instance
   * @return a list of instances that have the same coordinates as the source instance
   */
  def isOnSameTile(source: Instance, instancesList: List[Instance]): List[Instance] = {
    val result = instancesList.filter(_.coordinates == source.coordinates)
    result
  }

  /**
   * Get all the instances that are at walking distance from the source
   * @author Thomas GIOVANNINI
   * @param source the source instance
   * @return a list of instances that are at walking distance from the source
   */
  def isAtWalkingDistance(source: Instance, instancesList: List[Instance]): List[Instance] = {
    /**
     * Recursively run through all the instances to get a coordinates list of all the tile the source instance can go.
     * @param source instance
     * @param remainingDistance the instance can walk
     * @return list of all the ground instances where the source instance can go (with doubles)
     */
    def getNear(source: Instance, remainingDistance: Int): List[Instance] = {
      if (remainingDistance < 1) List()
      else {
        source :: isNextTo(source, instancesList)
          .flatMap(newSource => getNear(newSource, remainingDistance - 1))
      }
    }

    val propertyWalkingDistance = PropertyDAO.getByName("WalkingDistance")
    val result = getNear(source, source.getValueForProperty(propertyWalkingDistance).asInstanceOf[Int])
      .distinct
    result
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
