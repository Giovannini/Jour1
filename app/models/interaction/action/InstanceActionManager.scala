package models.interaction.action

import controllers.Application
import models.interaction.InteractionDAO
import models.interaction.parameter._
import models.interaction.precondition.PreconditionManager

/**
 * Manage actions
 */
object InstanceActionManager {

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
    InteractionDAO.clearDB()
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

    val _modifyProperty = {
      val p_instanceID = ParameterReference("instanceToModify", "Long")
      val p_propertyName = ParameterReference("propertyName", "Property")
      val p_propertyValue = ParameterReference("propertyValue", "Int")
      InstanceAction(
        0L,
        "modifyProperty",
        // Preconditions
        List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceToModify", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyName
            )
          )
        ),
        // SubActions
        List(),
        // Parameters
        List(p_instanceID, p_propertyName, p_propertyValue)
      ).save
    }
    nameToId += "_modifyProperty" -> _modifyProperty

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
        _subActions = List(
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
//Todo choisir de valeurs judicieuse de consommation et d'envie
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
            )//No need to check properties existence, that will be done by actions addToProperty
        ),
        _subActions = List(
          (
            _addToProperty,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatEat,
              ParameterReference("propertyName", "Property") -> ParameterValue("Hunger", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(-1, "Int")
            )
            ),
          (
            _addToProperty,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatIsEaten,
              ParameterReference("propertyName", "Property") -> ParameterValue("Feed", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(-2, "Int")
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

    val _actionProcreate = {
      val p_instanceThatProcreate = ParameterReference("instanceThatProcreate", "Long")
      val p_groundWhereToProcreate = ParameterReference("groundWhereToProcreate", "Long")
      InstanceAction(
        0L,
        "ACTION_PROCREATE",
        preconditions = List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatProcreate,
              ParameterReference("property", "Property") -> ParameterValue("Desire", "Property")
            )
            ),
          (PreconditionManager.nameToId("propertyIsHigherThan"),
          Map(
            ParameterReference("instanceID", "Long") -> p_instanceThatProcreate,
            ParameterReference("property", "Property") -> ParameterValue("Desire", "Property"),
            ParameterReference("value", "Int") -> ParameterValue(10, "Int")
          )),
            (PreconditionManager.nameToId("isOnSameTile"),
              Map(
                ParameterReference("instance1ID", "Long") -> p_instanceThatProcreate,
                ParameterReference("instance2ID", "Long") -> p_groundWhereToProcreate              )
        )
        ),
        _subActions = List(
          (
            _modifyProperty,
            Map(
              ParameterReference("instanceToModify", "Long") -> p_instanceThatProcreate,
              ParameterReference("propertyName", "Property") -> ParameterValue("Desire", "Property"),
              ParameterReference("propertyValue", "Int") -> ParameterValue(0, "Int")
            )
            ),
          (
            _addInstanceAt,
            Map(
              ParameterReference("instanceToAdd", "Long") -> p_instanceThatProcreate,
              ParameterReference("groundWhereToAddIt", "Long") -> p_groundWhereToProcreate
            )
            )
        ),
        parameters = List(
          p_instanceThatProcreate,
          p_groundWhereToProcreate
        )
      ).save
    }
    nameToId += "Procreate" -> _actionProcreate


    val _actionSpread = {
      val p_instanceThatSpread = ParameterReference("instanceThatProcreate", "Long")
      val p_groundWhereToAddIt = ParameterReference("instanceWhereToAddIt", "Long")

      InstanceAction(
        0L,
        "ACTION_SPREAD",
        preconditions = List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatSpread,
              ParameterReference("property", "Property") -> ParameterValue("DuplicationSpeed", "Property")
            )
            ),
          (PreconditionManager.nameToId("propertyIsHigherThan"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatSpread,
              ParameterReference("property", "Property") -> ParameterValue("DuplicationSpeed", "Property"),
              ParameterReference("value", "Int") -> ParameterValue(5, "Int")
            )
            ),
          (PreconditionManager.nameToId("isNextTo"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatSpread,
              ParameterReference("instance2ID", "Long") -> p_groundWhereToAddIt
            )
            )

        ),
        _subActions = List(
          (
            _modifyProperty,
            Map(
              ParameterReference("instanceToModify", "Long") -> p_instanceThatSpread,
              ParameterReference("propertyName", "Property") -> ParameterValue("DuplicationSpeed", "Property"),
              ParameterReference("propertyValue", "Int") -> ParameterValue(0, "Int")
            )
            ),
          (
            _addInstanceAt,
            Map(
              ParameterReference("instanceToAdd", "Long") -> p_instanceThatSpread,
              ParameterReference("groundWhereToAddIt", "Long") -> p_groundWhereToAddIt
            )
            )
        ),
        parameters = List(
          p_instanceThatSpread,
          p_groundWhereToAddIt
        )
      ).save
    }
    nameToId += "Spreed" -> _actionSpread

/*    val _actionRegenerate = {
      val p_instanceSelected = ParameterReference("instanceToDuplicate", "Long")
      val p_instanceToRegenate = ParameterReference("instanceToPutOn", "Long")
      InstanceAction(0L,
        "ACTION_REGENERATE",
        preconditions = List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceSelected,
              ParameterReference("property", "Property") -> ParameterValue("Feed", "Property")
            )
            ),          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceSelected,
              ParameterReference("property", "Property") -> ParameterValue("FeedMax", "Property")
            )
            ),
          (PreconditionManager.nameToId("isOnSameTile"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceSelected,
              ParameterReference("instance2ID", "Long") -> p_instanceToRegenate
            )
            )
        ),
        _subActions = List((
          _modifyProperty,
          Map(
            ParameterReference("instanceToModify", "Long") -> p_instanceSelected,
            ParameterReference("propertyName", "Property") -> ParameterValue("Feed", "Property"),
            ParameterReference("propertyValue", "Int") -> ParameterValue("FeedMax", "Property")
          )
          )),
        parameters = List(
          p_instanceSelected,
          p_instanceToRegenate
        )
      ).save
    }*/
  }
}

