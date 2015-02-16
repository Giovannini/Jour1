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

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param precondition to test
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def isFilled(precondition: Precondition, arguments: List[(Argument, Any)]): Boolean = {
    val args = arguments.map(_._2).toArray
    println("This is for : " + precondition.referenceId)
    precondition.referenceId match {
      case "isNextTo0" =>
        HardCodedPrecondition.isNextTo(args, map)
      case "isAtWalkingDistance0" =>
        println("Checink distance precondition")
        HardCodedPrecondition.isAtWalkingDistance(args, map)
      case _ => precondition.subPreconditions
        .map(precondition => isFilled(precondition, takeGoodArguments(precondition, arguments)))
        .foldRight(true)(_ & _)
    }
  }

  def takeGoodArguments(precondition: Precondition, arguments: List[(Argument, Any)]) = {
    println("Taking good arguments for precondition " + precondition.label + " in")
    println(arguments.mkString(", "))
    val keptArguments = arguments.filter{
      tuple => precondition.arguments
        .contains(tuple._1)
    }
    println("Those arguments were kept")
    println(keptArguments.mkString(", "))
    keptArguments
  }
}
