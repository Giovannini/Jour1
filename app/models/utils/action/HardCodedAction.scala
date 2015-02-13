package models.utils.action

import models.graph.custom_types.Coordinates
import models.graph.ontology.{Concept, Instance}
import models.map.WorldMap

/**
 * Hard coded actions that user can make instances do.
 */
object HardCodedAction {

  /**
   * Add an instance to the map at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing the instance to add and the coordinates where to add it
   * @param map where to add the instance
   */
  def addInstanceAt(args: Array[Any], map: WorldMap) = {
    println("Action: addInstanceAt")
    val instance = map.getInstanceById(args(0).asInstanceOf[Int])
    val groundInstance = map.getInstanceById(args(1).asInstanceOf[Int])
    val coordinates = groundInstance.coordinates
    map.addInstance(instance.at(coordinates))
  }

  def createInstanceAt(args: Array[Any], map: WorldMap) = {
    println("Action: createInstanceAt")
    val instance = map.getInstanceById(args(0).asInstanceOf[Int])
    val groundInstance = map.getInstanceById(args(1).asInstanceOf[Int])
    val coordinates = groundInstance.coordinates
    map.addInstance(instance)
  }

  /**
   * Remove an instance from the map at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing the instance to remove and the coordinates where to remove it
   * @param map where to add the instance
   */
  def removeInstanceAt(args: Array[Any], map: WorldMap) = {
    println("Action: removeInstanceAt")
    val instance = map.getInstanceById(args(0).asInstanceOf[Int])
    val key = instance.concept.id
    if (map.instances.contains(key)) {
      map.instances(key) = map.instances(key) diff List(instance)
      instance
    } else Instance.error
  }

  def searchInstance(args: Array[Any], map: WorldMap) = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Int])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).contains(instance)
    //map.search(instance, Coordinates(xCoordinate, yCoordinate))
  }

  def searchConcept(args: Array[Any], map: WorldMap) = {
    val concept = Concept.getById(args(0).asInstanceOf[Int]).get
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).map(_.concept).contains(concept)
    //map.search(concept, Coordinates(xCoordinate, yCoordinate))
  }

}
