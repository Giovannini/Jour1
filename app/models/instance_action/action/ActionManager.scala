package models.instance_action.action

import controllers.Application
import models.graph.ontology.property.PropertyDAO
import models.instance_action.parameter._
import models.instance_action.precondition.{Precondition, PreconditionManager}

import scala.collection.mutable

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

  val nameToId: collection.mutable.Map[String, InstanceAction] = collection.mutable.Map.empty[String, InstanceAction]

  /**
   * Initialize the action manager by creating basic actions.
   * @author Thomas GIOVANNINI
   */
  def initialization(): Unit = {
    println("Initialization of Action Manager")
    InstanceAction.clearDB
    val _actionAddInstanceAt = {
      val p_instanceToAdd = ParameterReference("instanceToAdd", "Long")
      val groundWhereToAddIt = ParameterReference("groundWhereToAddIt", "Long")
      InstanceAction(
        0L,
        "addInstanceAt",
        List(),
        List(),
        List(p_instanceToAdd, groundWhereToAddIt)
      ).save
    }
    nameToId += "_actionAddInstanceAt " -> _actionAddInstanceAt

    val _actionRemoveInstanceAt = {
      val p_instanceToRemove = ParameterReference("instanceToRemove", "Long")
      InstanceAction(
        0L,
        "removeInstanceAt",
        List(),
        List(),
        List(p_instanceToRemove)
      ).save
    }
    nameToId += "_actionRemoveInstanceAt " -> _actionRemoveInstanceAt

    val _actionAddOneToProperty = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyName = ParameterReference("propertyName", "Property")
      InstanceAction(
        0L,
        "addOneToProperty",
        // Preconditions
        List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyName
            )
          )
        ),
        // SubActions
        List(),
        // Parameters
        List(p_instanceID, p_propertyName)
      ).save
    }
    nameToId += "_actionAddOneToProperty " -> _actionAddOneToProperty

    val _actionRemoveOneFromProperty = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyName = ParameterReference("propertyName", "Property")
      InstanceAction(
        0L,
        "removeOneFromProperty",
        // Preconditions
        List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyName
            )
          )
        ),
        // SubActions
        List(),
        // Parameters
        List(p_instanceID, p_propertyName)).save
    }
    nameToId += "_actionRemoveOneFromProperty " -> _actionRemoveOneFromProperty

    /*Function to add to the BDD*/
    val _actionMoveInstanceAt = {
      val p_instanceToMove = ParameterReference("instanceToMove", "Long")
      val p_groundWhereToMoveIt = ParameterReference("groundWhereToMoveIt", "Long")
      InstanceAction(
        0L,
        "ACTION_MOVE",
      // Preconditions
        List(
          (
            PreconditionManager.nameToId("isAtWalkingDistance"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceToMove,
              ParameterReference("instance2ID", "Long") -> p_groundWhereToMoveIt
            )
          )

        ),
      // SubActions
        List(
          (
            _actionAddInstanceAt,
            Map(
              ParameterReference("instanceToAdd", "Long") -> p_instanceToMove,
              ParameterReference("groundWhereToAddIt", "Long") -> p_groundWhereToMoveIt
            )
          ),
          (
            _actionRemoveInstanceAt,
            Map(
              ParameterReference("instanceToRemove", "Long") -> p_instanceToMove
            )
          )
        ),
      // Parameters
        List(p_instanceToMove, p_groundWhereToMoveIt)).save
    }
    nameToId += "Move" -> _actionMoveInstanceAt

    val _actionEat = {
      val p_instanceThatEat = ParameterReference("instanceThatEat", "Long")
      val p_instanceThatIsEaten = ParameterReference("instanceThatIsEaten", "Long")
      val p_propertyHunger = ParameterReference("Hunger", "Property")
      InstanceAction(
        0L,
        "ACTION_EAT",
      // Preconditions
        List(
          (
            PreconditionManager.nameToId("isOnSameTile"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatEat,
              ParameterReference("instance1ID", "Long") -> p_instanceThatIsEaten
            )
          ),
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatEat,
              ParameterReference("instance1ID", "Long") -> ParameterValue("Hunger", "Property")
            )
          )
        ),
      // SubActions
        List(
          (
            _actionRemoveInstanceAt,
            Map(
              ParameterReference("instanceToRemove", "Long") -> p_instanceThatIsEaten
            )
          ),
          (
            _actionRemoveOneFromProperty,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatEat,
              ParameterReference("propertyName", "Property") -> ParameterValue("Hunger", "Property")
            )
          )
        ),
      // Parameters
        List(
          p_instanceThatEat,
          p_instanceThatIsEaten,
          p_propertyHunger
        )
      ).save
    }
    nameToId += "Eat" -> _actionEat
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param action to execute
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(action: InstanceAction, arguments: Map[ParameterReference, ParameterValue]):Boolean = {
    val preconditionCheck = action.preconditions.forall(_._1.isFilled(arguments))
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
          .map(subAction => execute(subAction._1, takeGoodArguments(subAction._2, arguments)))
          .foldRight(true)(_ & _)
      }
    }else{
      println("Precondition not filled for action " + action.label + ".")
      false
    }
  }

  /**
   * Take the good argument list from the list of arguments of sur-action
   * @author Thomas GIOVANNINI
   * @return a reduced argument list
   */
  def takeGoodArguments(parameters: Map[ParameterReference, Parameter], arguments: Map[ParameterReference, ParameterValue]): Map[ParameterReference, ParameterValue] = {
    val res = mutable.Map[ParameterReference, ParameterValue]()

    parameters.foreach(item => {
      println()
      println("item")
      println(item._2)
      println()

      item._2 match {
        case reference if reference.isInstanceOf[ParameterReference] => {
          res.update(item._1, arguments(reference.asInstanceOf[ParameterReference]))
        }
        case value if value.isInstanceOf[ParameterValue] => res.update(item._1, value.asInstanceOf[ParameterValue])
        case _ => println("Failed to match parameter " + item._1.toString)
      }
    })

    res.toMap
  }
}

