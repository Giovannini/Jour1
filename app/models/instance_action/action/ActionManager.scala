package models.instance_action.action

import controllers.Application
import models.instance_action.parameter._
import models.instance_action.precondition.PreconditionManager

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
    nameToId += "_actionAddInstanceAt" -> _actionAddInstanceAt

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
    nameToId += "_actionRemoveInstanceAt" -> _actionRemoveInstanceAt

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
    nameToId += "_actionAddOneToProperty" -> _actionAddOneToProperty

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
    nameToId += "_actionRemoveOneFromProperty" -> _actionRemoveOneFromProperty

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
        subActions =  List(
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
              ParameterReference("property", "Property") -> ParameterValue("Hunger", "Property")
            )
          )
        ),
      subActions = List(
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
      parameters = List(
          p_instanceThatEat,
          p_instanceThatIsEaten
        )
      ).save
    }
    nameToId += "Eat" -> _actionEat
  }
}

