package models.interaction.precondition

import models.interaction.parameter.ParameterReference

import scala.collection.mutable
import scala.collection.immutable

/**
 * Manager for the preconditions.
 */
object PreconditionManager {

  val nameToId: mutable.Map[String, Precondition] = mutable.Map.empty[String, Precondition]

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

    val preconditionHasInstanceOfConcept = Precondition(
      0,
      "hasInstanceOfConcept",
      List(),
      List(
        ParameterReference("instanceID", "Long"),
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

    nameToId.put("isNextTo", preconditionIsNextTo.save)
    nameToId.put("isOnSameTile", preconditionIsOnSameTile.save)
    nameToId.put("isAtWalkingDistance", preconditionIsAtWalkingDistance.save)
    nameToId.put("hasProperty", preconditionHasProperty.save)
    nameToId.put("hasInstanceOfConcept", preconditionHasInstanceOfConcept.save)
    nameToId.put("isSelf", preconditionIsSelf.save)
    nameToId.put("notSelf", preconditionNotSelf.save)
    nameToId.put("isDifferentConcept", preconditionDifferentConcept.save)

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
            immutable.Map(
              (ParameterReference("instanceID", "Long"), p_instanceID),
              (ParameterReference("property", "Property"), p_propertyID)
            )
            ),
          (
            nameToId("hasProperty"),
            immutable.Map(
              (ParameterReference("instanceID", "Long"), p_instanceID),
              (ParameterReference("property", "Property"), p_propertyValueID)
            )
            )
        ),
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
            immutable.Map(
              (ParameterReference("instanceID", "Long"), p_instanceID),
              (ParameterReference("property", "Property"), p_propertyID)
            )),
          (nameToId("hasProperty"),
            immutable.Map(
              (ParameterReference("instanceID", "Long"), p_instanceID),
              (ParameterReference("property", "Property"), p_propertyValueID)
            ))),
        List(p_instanceID, p_propertyID, p_propertyValueID)
      )
    }

    nameToId.put("propertyIsHigherThan", preconditionPropertyIsHigherThan.save)
    nameToId.put("propertyIsLowerThan", preconditionPropertyIsLowerThan.save)
    Console.println("PreconditionManager is initialized")
  }

}
