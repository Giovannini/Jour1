package models.instance_action.precondition

import controllers.Application
import models.WorldMap
import models.graph.ontology.Instance
import models.instance_action.Parameter
import play.api.libs.json.{JsNumber, JsString, Json}

/**
 * Model for preconditions
 */

case class Precondition(id: Long, label: String, subConditions: List[Precondition], parameters: List[Parameter]){
  /* A precondition can't have itself as a sub-condition. */
  require(! retrieveAllSubConditions.map(_.id).contains(id))

  /*######################
    Logical composition
  ######################*/
  def or(other: Precondition)(arguments: List[(Parameter, Any)], map: WorldMap) = {
    this.isFilled(arguments) || other.isFilled(arguments)
  }

  def and(other: Precondition)(arguments: List[(Parameter, Any)], map: WorldMap) = {
    this.isFilled(arguments) && other.isFilled(arguments)
  }

  /*######################
    Precondition modifications
  ######################*/
  def withId(id: Long): Precondition = {
    Precondition(id, this.label, this.subConditions, this.parameters)
  }

  def modifyParameter(oldParameter: Parameter, newParameter: Parameter): Precondition = {
    def replaceParameterInList(parameters: List[Parameter]): List[Parameter] = {
      parameters match {
        case List() => List()
        case head::tail =>
          if (head == oldParameter) newParameter :: tail
          else head :: replaceParameterInList(tail)
      }
    }
    if (parameters.contains(oldParameter)) {
      val newSubConditions: List[Precondition] = subConditions.map {
        subCondition => subCondition.modifyParameter(oldParameter, newParameter)
      }
      val newParameters: List[Parameter] = replaceParameterInList(parameters)
      Precondition(id, label, newSubConditions, newParameters)
    } else this
  }

  def withParameters(newParameters: List[Parameter]): Precondition = {
    Precondition(this.id, this.label, this.subConditions, newParameters)
  }


  def retrieveAllSubConditions: List[Precondition] = {
    subConditions.flatMap(_.retrieveAllSubConditions).distinct
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param parameters with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def isFilled(parameters: List[(Parameter, Any)]): Boolean = {
    val args = getArgumentsList(parameters).map(_._2).toArray
    this.label match {
      case "isNextTo" => HCPrecondition.isNextTo(args)
      case "isOnSameTile" => HCPrecondition.isOnSameTile(args)
      case "isAtWalkingDistance" => HCPrecondition.isAtWalkingDistance(args)
      case "hasProperty" => HCPrecondition.hasProperty(args)
      case "propertyIsHigherThan" => HCPrecondition.isHigherThan(args)
      case "propertyIsLowerThan" => ! HCPrecondition.isHigherThan(args)
      case _ =>
        this.subConditions
        .forall(precondition => precondition.isFilled(parameters))
    }
  }

  /**
   * Get the argument list needed to execute an action
   * @param availableParameters the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
  //TODO make it beautiful
  def getArgumentsList(availableParameters: List[(Parameter, Any)]): List[(Parameter, Any)] = {
    def getArgumentsListRec(arguments: List[Parameter], availableParameters: List[(Parameter, Any)])
    : List[(Parameter, Any)] = arguments match {
      case List() => List()
      case head::tail =>
        val goodParameter = availableParameters.find(_._1.reference == head.reference)
        if (goodParameter.isDefined){
          goodParameter.get :: getArgumentsListRec(tail, availableParameters diff List(goodParameter))
        }
        else{
          List()
        }
    }

    getArgumentsListRec(this.parameters, availableParameters)
  }

  def instancesThatFill(source: Instance): Set[Instance] = {
    this.label match {
      case "isNextTo" =>
        PreconditionFiltering.isNextTo(source).toSet
      case "isOnSameTile" =>
        PreconditionFiltering.isOnSameTile(source).toSet
      case "isAtWalkingDistance" =>
        PreconditionFiltering.isAtWalkingDistance(source).toSet
      case _ =>
        this.subConditions
          .map(precondition => precondition.instancesThatFill(source))
          .foldRight(Application.map.getInstances.toSet)(_ intersect _)
    }
  }

  def toJson = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label),
    "parameters" -> parameters.map(_.toJson)
  )

  def save: Precondition = PreconditionDAO.save(this)
}

object Precondition {

  val error = Precondition(-1L, "error", List[Precondition](), List[Parameter]())

  def parse(id: Long, label: String, parametersToParse: String, subConditionsToParse: String): Precondition = {
    val subConditions: List[Precondition] = if(subConditionsToParse != ""){
      subConditionsToParse.split(";")
        .map(s =>PreconditionDAO.getById(s.toLong))
        .toList
    }else List()
    val arguments: List[Parameter] = parametersToParse.split("/").map(Parameter.parseArgument).toList
    Precondition(id, label, subConditions, arguments)
  }
}

