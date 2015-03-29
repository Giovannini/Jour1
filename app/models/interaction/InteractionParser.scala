package models.interaction

import models.interaction.action.{InstanceActionDAO, InstanceAction}
import models.interaction.parameter.{ParameterReference, ParameterValue}

import scala.util.{Success, Failure, Try}

/**
 * Parser class for actions
 */
object InteractionParser {

  /**
   * Parse an action from client-side and execute it.
   * @author Thomas GIOVANNINI
   * @param actionReference the reference of the action, its id
   * @param instancesId the instances arguments of the action
   * @return true if the execution went well
   *         false else
   */
  def parseInteraction(actionReference: Long, instancesId: List[Long]): Boolean = {
    Try {
      val action = getAction(actionReference)
      val arguments = getArgumentsList(action, instancesId.map(id => ParameterValue(id, "Long")))
      action.execute(arguments)
    } match {
      case Success(bool) => bool
      case Failure(e) =>
        println("Error while parsing an interaction: ")
        println(e.getStackTrace)
        false
    }
  }

  /**
   * Parse an action from client-side and execute it.
   * @author Thomas GIOVANNINI
   * @param actionReference the reference of the action, its id
   * @param instancesId the instances arguments of the action
   * @return true if the execution went well
   *         false else
   */
  def parseActionForLog(actionReference: Long, instancesId: List[Long]): List[LogInteraction] = {
    val action = getAction(actionReference)
    if (action == InstanceAction.error) {
      println("Action not found.")
      List()
    } else {
      val arguments = getArgumentsList(
        action,
        instancesId.map(id => ParameterValue(id, "Long"))
      )

      action.log(arguments)
    }
  }

  /**
   * Retrieve action from the actions database and parse it to an action object
   * @author Thomas GIOVANNINI
   * @param actionReference the id of the desired action
   * @return an action object
   */
  def getAction(actionReference: Long): Interaction = {
    InstanceActionDAO.getById(actionReference)
  }

  /**
   * Get the argument list needed to execute an action
   * @param action the action to execute
   * @param args arguments used by the action
   * @return a list of arguments and their values
   */
  def getArgumentsList(action: Interaction, args: List[ParameterValue]): Map[ParameterReference, ParameterValue] = {
    action.parameters.zip(args).toMap
  }


}
