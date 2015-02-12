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
    List(Argument("instanceId", "Int"), Argument("coordinateX", "Int"), Argument("coordinateY", "Int")))

  def execute(action: Action, arguments: List[(Argument, Any)]):Boolean = {
    val args = arguments.map(_._2).toArray
    action.referenceId match {
      case "addInstanceAt0" =>
        HardCodedActions.addInstanceAt(args, map)
        true
      case "removeInstanceAt0" =>
        HardCodedActions.removeInstanceAt(args, map)
        true
      case "searchInstance0" =>
        HardCodedActions.searchInstance(args, map)
        true
      case "searchConcept0" =>
        HardCodedActions.searchConcept(args, map)
        true
      case _ => { println("toto"); true }
      /*case _ => action.subActions
        .map(action => execute(action, takeGoodArguments(action, arguments)))
        .foldRight(true)(_ & _)*/
    }
  }

  def takeGoodArguments(action: Action, arguments: List[(Argument, Any)]) = {
    arguments.filter{
      tuple => action.arguments
        .contains(tuple._1)
    }
  }
}

