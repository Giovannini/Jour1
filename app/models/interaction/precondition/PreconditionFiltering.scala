package models.interaction.precondition

import controllers.Application
import models.graph.ontology.Instance
import models.graph.ontology.property.PropertyDAO


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
    instancesList.filter(_.coordinates == source.coordinates)
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
      .filter(_.coordinates != source.coordinates)
    result
  }

  def notSelf(source: Instance, instancesList: List[Instance]): List[Instance] = {
    instancesList diff List(source)
  }

  def isSelf(source: Instance, instancesList: List[Instance]): List[Instance] = {
    List(source)
  }

  def isDifferentConcept(instance: Instance, listInstances: List[Instance]) = {
    listInstances.filter(_.concept != instance.concept)
  }
}
