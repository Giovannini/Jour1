package models.instance_action.action

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology.Instance
import models.graph.ontology.concept.ConceptDAO
import models.graph.ontology.property.{PropertyDAO, Property}
import models.instance_action.parameter.ParameterValue

/**
 * Hard coded actions that user can make instances do.
 */
object HardCodedAction {

  val map = Application.map
  /**
   * Add an instance to the map at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing the instance to add and the coordinates where to add it
   */
  def addInstanceAt(args: Array[ParameterValue]): Unit = {
    val instance = map.getInstanceById(args(0).value.asInstanceOf[Long])
    val groundInstance = map.getInstanceById(args(1).value.asInstanceOf[Long])
    val coordinates = groundInstance.coordinates
    map.addInstance(instance.at(coordinates))
  }

  /**
   * Create an instance at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing id of the instance to add and id of the ground instance where to add it
   */
  def createInstanceAt(args: Array[ParameterValue]): Unit = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val groundInstance = map.getInstanceById(args(1).asInstanceOf[Long])
    val coordinates = groundInstance.coordinates
    map.addInstance(instance.at(coordinates))
  }

  /**
   * Remove an instance from the map at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing the instance to remove and the coordinates where to remove it
   */
  def removeInstanceAt(args: Array[ParameterValue]) = {
    val instance = map.getInstanceById(args(0).value.asInstanceOf[Long])
    val key = instance.concept.id
    if (map.instances.contains(key)) {
      map.instances(key) = map.instances(key) diff List(instance)
      instance
    } else Instance.error
  }

  //NOT USED
  def searchInstance(args: Array[ParameterValue]): Unit = {
    val instance = map.getInstanceById(args(0).value.asInstanceOf[Long])
    val xCoordinate = args(1).value.asInstanceOf[Int]
    val yCoordinate = args(2).value.asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).contains(instance)
  }

  //NOT USED
  def searchConcept(args: Array[ParameterValue]) = {
    val concept = ConceptDAO.getById(args(0).value.asInstanceOf[Long])
    val xCoordinate = args(1).value.asInstanceOf[Int]
    val yCoordinate = args(2).value.asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).map(_.concept).contains(concept)
  }

  /**
   * Modify value of a property
   * @author Thomas GIOVANNINI
   * @param args array containing id of the instance to update, property to string to modify and new value
   */
  def modifyProperty(args: Array[ParameterValue]): Unit = {
    val instance = Application.map.getInstanceById(args(0).value.asInstanceOf[Long])
    val property = PropertyDAO.getById(args(1).value.asInstanceOf[Int])
    val value = args(2).value.asInstanceOf[Double]
    val newInstance = instance.modifyValueOfProperty(property, value)
    Application.map.updateInstance(instance, newInstance)
  }

  /**
   * Add one to a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def addOneToProperty(args: Array[ParameterValue]): Unit = {
    val instance = map.getInstanceById(args(0).value.asInstanceOf[Long])
    val property = PropertyDAO.getById(args(1).value.asInstanceOf[Int])

    val valueOfProperty: Double = instance.getValueForProperty(property)
    val newInstance = instance.modifyValueOfProperty(property, valueOfProperty + 1)
    Application.map.updateInstance(instance, newInstance)
  }

  /**
   * Remove one from a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def removeOneFromProperty(args: Array[ParameterValue]): Unit = {
    val instance = map.getInstanceById(args(0).value.asInstanceOf[Long])
    val property = PropertyDAO.getById(args(1).value.asInstanceOf[Int])

    val valueOfProperty: Double = instance.getValueForProperty(property)
    val newInstance = instance.modifyValueOfProperty(property, valueOfProperty - 1)
    Application.map.updateInstance(instance, newInstance)
  }

}
