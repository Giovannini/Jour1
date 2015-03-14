package models.rules.action

import controllers.Application
import models.rules.Argument
import models.rules.precondition.{Precondition, PreconditionManager}

/**
 * Manage actions
 */
object ActionManager{

  val map = Application.map

  InstanceAction.clearDB

  val _actionAddInstanceAt = InstanceAction.save(InstanceAction(0, "addInstanceAt", List[Precondition](), List[InstanceAction](),
    List(Argument("instanceId", "Int"), Argument("groundId", "Int"))))

  val _actionRemoveInstanceAt = InstanceAction.save(InstanceAction(0, "removeInstanceAt",
    List[Precondition](), List[InstanceAction](), List(Argument("instanceId", "Int"))))

  /*Function to add to the BDD*/
  /**
   * NOTE: The add action must be done before the remove one because it is not possible to add a non existing instance.
   */
  val _actionMoveInstanceAt = InstanceAction.save(InstanceAction.identify(0L, "ACTION_MOVE",
    List(PreconditionManager._preconditionIsAtWalkingDistance),
    List(_actionAddInstanceAt, _actionRemoveInstanceAt),
    List(Argument("instanceId", "Int"), Argument("groundId", "Int"))))

  def initialization = {
    println("ActionManager is initialized")
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param action to execute
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(action: InstanceAction, arguments: List[(Argument, Any)]):Boolean = {
    val preconditionCheck = action.preconditions.forall(_.isFilled(arguments, map))
    if (preconditionCheck) {
      val args = arguments.map(_._2).toArray
      action.id match {
        case `_actionAddInstanceAt` =>
          HardCodedAction.addInstanceAt(args, map)
          true
        case `_actionRemoveInstanceAt` =>
          HardCodedAction.removeInstanceAt(args, map)
          true
        case _ =>
          action.subActions
          .map(action => execute(action, takeGoodArguments(action, arguments)))
          .foldRight(true)(_ & _)
      }
    }else{
      println("Precondition not filled for action " + action.label + ".")
      false
    }
  }

  /**
   * Take the god argument list from the list of arguments of sur-action
   * @author Thomas GIOVANNINI
   * @param action from which the arguments are needed
   * @param arguments list to reduce
   * @return a reduced argument list
   */
  def takeGoodArguments(action: InstanceAction, arguments: List[(Argument, Any)]): List[(Argument, Any)] = {
    val keptArguments = arguments.filter{
      tuple => action.parameters
        .contains(tuple._1)
    }
    keptArguments
  }
}

