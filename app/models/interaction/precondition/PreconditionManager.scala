package models.interaction.precondition

import models.interaction.parameter.ParameterReference

/**
 * Manager for the preconditions.
 */
object PreconditionManager {

  val nameToId: collection.mutable.Map[String, Precondition] = collection.mutable.Map.empty[String, Precondition]

  def initialization() = {
    PreconditionDAO.clear
    /*Basic preconditions*/
    val preconditionIsNextTo = Precondition(
      0,
      "isNextTo",
      List(),
      List(
        ParameterReference("instance1ID", "Long"),
        ParameterReference("instance2ID", "Long")
      )
    )

    val preconditionIsOnSameTile = Precondition(
      0,
      "isOnSameTile",
      List(),
      List(
        ParameterReference("instance1ID", "Long"),
        ParameterReference("instance2ID", "Long")
      )
    )

    val preconditionIsAtWalkingDistance = Precondition(
      0,
      "isAtWalkingDistance",
      List(),
      List(
        ParameterReference("instance1ID", "Long"),
        ParameterReference("instance2ID", "Long")
      )
    )

    val preconditionHasProperty = Precondition(
      0,
      "hasProperty",
      List(),
      List(
        ParameterReference("instanceID", "Long"),
        ParameterReference("property", "Property")
      )
    )

    val preconditionHasInstanceOfconcept = Precondition(
        0,
        "hasInstanceOfConcept",
        List(),
        List(
          ParameterReference("instanceID","Long"),
          ParameterReference("ConceptID", "Long")
        )
      )

    val preconditionIsSelf = Precondition(
      0,
      "isSelf",
      List(),
      List(
        ParameterReference("instance1ID", "Long"),
        ParameterReference("instance2ID", "Long")
      )
    )

    val preconditionNotSelf = Precondition(
      0,
      "notSelf",
      List(),
      List(
        ParameterReference("instance1ID", "Long"),
        ParameterReference("instance2ID", "Long")
      )
    )

    val preconditionDifferentConcept = Precondition(
      0,
      "isDifferentConcept",
      List(),
      List(
        ParameterReference("instance1ID", "Long"),
        ParameterReference("instance2ID", "Long")
      )
    )

    nameToId += "isNextTo" -> preconditionIsNextTo.save
    nameToId += "isOnSameTile" -> preconditionIsOnSameTile.save
    nameToId += "isAtWalkingDistance" -> preconditionIsAtWalkingDistance.save
    nameToId += "hasProperty" -> preconditionHasProperty.save
    nameToId += "hasInstanceOfConcept" -> preconditionHasInstanceOfconcept.save
    nameToId += "isSelf" -> preconditionIsSelf.save
    nameToId += "notSelf" -> preconditionNotSelf.save
    nameToId += "isDifferentConcept" -> preconditionDifferentConcept.save

    /*Composed preconditions*/
    val preconditionPropertyIsHigherThan = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyID = ParameterReference("property", "Property")
      val p_propertyValueID = ParameterReference("propertyToCompare", "Property")
      Precondition(
        0,
        "propertyIsHigherThan",
        List(
          (
            nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyID
          )),
          (nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyValueID
            ))),
        List(p_instanceID, p_propertyID, p_propertyValueID)
      )
    }

    val preconditionPropertyIsLowerThan = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyID = ParameterReference("property", "Property")
      val p_propertyValueID = ParameterReference("propertyToCompare", "Property")
      Precondition(
        0,
        "propertyIsLowerThan",
        List(
          (
            nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyID
            )),
          (nameToId("hasProperty"),
            Map(
              ParameterReference("instanceID", "Long") -> p_instanceID,
              ParameterReference("property", "Property") -> p_propertyValueID
            ))),
        List(p_instanceID, p_propertyID, p_propertyValueID)
      )
    }

/*    val preconditionPropertyIsLowerThan = {
      val p_instanceID = ParameterReference("instanceID", "Long")
      val p_propertyID = ParameterReference("property", "Property")
      val p_value = ParameterReference("value", "Int")
      Precondition(
        0,
        "propertyIsLowerThan",
        List((nameToId("hasProperty"), Map(
          ParameterReference("instanceID", "Long") -> p_instanceID,
          ParameterReference("property", "Property") -> p_propertyID
        ))),
        List(p_instanceID, p_propertyID, p_value)
      )
    }*/

    nameToId += "propertyIsHigherThan" -> preconditionPropertyIsHigherThan.save
    nameToId += "propertyIsLowerThan" -> preconditionPropertyIsLowerThan.save
    println("PreconditionManager is initialized")
  }

}
