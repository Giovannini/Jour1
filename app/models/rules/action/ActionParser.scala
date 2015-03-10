package models.rules.action

import models.rules.Argument

/**
 * Parser class for actions
 */
case class ActionParser(actionManager: ActionManager) {

  /**
   * Parse an action from client-side and execute it.
   * @author Thomas GIOVANNINI
   * @param actionReference the reference of the action, its id
   * @param instancesId the instances arguments of the action
   * @return true if the execution went well
   *         false else
   */
  def parseAction(actionReference: String, instancesId: List[Int]): Boolean = {
    //println("Action Reference :" + actionReference)
    //println("Instances ids: " + instancesId.mkString(", "))
    val action = getAction(actionReference)
    val arguments = getArgumentsList(action, instancesId)
    actionManager.execute(action, arguments)
  }

  /**
   * Retrieve action from the actions database and parse it to an action object
   * @author Thomas GIOVANNINI
   * @param actionReference the id of the desired action
   * @return an action object
   */
  def getAction(actionReference: String): Action = {
    actionReference match {
      case "REMOVE" => actionManager._actionRemoveInstanceAt
      case "ADD"  => actionManager._actionAddInstanceAt
      case "ACTION_MOVE" => actionManager._actionMoveInstanceAt
      case _ =>
        //TODO take action from "rules" BDD instead of two next lines
        println(actionReference + "not found: REMOVE instead")
        actionManager._actionRemoveInstanceAt
    }
  }

  /**
   * Get the argument list needed to execute an action
   * @param action the action to execute
   * @param ids the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
  def getArgumentsList(action: Action, ids: List[Int]):List[(Argument, Any)] = {
    if(ids.length == action.parameters.length){
      action.parameters.zip(ids)
    }else{
      println("Error while getting arguments list: arguments list of different size.")
      action.parameters.zip(ids)
    }
  }



}
