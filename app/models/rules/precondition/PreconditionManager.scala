package models.rules.precondition

import models.rules.Argument

/**
 * Manager for the preconditions.
 */
object PreconditionManager {

  //val map = Application.map

  PreconditionDAO.clear

  val _preconditionIsNextTo = Precondition(0, "isNextTo", List[Precondition](),
    List(Argument("instance1ID", "Long"), Argument("instance2ID", "Long"))).save

  val _preconditionIsOnSameTile = Precondition(0, "isOnSameTile", List[Precondition](),
    List(Argument("instance1ID", "Long"), Argument("instance2ID", "Long"))).save

  val _preconditionIsAtWalkingDistance = Precondition(0, "isAtWalkingDistance",
    List[Precondition](),
    List(Argument("instance1ID", "Int"), Argument("instance2ID", "Int"), Argument("distance", "Int"))).save

  def initialization = {
    println("PreconditionManager is initialized")
  }

}
