package models.utils.precondition

import models.map.WorldMap
import models.utils.Argument

/**
 * Manager for the preconditions.
 */
case class PreconditionManager(map: WorldMap){

  val _preconditionIsNextTo = Precondition("isNextTo", "isNextTo0", List(),
    List(Argument("instance1ID", "Int"), Argument("instance2ID", "Int")))

  val _preconditionIsAtWalkingDistance = Precondition("isAtWalkingDistance", "isAtWalkingDistance0", List(),
    List(Argument("instance1ID", "Int"), Argument("instance2ID", "Int"), Argument("distance", "Int")))

}
