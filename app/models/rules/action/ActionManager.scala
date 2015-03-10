package models.rules.action

import models.WorldMap
import models.rules.Argument
import models.rules.precondition.PreconditionManager

/**
 * Manage actions
 */
case class ActionManager(actions: List[Action], map: WorldMap, preconditionManager: PreconditionManager){

  val _actionAddInstanceAt = Action("addInstanceAt", "addInstanceAt0", List(), List(),
    List(Argument("instanceId", "Int"), Argument("groundId", "Int")))

  val _actionRemoveInstanceAt = Action("removeInstanceAt", "removeInstanceAt0",
    List(),
    List(),
    List(Argument("instanceId", "Int")))

  /*Function to add to the BDD*/
  /**
   * NOTE: The add action must be done before the remove one because it is not possible to add a non existing instance.
   */
  val _actionMoveInstanceAt = Action("moveInstanceAt", "moveInstanceAt0",
    List(preconditionManager._preconditionIsAtWalkingDistance),
    List(_actionAddInstanceAt, _actionRemoveInstanceAt),
    List(Argument("instanceId", "Int"), Argument("groundId", "Int")))

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param action to execute
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(action: Action, arguments: List[(Argument, Any)]):Boolean = {
    val preconditionCheck = action.preconditions.forall(_.isFilled(arguments, map))
    if (preconditionCheck) {
      val args = arguments.map(_._2).toArray
      action.referenceId match {
        case "addInstanceAt0" =>
          HardCodedAction.addInstanceAt(args, map)
          true
        case "removeInstanceAt0" =>
          HardCodedAction.removeInstanceAt(args, map)
          true
        case _ => action.subActions
          .map(action => execute(action, takeGoodArguments(action, arguments)))
          .foldRight(true)(_ & _)
      }
    }else{
      println("Precondition not filled.")
      false
    }
  }

  def takeGoodArguments(action: Action, arguments: List[(Argument, Any)]) = {
    val keptArguments = arguments.filter{
      tuple => action.arguments
        .contains(tuple._1)
    }
    keptArguments
  }
}

