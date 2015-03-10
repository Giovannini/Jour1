package models.rules.precondition

import controllers.Application
import models.WorldMap
import models.graph.ontology.Instance
import models.rules.Argument

/**
 * Model for preconditions
 */

case class Precondition(id: Long, label: String, subConditions: List[Precondition], arguments: List[Argument]){

  def or(other: Precondition)(arguments: List[(Argument, Any)], map: WorldMap) = {
    this.isFilled(arguments, map) || other.isFilled(arguments, map)
  }
  
  def and(other: Precondition)(arguments: List[(Argument, Any)], map: WorldMap) = {
    this.isFilled(arguments, map) && other.isFilled(arguments, map)
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def isFilled(arguments: List[(Argument, Any)], map: WorldMap): Boolean = {
    val args = arguments.map(_._2).toArray
    //println("This is for : " + precondition.referenceId)
    this.label match {
      case "isNextTo" =>
        HardCodedPrecondition.isNextTo(args, map)
      case "isAtWalkingDistance" =>
        //println("Checking distance precondition")
        HardCodedPrecondition.isAtWalkingDistance(args, map)
      case _ => this.subConditions
        .map(precondition => precondition.isFilled(precondition.takeGoodArguments(arguments), map))
        .foldRight(true)(_ & _)
    }
  }

  /**
   * Take needed arguments for precondition to be tested
   * @param arguments that may be good to take
   * @return a list of good arguments with their values
   */
  def takeGoodArguments(arguments: List[(Argument, Any)]): List[(Argument, Any)] = {
    arguments.filter{
      tuple => this.arguments
        .contains(tuple._1)
    }
  }

  def instancesThatFill(source: Instance): Set[Instance] = {
    this.label match {
      case "isNextTo" =>
        InstancesThatFillPrecondition.isNextTo(source).toSet
      case "isAtWalkingDistance" =>
        //println("Checking distance precondition")
        InstancesThatFillPrecondition.isAtWalkingDistance(source).toSet
      case _ => this.subConditions
        .map(precondition => precondition.instancesThatFill(source))
        .foldRight(Application.map.getInstances.toSet)(_ intersect _)
    }
  }
}

object Precondition {
  def apply(id: Long, label: String, argumentsToParse: Array[String], subConditionsToIdentify: Array[String]): Precondition = {
    val subConditions: List[Precondition] = List()
    val arguments: List[Argument] = List()
    Precondition(id, label, subConditions, arguments)
  }
}
