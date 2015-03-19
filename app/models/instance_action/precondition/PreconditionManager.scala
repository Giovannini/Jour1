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

  def initialization = {
    PreconditionDAO.clear
    /*Basic preconditions*/
    val _preconditionIsNextTo = Precondition(0, "isNextTo", List[(Precondition)](),
      List(Parameter("instance1ID", "Long"), Parameter("instance2ID", "Long"))).save
    nameToId += "isNextTo" -> _preconditionIsNextTo
    val _preconditionIsOnSameTile = Precondition(0, "isOnSameTile", List[(Precondition)](),
      List(Parameter("instance1ID", "Long"), Parameter("instance2ID", "Long"))).save
    nameToId += "isOnSameTile" -> _preconditionIsOnSameTile
    val _preconditionIsAtWalkingDistance = Precondition(0, "isAtWalkingDistance",
      List[(Precondition)](),
      List(Parameter("instance1ID", "Long"), Parameter("instance2ID", "Long"), Parameter("distance", "Int"))).save
    nameToId += "isAtWalkingDistance" -> _preconditionIsAtWalkingDistance
    val _preconditionHasProperty = Precondition(0L, "hasProperty", List[Precondition](),
      List(Parameter("instanceID", "Long"), Parameter("property", "Property"))).save
    nameToId += "hasProperty" -> _preconditionHasProperty
    val _preconditionIsANumberProperty = Precondition(0L, "isANumberProperty", List[Precondition](),
      List(Parameter("property", "Property"))).save
    nameToId += "isANumberProperty" -> _preconditionIsANumberProperty

    /*Composed preconditions*/
    val _preconditionPropertyIsHigherThan = {
      val p_instanceID = Parameter("instanceID", "Long")
      val p_propertyID = Parameter("property", "Property")
      val p_value = Parameter("value", "Int")
      val result = Precondition.identify(0L, "propertyIsHigherThan",
        List((_preconditionHasProperty, List(p_instanceID, p_propertyID)),
          (_preconditionIsANumberProperty, List(p_propertyID))),
        List(p_instanceID, p_propertyID, p_value)).save
      result
    }
    nameToId += "propertyIsHigherThan" -> _preconditionPropertyIsHigherThan
    val _preconditionPropertyIsLowerThan = {
      val p_instanceID = Parameter("instanceID", "Long")
      val p_propertyID = Parameter("property", "Property")
      val p_value = Parameter("value", "Int")
      val result = Precondition.identify(0L, "propertyIsLowerThan",
        List((_preconditionHasProperty, List(p_instanceID, p_propertyID)),
          (_preconditionIsANumberProperty, List(p_propertyID))),
        List(p_instanceID, p_propertyID, p_value)).save
      result
    }
    nameToId += "propertyIsLowerThan" -> _preconditionPropertyIsLowerThan
    println("PreconditionManager is initialized")
  }

}
