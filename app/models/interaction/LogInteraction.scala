package models.interaction

import controllers.Application

import scala.util.{Failure, Success, Try}

/**
 * Class to log an action without executing it.
 */
case class LogInteraction(value: String){

  def execute(): Unit = {
    Try {
      //println("Executing LOG: " + this.value)
      val splitted = value.split(" ")
      splitted(0) match {
        case "ADD" =>/*###################################################*/
          val instanceId = splitted(1).toLong
          val groundId = splitted(2).toLong
          Application.map.addInstance(instanceId, groundId)
        case "REMOVE" =>/*################################################*/
          val instanceId = splitted(1).toLong
          Application.map.removeInstance(instanceId)
        /*case "CREATE" =>/*##############################################*/
          val json = Json.toJson(splitted(1))
          Application.map.createInstance(Instance.parseJson(json))*/
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
        case error =>/*###################################################*/
          throw new Exception("Error, log: " + error + " is unknown.")
      }
    } match {
      case Success(_) => //println("Done")
      case Failure(e) =>
        println("Error while parsing action log:")
        println(e.getStackTrace)
    }
  }

}

object LogInteraction {

  val nothing = LogInteraction("ERROR")

}
