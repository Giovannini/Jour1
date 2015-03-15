package models.rules.precondition

import controllers.Application
import models.WorldMap
import models.graph.ontology.Instance
import models.rules.Argument
import play.api.libs.json.{JsString, JsNumber, Json}

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

  def withId(id: Long): Precondition = {
    Precondition(id, this.label, this.subConditions, this.arguments)
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
    this.id match {
      case PreconditionManager._preconditionIsNextTo =>
        HardCodedPrecondition.isNextTo(args, map)
      case PreconditionManager._preconditionIsOnSameTile =>
        HardCodedPrecondition.isOnSameTile(args, map)
      case PreconditionManager._preconditionIsAtWalkingDistance =>
        HardCodedPrecondition.isAtWalkingDistance(args, map)
      case _ =>
        this.subConditions
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
    this.id match {
      case PreconditionManager._preconditionIsNextTo =>
        InstancesThatFillPrecondition.isNextTo(source).toSet
      case PreconditionManager._preconditionIsOnSameTile =>
        InstancesThatFillPrecondition.isOnSameTile(source).toSet
      case PreconditionManager._preconditionIsAtWalkingDistance =>
        InstancesThatFillPrecondition.isAtWalkingDistance(source).toSet
      case _ => this.subConditions
        .map(precondition => precondition.instancesThatFill(source))
        .foldRight(Application.map.getInstances.toSet)(_ intersect _)
    }
  }

  def toJson = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label)
  )

  def save: Long = PreconditionDAO.save(this)
}

object Precondition {

  def identify(id: Long, label: String, subConditions: List[Long], arguments: List[Argument]): Precondition = {
    Precondition(id, label, subConditions.map(PreconditionDAO.getById), arguments)
  }

  def parse(id: Long, label: String, argumentsToParse: Array[String], subConditionsToIdentify: Array[String]): Precondition = {
    println("TODO") // TODO
    val subConditions: List[Precondition] = List()
    val arguments: List[Argument] = List()
    Precondition(id, label, subConditions, arguments)
  }

  val error = Precondition(-1L, "error", List[Precondition](), List[Argument]())
}

