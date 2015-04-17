package models.interaction

import controllers.Application
import models.graph.Instance

import scala.util.{Failure, Success, Try}

/**
 * Class to log an action without executing it.
 */
case class LogInteraction(value: String, priority: Int){

  class UnknownLogException extends Exception

  def execute(): Instance = {
    Try {
//      Console.println("Executing LOG: " + this.value)
      val splitted = value.split(" ")
      splitted(0) match {
        case "ADD" =>/*###################################################*/
          val instanceId = splitted(1).toLong
          val groundId = splitted(2).toLong
          Application.map.addInstance(instanceId, groundId)
        case "REMOVE" =>/*################################################*/
          val instanceId = splitted(1).toLong
          Application.map.removeInstance(instanceId)
        case "MODIFY_PROPERTY" =>/*#######################################*/
          val instanceId = splitted(1).toLong
          val propertyString = splitted(2)
          val propertyValue = splitted(3).toDouble
          Application.map.modifyProperty(instanceId, propertyString, propertyValue)
        case "ADD_TO_PROPERTY" =>/*#######################################*/
          val instanceId = splitted(1).toLong
          val propertyString = splitted(2)
          val propertyValue = splitted(3).toDouble
          Application.map.addToProperty(instanceId, propertyString, propertyValue)
//TODO LOG CONSUME
        case _ =>/*###################################################*/
          Instance.error
      }
    } match {
      case Success(instance) => instance
      case Failure(e) =>
        Console.println("Error while parsing action log:")
        Console.println(e)
        Instance.error
    }
  }

  def isAddOrRemove: Boolean = {
    value.startsWith("ADD") || value.startsWith("REMOVE")
  }

}

object LogInteraction {

  val nothing = LogInteraction("ERROR", 0)

  def createModifyLog(instanceId: Long, propertyName: String, propertyValue: Double): LogInteraction = {
    LogInteraction("MODIFY_PROPERTY " + instanceId + " " + propertyName + " " + propertyValue, 1)
  }

  def createAddLog(instanceId: Long, propertyName: String, valueToAdd: Double): LogInteraction = {
    LogInteraction("ADD_TO_PROPERTY " + instanceId + " " + propertyName + " " + valueToAdd, 2)
  }

}
