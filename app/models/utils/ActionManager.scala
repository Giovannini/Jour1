package models.utils

import models.graph.custom_types.Coordinates
import models.graph.ontology.Instance
import models.map.WorldMap

/**
 * Manage actions
 */
case class ActionManager(actions: List[Action], map: WorldMap){

  val _actionAddInstanceAt = Action("addInstanceAt", "addInstanceAt0", List(), List(),
    List(Argument("instanceId", "Int"), Argument("coordinateX", "Int"), Argument("coordinateY", "Int")))

  val _actionRemoveInstanceAt = Action("removeInstanceAt", "removeInstanceAt0", List(), List(),
    List(Argument("instanceId", "Int")))

  /*Function to add to the BDD*/
  /**
   * NOTE: The add action must be done before the remove one because it is not possible to add a non existing instance.
   */
  val _actionMoveInstanceAt = Action("moveInstanceAt", "moveInstanceAt0", List(), List(_actionAddInstanceAt, _actionRemoveInstanceAt),
    List(Argument("instanceId", "Int"), Argument("coordinateX", "Int"), Argument("coordinateY", "Int")))


  /**
   * Execute a given action with given arguments
   * @author THomas GIOVANNINI
   * @param action to execute
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(action: Action, arguments: List[(Argument, Any)]):Boolean = {
    val args = arguments.map(_._2).toArray

    action.referenceId match {
      case "addInstanceAt0" =>
        HardCodedActions.addInstanceAt(args, map)
        true
      case "removeInstanceAt0" =>
        HardCodedActions.removeInstanceAt(args, map)
        true
      case _ => action.subActions
        .map(action => execute(action, takeGoodArguments(action, arguments)))
        .foldRight(true)(_ & _)
    }
  }

  def takeGoodArguments(action: Action, arguments: List[(Argument, Any)]) = {
    println("Taking good arguments for action " + action.label + " in")
    println(arguments.mkString(", "))
    val keptArguments = arguments.filter{
      tuple => action.arguments
        .contains(tuple._1)
    }
    println("Those arguments were kept")
    println(keptArguments.mkString(", "))
    keptArguments
  }
}

