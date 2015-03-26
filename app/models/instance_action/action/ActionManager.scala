package models.instance_action.action

import controllers.Application
import models.instance_action.parameter._
import models.instance_action.precondition.PreconditionManager

/**
 * Manage actions
 */
object ActionManager {

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
    //TODO thing of a way to create an instance from nothing (using json maybe)
    //nameToId += "_createInstance" -> _createInstance

    val _addInstanceAt = {
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
    nameToId += "_addInstanceAt" -> _addInstanceAt

    val _removeInstanceAt = {
      val p_instanceToRemove = ParameterReference("instanceToRemove", "Long")
      InstanceAction(
        0L,
        "removeInstanceAt",
        List(),
        List(),
        List(p_instanceToRemove)
      ).save
    }
    nameToId += "_removeInstanceAt" -> _removeInstanceAt

    val _addToProperty = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyName = ParameterReference("propertyName", "Property")
      val p_valueToAdd = ParameterReference("valueToAdd", "Int")
      InstanceAction(
        0L,
        "addToProperty",
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
        List(p_instanceID, p_propertyName, p_valueToAdd)
      ).save
    }
    nameToId += "_addToProperty" -> _addToProperty

    /*Function to add to the BDD*/
    val _actionMoveInstanceAt = {
      val p_instanceToMove = ParameterReference("instanceToMove", "Long")
      val p_groundWhereToMoveIt = ParameterReference("groundWhereToMoveIt", "Long")
      InstanceAction(
        0L,
        "ACTION_MOVE",
        preconditions = List(
          (
            PreconditionManager.nameToId("isAtWalkingDistance"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceToMove,
              ParameterReference("instance2ID", "Long") -> p_groundWhereToMoveIt
            )
            )
        ),
        subActions = List(
          (
            _addInstanceAt,
            Map(
              ParameterReference("instanceToAdd", "Long") -> p_instanceToMove,
              ParameterReference("groundWhereToAddIt", "Long") -> p_groundWhereToMoveIt
            )
            ),
          (
            _removeInstanceAt,
            Map(
              ParameterReference("instanceToRemove", "Long") -> p_instanceToMove
            )
            )
        ),
        parameters = List(p_instanceToMove, p_groundWhereToMoveIt)).save
    }
    nameToId += "Move" -> _actionMoveInstanceAt

    val _actionEat = {
      val p_instanceThatEat = ParameterReference("instanceThatEat", "Long")
      val p_instanceThatIsEaten = ParameterReference("instanceThatIsEaten", "Long")
      InstanceAction(
        0L,
        "ACTION_EAT",
        preconditions = List(
          (
            PreconditionManager.nameToId("isOnSameTile"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatEat,
              ParameterReference("instance2ID", "Long") -> p_instanceThatIsEaten
            )
            ),
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatEat,
              ParameterReference("property", "Property") -> ParameterValue("Hunger", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(1, "Int")
            )
            )
        ),
        subActions = List(
          (
            _removeInstanceAt,
            Map(
              ParameterReference("instanceToRemove", "Long") -> p_instanceThatIsEaten
            )
            ),
          (
            _addToProperty,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatEat,
              ParameterReference("propertyName", "Property") -> ParameterValue("Hunger", "Property")
            )
            )
        ),
        parameters = List(
          p_instanceThatEat,
          p_instanceThatIsEaten
        )
      ).save
    }
    nameToId += "Eat" -> _actionEat
  }
}

