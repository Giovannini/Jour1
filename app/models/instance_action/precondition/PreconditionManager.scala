package models.instance_action.precondition

import models.instance_action.Parameter

/**
 * Manager for the preconditions.
 */
object PreconditionManager {

  /* TODO: preconditions to implement
   * See [Thomas]
   * Search "Can I see an apple from there?"
   */

  val nameToId: collection.mutable.Map[String, Long] = collection.mutable.Map.empty[String, Long]

  /*#######################
    Preconditions creation
  #######################*/
  val preconditionIsNextTo = Precondition(0, "isNextTo", List(),
    List(Parameter("instance1ID", "Long"), Parameter("instance2ID", "Long")))
  val preconditionIsOnSameTile = Precondition(0, "isOnSameTile", List[(Precondition)](),
    List(Parameter("instance1ID", "Long"), Parameter("instance2ID", "Long")))
  val preconditionIsAtWalkingDistance = Precondition(0, "isAtWalkingDistance",
    List[(Precondition)](),
    List(Parameter("instance1ID", "Long"), Parameter("instance2ID", "Long"), Parameter("distance", "Int")))
  val preconditionHasProperty = Precondition(0L, "hasProperty", List[Precondition](),
    List(Parameter("instanceID", "Long"), Parameter("property", "Property")))
  val preconditionIsANumberProperty = Precondition(0L, "isANumberProperty", List[Precondition](),
    List(Parameter("property", "Property")))

  /*Composed preconditions*/
  val preconditionPropertyIsHigherThan = {
    val p_instanceID = Parameter("instanceID", "Long")
    val p_propertyID = Parameter("property", "Property")
    val p_value = Parameter("value", "Int")
    Precondition(0L, "propertyIsHigherThan",
      List(preconditionHasProperty.withParameters(List(p_instanceID, p_propertyID)),
        preconditionIsANumberProperty.withParameters(List(p_propertyID))),
      List(p_instanceID, p_propertyID, p_value))
  }
  val preconditionPropertyIsLowerThan = {
    val p_instanceID = Parameter("instanceID", "Long")
    val p_propertyID = Parameter("property", "Property")
    val p_value = Parameter("value", "Int")
    Precondition(0L, "propertyIsLowerThan",
      List(preconditionHasProperty.withParameters(List(p_instanceID, p_propertyID)),
        preconditionIsANumberProperty.withParameters(List(p_propertyID))),
      List(p_instanceID, p_propertyID, p_value))
  }

  def initialization() = {
    PreconditionDAO.clear
    /*Basic preconditions*/
    nameToId += "isNextTo" -> preconditionIsNextTo.save
    nameToId += "isOnSameTile" -> preconditionIsOnSameTile.save
    nameToId += "isAtWalkingDistance" -> preconditionIsAtWalkingDistance.save
    nameToId += "hasProperty" -> preconditionHasProperty.save
    nameToId += "isANumberProperty" -> preconditionIsANumberProperty.save

    /*Composed preconditions*/
    nameToId += "propertyIsHigherThan" -> preconditionPropertyIsHigherThan.save
    nameToId += "propertyIsLowerThan" -> preconditionPropertyIsLowerThan.save
    println("PreconditionManager is initialized")
  }

}
