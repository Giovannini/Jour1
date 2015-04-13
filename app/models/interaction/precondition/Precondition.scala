package models.interaction.precondition

import models.graph.Instance
import models.interaction.parameter.{Parameter, ParameterReference, ParameterValue}
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import scala.util.{Failure, Success, Try}

/**
 * Model for preconditions
 */
case class Precondition(
  id: Long,
  label: String,
  subConditions: List[(Precondition, Map[ParameterReference, Parameter])],
  parameters: List[ParameterReference]) {


  /* A precondition can't have itself as a sub-condition. */
  require(!retrieveAllSubConditions.map(_.id).contains(id))

  /*######################
    Logical composition
  ######################*/
  /*def or(other: Precondition)(arguments: Map[ParameterReference, ParameterValue]) = {
    this.isFilled(arguments) || other.isFilled(arguments)
  }

  def and(other: Precondition)(arguments: Map[ParameterReference, ParameterValue]) = {
    this.isFilled(arguments) && other.isFilled(arguments)
  }*/

  /*######################
    Precondition modifications
  ######################*/
  def withId(id: Long): Precondition = {
    Precondition(id, this.label, this.subConditions, this.parameters)
  }

  def retrieveAllSubConditions: List[Precondition] = {
    subConditions.unzip._1.flatMap(_.retrieveAllSubConditions).distinct
  }

  /**
   * Tests if a Precondition is filled or not
   * Every single parameters must be a value for the test
   * @author Thomas GIOVANNINI
   * @author Julien Pradet
   * @param parameters with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def isFilled(parameters: Map[ParameterReference, Parameter], arguments: Map[ParameterReference, ParameterValue])
  : Boolean = {
    def retrieveGoodArguments() = {
      parameters.mapValues({
        case key: ParameterReference => arguments(key)
        case value: ParameterValue => value
      })
    }
    val args = retrieveGoodArguments()

    val result = this.label match {
      // First test if it's among the Hard Coded preconditions
      case "isNextTo" => HCPrecondition.isNextTo(args)
      case "isOnSameTile" => HCPrecondition.isOnSameTile(args)
      case "isAtWalkingDistance" => HCPrecondition.isAtWalkingDistance(args)
      case "hasProperty" => HCPrecondition.hasProperty(args)
      case "propertyIsHigherThan" => HCPrecondition.isHigherThan(args)
      case "propertyIsLowerThan" => !HCPrecondition.isHigherThan(args)
      case "hasInstanceOfConcept" => HCPrecondition.hasInstanceOfConcept(args)
      case "isSelf" => HCPrecondition.isSelf(args)
      case "notSelf" => HCPrecondition.notSelf(args)
      case "isDifferentConcept" => HCPrecondition.isDifferentConcept(args)
      // It's a user-created precondition
      case _ =>
        this.subConditions.forall(current => current._1.isFilled(current._2, arguments))
    }
    if (!result) {
//      println("Precondition " + this.label + " is not filled.")
    }
    result
  }

  def instancesThatFill(source: Instance, instancesList: List[Instance]): Set[Instance] = {
    this.label match {
      case "isNextTo" =>
        PreconditionFiltering.isNextTo(source, instancesList).toSet
      case "isOnSameTile" =>
        PreconditionFiltering.isOnSameTile(source, instancesList).toSet
      case "isAtWalkingDistance" =>
        PreconditionFiltering.isAtWalkingDistance(source, instancesList).toSet
      case "notSelf" =>
        PreconditionFiltering.notSelf(source, instancesList).toSet
      case "isSelf" =>
        PreconditionFiltering.isSelf(source,instancesList).toSet
      case "isDifferentConcept" =>
        PreconditionFiltering.isDifferentConcept(source, instancesList).toSet
      //case "hasInstanceOfConcept" =>PreconditionFiltering.hasInstanceOfConcept(source, instancesList)
      case _ =>
        this.subConditions
          .map(_._1.instancesThatFill(source, instancesList))
          .foldRight(instancesList.toSet)(_ intersect _)
    }
  }

  def toJson = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label),
    "parameters" -> parameters.map(_.toJson)
  )

  def toJsonWithParameters(parameters: Map[ParameterReference, Parameter]): JsValue = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label),
    "parameters" -> parameters.map(item => {
      Parameter.toJsonWithIsParam(item._1, item._2)
    })
  )

  def save: Precondition = PreconditionDAO.save(this)
}

object Precondition {

  /*
   * Parsers used to create a Precondition from DB
   */

  def parseSubCondition(subConditionToParse: String): (Precondition, Map[ParameterReference, Parameter]) = {
    val globalPattern = "(^\\w*|\\(.*\\)$)".r
    val result = globalPattern.findAllIn(subConditionToParse).toArray

    // Get the precondition
    val id = result(0).toInt
    val precondition = PreconditionDAO.getById(id)

    // Set the map of parameters
    val paramPattern = "([^\\(|,|\\)]+)".r
    val params = paramPattern.findAllIn(result(1)).toList.map(Parameter.parse)

    // Return the subCondition
    (precondition, Parameter.linkParameterToReference(precondition.parameters, params))
  }

  def parseSubConditions(subConditionsToParse: String): List[(Precondition, Map[ParameterReference, Parameter])] = {
    Try {
      if (subConditionsToParse != "") {
        subConditionsToParse.split(";")
          .map(s => parseSubCondition(s))
          .toList
      } else {
        List()
      }
    } match {
      case Success(list) => list
      case Failure(e) =>
        println("Error while parsing sub-conditions from string: " + subConditionsToParse)
        println(e.getStackTrace)
        List()
    }
  }

  /**
   * Parses a precondition from BDD
   * @author Thomas GIOVANNINI
   * @param id of the precondition
   * @param label of the precondition
   * @param parametersToParse string to parse to retrieve parameters of the precondition
   * @param subConditionsToParse string to parse to retrieve subConditions of the precondition
   * @return a Precondition object
   */
  def parse(id: Long, label: String, parametersToParse: String, subConditionsToParse: String): Precondition = {
    Try {
      Precondition(
        id,
        label,
        parseSubConditions(subConditionsToParse),
        Parameter.parseParameters(parametersToParse)
      )
    } match {
      case Success(p) => p
      case Failure(e) =>
        println("Error while parsing precondition " + label + " from strings:")
        println(e.getStackTrace)
        error
    }
  }

  val error = Precondition(-1L, "error", List(), List())
}

