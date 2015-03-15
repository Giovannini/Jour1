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

  val _actionAddInstanceAt = InstanceAction(0, "addInstanceAt", List[Precondition](), List[(InstanceAction, String)](),
    List(Argument("instanceToAdd", "Int"), Argument("groundWhereToAddIt", "Int"))).save

  val _actionRemoveInstanceAt = InstanceAction(0, "removeInstanceAt",
    List[Precondition](), List[(InstanceAction, String)](), List(Argument("instanceToRemove", "Int"))).save

  /*Function to add to the BDD*/
  /**
   * NOTE: The add action must be done before the remove one because it is not possible to add a non existing instance.
   */
  val _actionMoveInstanceAt = InstanceAction.identify(0L, "ACTION_MOVE",
    List(PreconditionManager._preconditionIsAtWalkingDistance),
    List((_actionAddInstanceAt, "instanceToMove, groundWhereToMoveIt"), (_actionRemoveInstanceAt, "instanceToMove")),
    List(Argument("instanceToMove", "Int"), Argument("groundWhereToMoveIt", "Int"))).save

  val _actionEat = InstanceAction.identify(0L, "ACTION_EAT",
    List(PreconditionManager._preconditionIsOnSameTile),
    List((_actionRemoveInstanceAt, "instanceThatIsEaten")),
    List(Argument("instanceThatEat", "Int"), Argument("instanceThatIsEaten", "Int"))).save

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
      //println(args.length + " arguments for action " + action.label)
      action.id match {
        case `_actionAddInstanceAt` =>
          HardCodedAction.addInstanceAt(args, map)
          true
        case `_actionRemoveInstanceAt` =>
          HardCodedAction.removeInstanceAt(args, map)
          true
        case _ =>
          action.subActions
          .map(tuple => execute(tuple._1, takeGoodArguments(tuple._2, arguments)))
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
   * @param argumentsNameToParse from which the arguments are needed
   * @param arguments list to reduce
   * @return a reduced argument list
   */
  def takeGoodArguments(argumentsNameToParse: String, arguments: List[(Argument, Any)]): List[(Argument, Any)] = {
    val result = argumentsNameToParse.split(", ")
      .map { string =>
        arguments.find(_._1.reference == string)
          .getOrElse((Argument.error, ""))
      }.toList
    println(result.map(_._1.reference))
    result
    /*val keptArguments = arguments.filter{
      tuple => action.parameters
        .contains(tuple._1)
    }
    keptArguments*/
  }
}

