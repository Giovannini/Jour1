package models.instance_action.action

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology.Instance
import models.graph.ontology.concept.ConceptDAO
import models.graph.ontology.property.Property

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
  def addInstanceAt(args: Array[Any]): Unit = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val groundInstance = map.getInstanceById(args(1).asInstanceOf[Long])
    val coordinates = groundInstance.coordinates
    map.addInstance(instance.at(coordinates))
  }

  /**
   * Create an instance at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing id of the instance to add and id of the ground instance where to add it
   */
  def createInstanceAt(args: Array[Any]): Unit = {
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

  //NOT USED
  def searchInstance(args: Array[Any]): Unit = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).contains(instance)
  }

  //NOT USED
  def searchConcept(args: Array[Any]) = {
    val concept = ConceptDAO.getById(args(0).asInstanceOf[Long])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    val coordinates = Coordinates(xCoordinate, yCoordinate)
    map.getInstancesAt(coordinates).map(_.concept).contains(concept)
  }

  /**
   * Modify value of a property
   * @author Thomas GIOVANNINI
   * @param args array containing id of the instance to update, property to string to modify and new value
   */
  def modifyProperty(args: Array[Any]): Unit = {
    val instance = Application.map.getInstanceById(args(0).asInstanceOf[Long])
    val property =  Property.parseString(args(1).asInstanceOf[String])
    val value = args(2)
    val newInstance = instance.modifyValueOfProperty(property, value)
    Application.map.updateInstance(instance, newInstance)
  }

  /**
   * Add one to a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def addOneToProperty(args: Array[Any]): Unit = {
    /**
     * Parse a property value to Int if it should be
     * @param value to maybe parse
     * @param property from which the value may be modified
     * @return the value that may have been parsed
     */
    def parseMaybe(value: Double, property: Property): Any = {
      if(property.valueType == "Int") value.asInstanceOf[Int]
      else value
    }
    /**
     * Get value for an instance of a given property
     * @author Thomas GIOVANNINI
     * @param instance from which the property is taken
     * @param property to look for
     * @return the desired value
     */
    def getValueOfProperty(instance: Instance, property: Property): Double = {
      val _valueOfProperty = instance.getValueForProperty(property)
      if (property.valueType == "Int") _valueOfProperty.asInstanceOf[Int]
      else _valueOfProperty.asInstanceOf[Double]
    }
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val property = Property.parseString(args(1).asInstanceOf[String])

    val valueOfProperty: Double = getValueOfProperty(instance, property)
    val newInstance = instance.modifyValueOfProperty(property, parseMaybe(valueOfProperty + 1, property))
    Application.map.updateInstance(instance, newInstance)
  }

  /**
   * Remove one from a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def removeOneFromProperty(args: Array[Any]): Unit = {
    /**
     * Parse a property value to Int if it should be
     * @param value to maybe parse
     * @param property from which the value may be modified
     * @return the value that may have been parsed
     */
    def parseMaybe(value: Double, property: Property): Any = {
      if(property.valueType == "Int") value.asInstanceOf[Int]
      else value
    }
    /**
     * Get value for an instance of a given property
     * @author Thomas GIOVANNINI
     * @param instance from which the property is taken
     * @param property to look for
     * @return the desired value
     */
    def getValueOfProperty(instance: Instance, property: Property): Double = {
      val _valueOfProperty = instance.getValueForProperty(property)
      if (property.valueType == "Int") _valueOfProperty.asInstanceOf[Int]
      else _valueOfProperty.asInstanceOf[Double]
    }
    val instance = map.getInstanceById(args(0).asInstanceOf[Long])
    val property = Property.parseString(args(1).asInstanceOf[String])

    val valueOfProperty: Double = getValueOfProperty(instance, property)
    val newInstance = instance.modifyValueOfProperty(property, parseMaybe(valueOfProperty - 1, property))
    Application.map.updateInstance(instance, newInstance)
  }

}
