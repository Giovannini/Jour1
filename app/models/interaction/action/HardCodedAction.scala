package models.interaction.action

import controllers.Application
import models.graph.concept
import models.graph.concept.{ConceptDAO}
import models.graph.property.{ValuedProperty, PropertyDAO}
import models.interaction.parameter.{ParameterReference, ParameterValue}



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
  def addInstanceAt(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceToAdd", "Long")).value.asInstanceOf[Long]
    val groundWhereToAddItId = args(ParameterReference("groundWhereToAddIt", "Long")).value.asInstanceOf[Long]
    map.addInstance(instanceId, groundWhereToAddItId)
  }

  /**
   * Create a random Instance of a concept at given coordinates
   * @author Simon RONCIERE
   * @param args arguments containing the instance to add and the coordinates where to add it
   */
  def createInstanceAt(args: Map[ParameterReference, ParameterValue]): Unit = {
    val conceptId = args(ParameterReference("conceptID", "Long")).value.toString.toLong
    val groundWhereToAddItId = args(ParameterReference("groundWhereToAddIt", "Long")).value.asInstanceOf[Long]
    val instance = models.graph.Instance.createRandomInstanceOf(ConceptDAO.getById(conceptId))
    .at(map.getInstanceById(groundWhereToAddItId).coordinates)
    map.createInstance(instance)
  }

  /**
   * Remove an instance from the map at given coordinates
   * @author Thomas GIOVANNINI
   * @param args arguments containing the instance to remove and the coordinates where to remove it
   */
  def removeInstanceAt(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceToRemove", "Long")).value.asInstanceOf[Long]
    map.removeInstance(instanceId)
  }

  def modifyProperty(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceToModify", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
    val newValue = args(ParameterReference("propertyValue", "Int")).value.toString.toDouble

    map.modifyProperty(instanceId, propertyString, newValue)
  }

  def modifyPropertyWithParam(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceToModify", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
    val propertyToUse = args(ParameterReference("propertyValue", "Property")).value.asInstanceOf[String]
    val instance = map.getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyToUse)

    val valueOfProperty: Double = instance.getValueForProperty(property)
    map.modifyProperty(instanceId, propertyString, valueOfProperty)
  }



  /**
   * Add one to a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def addToProperty(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
    val valToAdd = args(ParameterReference("valueToAdd", "Int")).value.asInstanceOf[String].toDouble


    val instance = map.getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    val valueOfProperty: Double = instance.getValueForProperty(property) + valToAdd
    val newValuedProperty = ValuedProperty(property, valueOfProperty)
    val newInstance = instance.modifyValueOfProperty(newValuedProperty)

    Application.map.updateInstance(instance, newInstance)
  }

}
