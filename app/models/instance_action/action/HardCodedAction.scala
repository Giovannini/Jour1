package models.instance_action.action

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology.property.Property
import models.graph.ontology.{Concept, Instance}

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
  def addInstanceAt(args: Array[Any]) = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val groundInstance = map.getInstanceById(args(1).asInstanceOf[Long])
    val coordinates = groundInstance.coordinates
    map.addInstance(instance.at(coordinates))
  }

  def createInstanceAt(args: Array[Any]) = {
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
  def removeInstanceAt(args: Array[Any]) = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val key = instance.concept.id
    if (map.instances.contains(key)) {
      map.instances(key) = map.instances(key) diff List(instance)
      instance
    } else Instance.error
  }

  def searchInstance(args: Array[Any]) = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).contains(instance)
  }

  def searchConcept(args: Array[Any]) = {
    val concept = Concept.getById(args(0).asInstanceOf[Long])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).map(_.concept).contains(concept)
  }

  def modifyProperty(args: Array[Any]) = {
    val instance = Application.map.getInstanceById(args(0).asInstanceOf[Long])
    val property =  Property.parseString(args(1).asInstanceOf[String])
    val value = args(2)
    val newInstance = instance.modifyProperty(property, value)
    Application.map.updateInstance(instance, newInstance)
  }

  def addOneToProperty(args: Array[Any]) = {
    def parseMaybe(value: Double, property: Property): Any = {
      if(property.valueType == "Int") value.asInstanceOf[Int]
      else value
    }
    val instance = Application.map.getInstanceById(args(0).asInstanceOf[Long])
    val property = Property.parseString(args(1).asInstanceOf[String])
    if (property.valueType == "Int" || property.valueType == "Double") {
      val _valueOfProperty = instance.getValueForProperty(property)
      val valueOfProperty = {
        if(property.valueType == "Int") _valueOfProperty.asInstanceOf[Int]
        else _valueOfProperty.asInstanceOf[Double]
      }
      val newInstance = instance.modifyProperty(property, parseMaybe(valueOfProperty + 1, property))
      Application.map.updateInstance(instance, newInstance)
    }
  }

}
