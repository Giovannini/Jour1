package models.instance_action.action

import models.instance_action.parameter.{ParameterReference, ParameterValue}

/**
 * Parser class for actions
 */
object ActionParser {


  /**
   * Parse an action from client-side and execute it.
   * @author Thomas GIOVANNINI
   * @param actionReference the reference of the action, its id
   * @param instancesId the instances arguments of the action
   * @return true if the execution went well
   *         false else
   */
  def parseAction(actionReference: Long, instancesId: List[Long]): Boolean = {
    val action = getAction(actionReference)
    if (action == InstanceAction.error) {
      println("Action not found.")
      false
    } else {
      val arguments = getArgumentsList(
        action,
        instancesId.map(id => ParameterValue(id, "Long"))
      )

      action.execute(arguments)
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
  def parseActionForLog(actionReference: Long, instancesId: List[Long]): List[LogAction] = {
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
  def getAction(actionReference: Long): InstanceAction = {
    val action = InstanceAction.getById(actionReference)
    action
  }

  /**
   * Get the argument list needed to execute an action
   * @param action the action to execute
   * @param args arguments used by the action
   * @return a list of arguments and their values
   */
  def getArgumentsList(action: InstanceAction, args: List[ParameterValue]): Map[ParameterReference, ParameterValue] = {
    action.parameters.zip(args).toMap
  }


}
