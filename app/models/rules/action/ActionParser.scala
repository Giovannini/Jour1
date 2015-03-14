package models.rules.action

import models.rules.Argument

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
  def parseAction(actionReference: Long, instancesId: List[Int]): Boolean = {
    val action = getAction(actionReference)
    (action == Action.error) && {
      val arguments = getArgumentsList(action, instancesId)
      ActionManager.execute(action, arguments)
    }
  }

  /**
   * Retrieve action from the actions database and parse it to an action object
   * @author Thomas GIOVANNINI
   * @param actionReference the id of the desired action
   * @return an action object
   */
  def getAction(actionReference: Long): Action = {
    val action = Action.getById(actionReference)
    //println("Action: " + action.label + " - " + action.id)
    action
  }

  /**
   * Get the argument list needed to execute an action
   * @param action the action to execute
   * @param ids the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
  def getArgumentsList(action: Action, ids: List[Int]): List[(Argument, Any)] = {
    if (ids.length == action.parameters.length) {
      action.parameters.zip(ids)
    } else {
      println("Error while getting arguments list: arguments list of different size.")
      action.parameters.zip(ids)
    }
  }


}
