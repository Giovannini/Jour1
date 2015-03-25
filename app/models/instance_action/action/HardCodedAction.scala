package models.instance_action.action

import controllers.Application
import models.graph.ontology.property.PropertyDAO
import models.instance_action.parameter.{ParameterReference, ParameterValue}

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
    val newValue = args(ParameterReference("propertyValue", "Int")).value.asInstanceOf[Double]

    map.modifyProperty(instanceId, propertyString, newValue)
  }



  /**
   * Add one to a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def addOneToProperty(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]

    val instance = map.getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)

    val valueOfProperty: Double = instance.getValueForProperty(property)
    val newInstance = instance.modifyValueOfProperty(property, valueOfProperty + 1)
    Application.map.updateInstance(instance, newInstance)
  }

  /**
   * Remove one from a number property
   * @param args array containing id of the instance to update and property to string to modify
   */
  def removeOneFromProperty(args: Map[ParameterReference, ParameterValue]): Unit = {
    val instanceId = args(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
    val propertyString = args(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]

    val instance = map.getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)

    val valueOfProperty: Double = instance.getValueForProperty(property)
    val newInstance = instance.modifyValueOfProperty(property, valueOfProperty - 1)
    Application.map.updateInstance(instance, newInstance)
  }

}
