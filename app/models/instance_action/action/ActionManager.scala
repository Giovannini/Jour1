package models.instance_action.action

import controllers.Application
import models.instance_action.Parameter
import models.instance_action.precondition.{Precondition, PreconditionManager}

/**
 * Manage actions
 */
object ActionManager{

  /* TODO: actions to implement
   * Eat do not remove the object but only a part of it [Simon]
   * Kill -> an action that is killed can't act anymore
   * Procreate: isProcreationLevelSufficient
   */

  val map = Application.map

  val nameToId: collection.mutable.Map[String, Long] = collection.mutable.Map.empty[String, Long]

  /**
   * Initialize the action manager by creating basic actions.
   * @author Thomas GIOVANNINI
   */
  def initialization(): Unit = {
    println("Initialization of Action Manager")
    InstanceAction.clearDB
    val _actionAddInstanceAt = {
      val p_instanceToAdd = Parameter("instanceToAdd", "Long")
      val groundWhereToAddIt = Parameter("groundWhereToAddIt", "Long")
      InstanceAction(0L, "addInstanceAt",
        List[Precondition](),
        List[InstanceAction](),
        List(p_instanceToAdd, groundWhereToAddIt)).save
    }
    nameToId += "_actionAddInstanceAt " -> _actionAddInstanceAt
    val _actionRemoveInstanceAt = {
      val p_instanceToRemove = Parameter("instanceToRemove", "Long")
      InstanceAction(0L, "removeInstanceAt",
        List[Precondition](),
        List[InstanceAction](),
        List(p_instanceToRemove)).save
    }
    nameToId += "_actionRemoveInstanceAt " -> _actionRemoveInstanceAt
    val _actionAddOneToProperty = {
      val p_instanceID = Parameter("instanceID", "Long")
      val p_propertyName = Parameter("propertyName", "Property")
      InstanceAction.identify(0L, "addOneToProperty",
        List((PreconditionManager.nameToId("hasProperty"), List(p_instanceID, p_propertyName)),
          (PreconditionManager.nameToId("isANumberProperty"), List(p_propertyName))),
        List[(Long, List[Parameter])](),
        List(p_instanceID, p_propertyName)).save
    }
    nameToId += "_actionAddOneToProperty " -> _actionAddOneToProperty
    val _actionRemoveOneFromProperty = {
      val p_instanceID = Parameter("instanceID", "Long")
      val p_propertyName = Parameter("propertyName", "Property")
      InstanceAction.identify(0L, "removeOneFromProperty",
        List((PreconditionManager.nameToId("hasProperty"), List(p_instanceID, p_propertyName))),
        List[(Long, List[Parameter])](),
        List(p_instanceID, p_propertyName)).save
    }
    nameToId += "_actionRemoveOneFromProperty " -> _actionRemoveOneFromProperty
    /*Function to add to the BDD*/
    val _actionMoveInstanceAt = {
      val p_instanceToMove = Parameter("instanceToMove", "Long")
      val p_groundWhereToMoveIt = Parameter("groundWhereToMoveIt", "Long")
      InstanceAction.identify(0L, "ACTION_MOVE",
        List((PreconditionManager.nameToId("isAtWalkingDistance"), List(p_instanceToMove, p_groundWhereToMoveIt))),
        List((_actionAddInstanceAt, List(p_instanceToMove, p_groundWhereToMoveIt)),
          (_actionRemoveInstanceAt, List(p_instanceToMove))),
        List(p_instanceToMove, p_groundWhereToMoveIt)).save
    }
    nameToId += "_actionMoveInstanceAt " -> _actionMoveInstanceAt
    val _actionEat = {
      val p_instanceThatEat = Parameter("instanceThatEat", "Long")
      val p_instanceThatIsEaten = Parameter("instanceThatIsEaten", "Long")
      val p_propertyHunger = Parameter("Hunger", "Property")
      InstanceAction.identify(0L, "ACTION_EAT",
        List((PreconditionManager.nameToId("isOnSameTile"), List(p_instanceThatEat, p_instanceThatIsEaten)),
          (PreconditionManager.nameToId("hasProperty"), List(p_instanceThatEat, p_propertyHunger))),
        List((_actionRemoveInstanceAt, List(p_instanceThatIsEaten)),
          (_actionRemoveOneFromProperty, List(p_instanceThatEat, p_propertyHunger))),
        List(p_instanceThatEat, p_instanceThatIsEaten, p_propertyHunger)).save
    }
    nameToId += "_actionEat " -> _actionEat
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param action to execute
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(action: InstanceAction, arguments: List[(Parameter, Any)]):Boolean = {
    val preconditionCheck = action.preconditions.forall(_.isFilled(arguments))
    if (preconditionCheck) {
      val args = arguments.map(_._2).toArray
      action.label match {
        case "addInstanceAt" =>
          HardCodedAction.addInstanceAt(args)
          true
        case "removeInstanceAt" =>
          HardCodedAction.removeInstanceAt(args)
          true
        case "addOneToProperty" =>
          HardCodedAction.addOneToProperty(args)
          true
        case "removeOneFromProperty" =>
          HardCodedAction.removeOneFromProperty(args)
          true
        case _ =>
          action.subActions
          .map(action => execute(action, takeGoodArguments(action.parameters, arguments)))
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
  def takeGoodArguments(argumentsNameToParse: List[Parameter], arguments: List[(Parameter, Any)]): List[(Parameter, Any)] = {
    val result = argumentsNameToParse
      .map { parameter =>
        arguments.find(_._1.reference == parameter.reference)
          .getOrElse((Parameter.error, ""))
      }
    result
  }
}

