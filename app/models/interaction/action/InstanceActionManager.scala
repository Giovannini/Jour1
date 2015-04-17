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
    Console.println("Initialization of Action Manager")
    InteractionDAO.clearDB()

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
    nameToInstanceAction.put("_addInstanceAt", _addInstanceAt)

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
    nameToInstanceAction.put("_actionCreateInstanceAt", _actionCreateInstanceAt)

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
    nameToInstanceAction.put("_removeInstanceAt", _removeInstanceAt)

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
              (ParameterReference("instanceID", "Long"), p_instanceID),
              (ParameterReference("property", "Property"), p_propertyName)
            )
            )
        ),
        // SubActions
        List(),
        // Parameters
        List(p_instanceID, p_propertyName, p_valueToAdd)
      ).save
    }
    nameToInstanceAction.put("_addToProperty", _addToProperty)

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
              (ParameterReference("instanceToModify", "Long"), p_instanceID),
              (ParameterReference("property", "Property"), p_propertyName)
            )
            )
        ),
        // SubActions
        List(),
        // Parameters
        List(p_instanceID, p_propertyName, p_propertyValue)
      ).save
    }
    nameToInstanceAction.put("_modifyProperty", _modifyProperty)

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
              (ParameterReference("instance1ID", "Long"), p_instanceToMove),
              (ParameterReference("instance2ID", "Long"), p_groundWhereToMoveIt)
            )
            ),
          (
            PreconditionManager.nameToId("isAtWalkingDistance"),
            Map(
              (ParameterReference("instance1ID", "Long"), p_instanceToMove),
              (ParameterReference("instance2ID", "Long"), p_groundWhereToMoveIt)
            )
            )
        ),
        _subActions = List(
          (
            _addInstanceAt,
            Map(
              (ParameterReference("instanceToAdd", "Long"), p_instanceToMove),
              (ParameterReference("groundWhereToAddIt", "Long"), p_groundWhereToMoveIt)
            )
            ),
          (
            _removeInstanceAt,
            Map(
              (ParameterReference("instanceToRemove", "Long"), p_instanceToMove)
            )
            )
        ),
        parameters = List(p_instanceToMove, p_groundWhereToMoveIt)).save
    }
    nameToInstanceAction.put("Move", _actionMoveInstanceAt)
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
              (ParameterReference("instance1ID", "Long"), p_instanceThatEat),
              (ParameterReference("instance2ID", "Long"), p_instanceThatIsEaten)
            )
            ),
          (
            PreconditionManager.nameToId("isDifferentConcept"),
            Map(
              (ParameterReference("instance1ID", "Long"), p_instanceThatEat),
              (ParameterReference("instance2ID", "Long"), p_instanceThatIsEaten)
            )
            )
        ),
        _subActions = List(
          (
            _addToProperty,
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceThatEat),
              (ParameterReference("propertyName", "Propert(y"), ParameterValue("Hunger", "Property")),
              (ParameterReference("valueToAdd", "In(t"), ParameterValue(-3, "Int"))
            )
            ),
          (
              _addToProperty,
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceThatIsEaten),
              (ParameterReference("propertyName", "Propert(y"), ParameterValue("Wound", "Property")),
              (ParameterReference("valueToAdd", "In(t"), ParameterValue(2, "Int"))
            )
            )
        ),
        parameters = List(
          p_instanceThatEat,
          p_instanceThatIsEaten
        )
      ).save
    }
    nameToInstanceAction.put("Eat", _actionEat)

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
              (ParameterReference("instance1ID", "Long"), p_instanceThatCut),
              (ParameterReference("instance2ID", "Long"), p_instanceThatIsCut)
            )
            ),
          (
            PreconditionManager.nameToId("isDifferentConcept"),
            Map(
              (ParameterReference("instance1ID", "Long"), p_instanceThatCut),
              (ParameterReference("instance2ID", "Long"), p_instanceThatIsCut)
            )
            )
        ),
        _subActions = List(
          (
            _removeInstanceAt,
            Map(
              (ParameterReference("instanceToRemove", "Long"), p_instanceThatIsCut)
            )
            )
        ),
        parameters = List(
          p_instanceThatCut,
          p_instanceThatIsCut
        )
      ).save
    }
    nameToInstanceAction.put("Cut", _actionCut)

    val _actionProcreate = {
      val p_instanceThatProcreate = ParameterReference("instanceThatProcreate", "Long")
      val p_groundWhereToProcreate = ParameterReference("groundWhereToProcreate", "Long")
      InstanceAction(
        0L,
        "ACTION_PROCREATE",
        preconditions = List(
          (PreconditionManager.nameToId("propertyIsHigherThan"),
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceThatProcreate),
              (ParameterReference("property", "Propert(y"), ParameterValue("Desire", "Property")),
              (ParameterReference("propertyToCompare", "Propert(y"), ParameterValue(20, "Int"))
            )
            ),
          (PreconditionManager.nameToId("isOnSameTile"),
            Map(
              (ParameterReference("instance1ID", "Long"), p_instanceThatProcreate),
              (ParameterReference("instance2ID", "Long"), p_groundWhereToProcreate))
            )
        ),
        _subActions = List(
          (
            _addToProperty,
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceThatProcreate),
              (ParameterReference("propertyName", "Propert(y"), ParameterValue("Desire", "Property")),
              (ParameterReference("valueToAdd", "In(t"), ParameterValue(-20, "Int"))
            )
            ),
          (
            _addInstanceAt,
            Map(
              (ParameterReference("instanceToAdd", "Long"), p_instanceThatProcreate),
              (ParameterReference("groundWhereToAddIt", "Long"), p_groundWhereToProcreate)
            )
            )
        ),
        parameters = List(
          p_instanceThatProcreate,
          p_groundWhereToProcreate
        )
      ).save
    }
    nameToInstanceAction.put("Procreate", _actionProcreate)

    val _actionSpread = {
      val p_instanceThatSpread = ParameterReference("instanceThatSpread", "Long")
      val p_groundWhereToAddIt = ParameterReference("instanceWhereToAddIt", "Long")

      InstanceAction(
        0L,
        "ACTION_SPREAD",
        preconditions = List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceThatSpread),
              (ParameterReference("property", "Propert(y"), ParameterValue("DuplicationSpeed", "Property"))
            )
        ),
        (PreconditionManager.nameToId("propertyIsLowerThan"),
          Map(
            (ParameterReference("instanceID", "Long"), p_instanceThatSpread),
            (ParameterReference("property", "Propert(y"), ParameterValue("DuplicationSpeed", "Property")),
            (ParameterReference("propertyToCompare", "Propert(y"), ParameterValue(0, "Int"))
      )
      ),
      (PreconditionManager.nameToId("propertyIsLowerThan"),
        Map(
          (ParameterReference("instanceID", "Long"), p_instanceThatSpread),
          (ParameterReference("property", "Propert(y"), ParameterValue("Wound", "Property")),
          (ParameterReference("propertyToCompare", "Propert(y"), ParameterValue(0, "Int"))
      )
      ),
      (PreconditionManager.nameToId("isNextTo"),
        Map(
          (ParameterReference("instance1ID", "Long"), p_instanceThatSpread),
          (ParameterReference("instance2ID", "Long"), p_groundWhereToAddIt)
        )
        )

      ),
      _subActions = List(
        (
          _addToProperty,
          Map(
            (ParameterReference("instanceID", "Long"), p_instanceThatSpread),
            (ParameterReference("propertyName", "Propert(y"), ParameterValue("DuplicationSpeed", "Property")),
            (ParameterReference("valueToAdd", "In(t"), ParameterValue(10, "Int"))
      )
      ),
      (
        _addInstanceAt,
        Map(
          (ParameterReference("instanceToAdd", "Long"), p_instanceThatSpread),
          (ParameterReference("groundWhereToAddIt", "Long"), p_groundWhereToAddIt)
        )
        )
      ),
      parameters = List(
        p_instanceThatSpread,
        p_groundWhereToAddIt
      )
      ).save
    }
    nameToInstanceAction.put("Spread", _actionSpread)

    val _actionRegenerate = {
      val p_instanceSelected = ParameterReference("instanceToDuplicate", "Long")
      val p_instanceToRegenate = ParameterReference("instanceToPutOn", "Long")
      InstanceAction(0L,
        "ACTION_REGENERATE",
        preconditions = List(
          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceSelected),
              (ParameterReference("property", "Propert(y"), ParameterValue("Wound", "Property"))
            )
        ), /*          (
            PreconditionManager.nameToId("hasProperty"),
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceSelected),
              ParameterReference("property", "Propert(y"), ParameterValue("WoundMax", "Property"))
            )
            ),*/
        (PreconditionManager.nameToId("isSelf"),
          Map(
            (ParameterReference("instance1ID", "Long"), p_instanceSelected),
            (ParameterReference("instance2ID", "Long"), p_instanceToRegenate)
          )
          )
      ),
      _subActions = List((
        _modifyProperty,
        Map(
          (ParameterReference("instanceToModify", "Long"), p_instanceSelected),
          (ParameterReference("propertyName", "Propert(y"), ParameterValue("Wound", "Property")),
          (ParameterReference("propertyValue", "In(t"), ParameterValue(0, "Int"))
        )
        )),
      parameters = List(
        p_instanceSelected,
        p_instanceToRegenate
      )
      ).save
    }
    nameToInstanceAction.put("Regenerate", _actionRegenerate)

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
              (ParameterReference("instance1ID", "Long"), p_instanceThatCreate),
              (ParameterReference("instance2ID", "Long"), p_instanceThatIsUse)
            )
            )
        ),
        _subActions = List(
          (_removeInstanceAt,
            Map(
              (ParameterReference("instanceToRemove", "Long"), p_instanceThatIsUse)
            )
            ),
          (_actionCreateInstanceAt,
            Map(
              (ParameterReference("conceptID", "Long"), p_conceptId),
              (ParameterReference("groundWhereToAddIt", "Long"), p_instanceThatCreate)
            ))
        ),
        parameters = List(
          p_instanceThatCreate,
          p_conceptId,
          p_instanceThatIsUse
        )
      ).save

    }
    nameToInstanceAction.put("Craft", _actionCraft)

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
              (ParameterReference("instance1ID", "Long"), p_instanceThatCreate),
              (ParameterReference("instance2ID", "Long"), p_instanceThatIsUse)
            )
            )
        ),
        _subActions = List(
          (_actionCraft,
            Map(
              (ParameterReference("instanceID", "Long"), p_instanceThatCreate),
              (ParameterReference("conceptID", "Long"), ParameterValue(Concept("Sheep").id, "Long")),
              (ParameterReference("instanceThatIsUse", "Long"), p_instanceThatIsUse)
            ))
        ),
        parameters = List(
          p_instanceThatCreate,
          p_instanceThatIsUse
        )
      ).save
    }
    nameToInstanceAction.put("CreateBow", _actionCreateBow)

  }
}

