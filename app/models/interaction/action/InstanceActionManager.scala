package models.interaction.action

import controllers.Application
import models.graph.concept.Concept
import models.interaction.InteractionDAO
import models.interaction.parameter._
import models.interaction.precondition.PreconditionManager

/**
 * Manage actions
 */
object InstanceActionManager {

  val map = Application.map

  val nameToInstanceAction: collection.mutable.Map[String, InstanceAction] = collection.mutable.Map.empty[String, InstanceAction]

  /**
   * Initialize the action manager by creating basic actions.
   * @author Thomas GIOVANNINI
   */
  def initialization(): Unit = {
    println("Initialization of Action Manager")
    InteractionDAO.clearDB()
    //TODO think of a way to create an instance from nothing (using json maybe)
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
    nameToInstanceAction += "_addInstanceAt" -> _addInstanceAt

    val _actionCreateInstanceAt = {
      val p_conceptToInstanciate = ParameterReference("conceptID", "Long")
      val groundWhereToAddIt = ParameterReference("groundWhereToAddIt", "Long")
      InstanceAction(
        0L,
        "createInstanceAt",
        List(),
        List(),
        List(p_conceptToInstanciate, groundWhereToAddIt)
      ).save
    }
    nameToInstanceAction += "_actionCreateInstanceAt" -> _actionCreateInstanceAt

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
    nameToInstanceAction += "_removeInstanceAt" -> _removeInstanceAt

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
    nameToInstanceAction += "_addToProperty" -> _addToProperty

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
    nameToInstanceAction += "_modifyProperty" -> _modifyProperty

    val _consume = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyName = ParameterReference("propertyName", "Property")
      val p_propertyValue = ParameterReference("propertyValue", "Property")
      val p_valueToAdd = ParameterReference("valueToAdd", "Int")
      InstanceAction(
        0L,
        "consume",
        // Preconditions
        List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyName
            )
            ),
          (PreconditionManager.nameToId("hasProperty"),
          Map(
            ParameterReference("instanceID", "Long") -> p_instanceID,
            ParameterReference("property", "Property") -> p_propertyValue
            )
          ),(PreconditionManager.nameToId("propertyIsLowerThan"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyName,
              ParameterReference("propertyToCompare", "Property") -> p_propertyValue
            ))

        ),
        // SubActions
        List(
          (
            _addToProperty,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("propertyName", "Property") -> p_propertyName,
              ParameterReference("valueToAdd", "Int") -> p_valueToAdd
            )
            )

        ),
        // Parameters
        List(p_instanceID, p_propertyName, p_propertyValue, p_valueToAdd)
      ).save
    }
    nameToInstanceAction += "_consume" -> _consume

    /*Function to add to the BDD*/
    val _actionMoveInstanceAt = {
      val p_instanceToMove = ParameterReference("instanceToMove", "Long")
      val p_groundWhereToMoveIt = ParameterReference("groundWhereToMoveIt", "Long")
      InstanceAction(
        0L,
        "ACTION_MOVE",
        preconditions = List(
          (
            PreconditionManager.nameToId("notSelf"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceToMove,
              ParameterReference("instance2ID", "Long") -> p_groundWhereToMoveIt
            )
          ),
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
    nameToInstanceAction += "Move" -> _actionMoveInstanceAt
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
            ),
          (
            PreconditionManager.nameToId("isDifferentConcept"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatEat,
              ParameterReference("instance2ID", "Long") -> p_instanceThatIsEaten
            )
          )
        ),
        _subActions = List(
          (
            _addToProperty,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatEat,
              ParameterReference("propertyName", "Property") -> ParameterValue("Hunger", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(-3, "Int")
            )
            ),
          (
            _consume,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatIsEaten,
              ParameterReference("propertyName", "Property") -> ParameterValue("Wound", "Property"),
              ParameterReference("propertyValue", "Property") -> ParameterValue("WoundMax", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(2, "Int")
            )
            )
        ),
        parameters = List(
          p_instanceThatEat,
          p_instanceThatIsEaten
        )
      ).save
    }
    nameToInstanceAction += "Eat" -> _actionEat

    val _actionCut = {
      val p_instanceThatCut = ParameterReference("instanceThatCut", "Long")
      val p_instanceThatIsCut = ParameterReference("instanceThatIsCut", "Long")
      InstanceAction(
        0L,
        "ACTION_CUT",
        preconditions = List(
          (
            PreconditionManager.nameToId("isOnSameTile"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatCut,
              ParameterReference("instance2ID", "Long") -> p_instanceThatIsCut
            )
            ),
          (
            PreconditionManager.nameToId("isDifferentConcept"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatCut,
              ParameterReference("instance2ID", "Long") -> p_instanceThatIsCut
            )
            )
        ),
        _subActions = List(
          (
            _removeInstanceAt,
            Map(
              ParameterReference("instanceToRemove", "Long") -> p_instanceThatIsCut
            )
            )
        ),
        parameters = List(
          p_instanceThatCut,
          p_instanceThatIsCut
        )
      ).save
    }
    nameToInstanceAction += "Cut" -> _actionCut

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
              ParameterReference("propertyToCompare", "Property") -> ParameterValue("DesireMax", "Property")
            )),
            (PreconditionManager.nameToId("isOnSameTile"),
              Map(
                ParameterReference("instance1ID", "Long") -> p_instanceThatProcreate,
                ParameterReference("instance2ID", "Long") -> p_groundWhereToProcreate              )
        )
        ),
        _subActions = List(
          (
            _addToProperty,
            Map(
              ParameterReference("instanceToModify", "Long") -> p_instanceThatProcreate,
              ParameterReference("propertyName", "Property") -> ParameterValue("Desire", "Property"),
              ParameterReference("propertyValue", "Int") -> ParameterValue(-10, "Int")
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
    nameToInstanceAction += "Procreate" -> _actionProcreate

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
              ParameterReference("propertyToCompare", "Property") -> ParameterValue("DuplicaSpeedVal", "Property")
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
              ParameterReference("instanceID", "Long") -> p_instanceThatSpread,
              ParameterReference("propertyName", "Property") -> ParameterValue("DuplicationSpeed", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(-5, "Int")
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
    nameToInstanceAction += "Spreed" -> _actionSpread

    val _actionRegenerate = {
      val p_instanceSelected = ParameterReference("instanceToDuplicate", "Long")
      val p_instanceToRegenate = ParameterReference("instanceToPutOn", "Long")
      InstanceAction(0L,
        "ACTION_REGENERATE",
        preconditions = List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceSelected,
              ParameterReference("property", "Property") -> ParameterValue("Wound", "Property")
            )
            ),          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceSelected,
              ParameterReference("property", "Property") -> ParameterValue("WoundMax", "Property")
            )
            ),
          (PreconditionManager.nameToId("isSelf"),
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
            ParameterReference("propertyName", "Property") -> ParameterValue("Wound", "Property"),
            ParameterReference("propertyValue", "Int") -> ParameterValue(0, "Int")
          )
          )),
        parameters = List(
          p_instanceSelected,
          p_instanceToRegenate
        )
      ).save
    }
    nameToInstanceAction += "Regenerate" -> _actionRegenerate

    val _actionCraft = {
      val p_instanceThatCreate = ParameterReference("instanceID", "Long")
      val p_conceptId = ParameterReference("conceptID", "Long")
      val p_instanceThatIsUse = ParameterReference("instanceThatIsUse", "Long")

      InstanceAction(
        0L,
        "ACTION_CRAFT",
        preconditions = List(
          (
            PreconditionManager.nameToId("isOnSameTile"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatCreate,
              ParameterReference("instance2ID", "Long") -> p_instanceThatIsUse
            )
            )
        ),
        _subActions = List(
          (_removeInstanceAt,
            Map(
              ParameterReference("instanceToRemove", "Long") -> p_instanceThatIsUse
            )
            ),
          (_actionCreateInstanceAt,
            Map(
              ParameterReference("conceptID", "Long") -> p_conceptId,
              ParameterReference("groundWhereToAddIt", "Long") -> p_instanceThatCreate
            ))
        ),
        parameters = List(
          p_instanceThatCreate,
          p_conceptId,
          p_instanceThatIsUse
        )
      ).save

    }
    nameToInstanceAction += "Craft" -> _actionCraft

    val _actionCreateBow = {
      val p_instanceThatCreate = ParameterReference("instanceThatCreate", "Long")
      val p_instanceThatIsUse = ParameterReference("instanceThatIsUse", "Long")
      InstanceAction(
        0L,
        "ACTION_CREATE_BOW",
        preconditions = List(
          (
            PreconditionManager.nameToId("isOnSameTile"),
            Map(
              ParameterReference("instance1ID", "Long") -> p_instanceThatCreate,
              ParameterReference("instance2ID", "Long") -> p_instanceThatIsUse
            )
            )
        ),
        _subActions = List(
          (_actionCraft,
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceThatCreate,
              ParameterReference("conceptID", "Long") -> ParameterValue(Concept("Sheep").id,"Long"),
              ParameterReference("instanceThatIsUse", "Long") ->p_instanceThatIsUse
            ))
        ),
        parameters = List(
          p_instanceThatCreate,
          p_instanceThatIsUse
        )
      ).save
    }
    nameToInstanceAction += "CreateBow" -> _actionCreateBow

  }
}

