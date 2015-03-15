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
  def parseAction(actionReference: Long, instancesId: List[Long]): Boolean = {
    val action = getAction(actionReference)
    if (action == InstanceAction.error) {
      println("Action not found.")
      false
    }else{
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
  def getAction(actionReference: Long): InstanceAction = {
    val action = InstanceAction.getById(actionReference)
    action
  }

  /**
   * Get the argument list needed to execute an action
   * @param action the action to execute
   * @param ids the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
  def getArgumentsList(action: InstanceAction, ids: List[Long]): List[(Argument, Any)] = {
    def getArgumentsListRec(arguments: List[Argument], ids: List[Long]): List[(Argument, Any)] = {
      arguments match {
        case List() => List()
        case head::tail =>
          if (head._type == "Property") (head, head.reference.toLong) :: getArgumentsListRec(tail, ids)
          else if (ids.nonEmpty) (head, ids.head) :: getArgumentsListRec(tail, ids.tail)
          else List() //error
      }
    }
    getArgumentsListRec(action.parameters, ids)

    /*if (ids.length == action.parameters.length) {
      action.parameters.zip(ids)
    } else {
      action.parameters.filter(_._type == "Property")
      // TODO add needed properties ID for later use
      println("Error while getting arguments list: arguments list of different size.")
      action.parameters.zip(ids)
    }*/
  }


}
