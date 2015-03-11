package models.rules.precondition

import models.WorldMap
import models.rules.Argument

/**
 * Manager for the preconditions.
 */
case class PreconditionManager(map: WorldMap){

  val _preconditionIsNextTo = PreconditionDAO.save(Precondition(0, "isNextTo", List[Precondition](),
    List(Argument("instance1ID", "Int"), Argument("instance2ID", "Int"))))

  val _preconditionIsAtWalkingDistance = PreconditionDAO.save(Precondition(0, "isAtWalkingDistance",
    List[Precondition](),
    List(Argument("instance1ID", "Int"), Argument("instance2ID", "Int"), Argument("distance", "Int"))))

}
