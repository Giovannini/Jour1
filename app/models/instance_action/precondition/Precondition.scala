package models.instance_action.precondition

import controllers.Application
import models.WorldMap
import models.graph.ontology.Instance
import models.instance_action.parameter.{ParameterError, ParameterValue, Parameter, ParameterReference}
import play.api.libs.json.{JsValue, JsNumber, JsString, Json}

/**
 * Model for preconditions
 */
case class Precondition(id: Long,
                        label: String,
                        subConditions: List[(Precondition, Map[ParameterReference, Parameter])],
                        parameters: List[ParameterReference]) {


  /* A precondition can't have itself as a sub-condition. */
  require(!retrieveAllSubConditions.map(_.id).contains(id))

  /*######################
    Logical composition
  ######################*/
  def or(other: Precondition)(arguments: Map[ParameterReference, ParameterValue], map: WorldMap) = {
    this.isFilled(arguments) || other.isFilled(arguments)
  }

  def and(other: Precondition)(arguments: Map[ParameterReference, ParameterValue], map: WorldMap) = {
    this.isFilled(arguments) && other.isFilled(arguments)
  }

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
  def isFilled(parameters: Map[ParameterReference, ParameterValue]): Boolean = {
    def isSubconditionFilled(subcondition: Precondition, parameters: Map[ParameterReference, Parameter]): Boolean = {
      // Replace any reference to a current parameter to its value
      val newParameters = parameters.map({
        case (ref, param) if param.isInstanceOf[ParameterReference] => (ref, parameters(param.asInstanceOf[ParameterReference]))
        case (ref, param) => (ref, param.asInstanceOf[ParameterValue])
      }).asInstanceOf[Map[ParameterReference, ParameterValue]]

      // Evaluate if the subcondition is filled or not
      subcondition.isFilled(newParameters)
    }

    this.label match {
      // First test if it's among the Hard Coded preconditions
      case "isNextTo" => HCPrecondition.isNextTo(parameters)
      case "isOnSameTile" => HCPrecondition.isOnSameTile(parameters)
      case "isAtWalkingDistance" => HCPrecondition.isAtWalkingDistance(parameters)
      case "hasProperty" => HCPrecondition.hasProperty(parameters)
      case "propertyIsHigherThan" => HCPrecondition.isHigherThan(parameters)
      case "propertyIsLowerThan" => ! HCPrecondition.isHigherThan(parameters)
      // It's a user-created precondition
      case _ =>
        this.subConditions.forall(current => isSubconditionFilled(current._1, current._2))
    }
  }

  /**
   * Get the argument list needed to execute an action
   * @param availableParameters the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
  def getArgumentsList(args: List[ParameterValue]): Map[ParameterReference, ParameterValue] = {
    parameters.zip(args).toMap
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
          .map(_._1.instancesThatFill(source))
          .foldRight(Application.map.getInstances.toSet)(_ intersect _)
    }
  }

  def toJson = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label)
  )

  def toJsonWithParameters(parameters: Map[ParameterReference, Parameter]): JsValue = Json.obj(
    "id" -> JsNumber(id),
    "label" -> JsString(label),
    "parameters" -> parameters.map(item => {
      Parameter.toJsonWithIsParam(item._2)
    })
  )

  def save: Precondition = PreconditionDAO.save(this)
}

object Precondition {

  /*
   * Parsers used to create a Precondition from DB
   */

  def parseSubCondition(subConditionToParse: String): (Precondition, Map[ParameterReference, Parameter]) = {
    val globalPattern = "(^\\d*|\\(.*\\)$)".r
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
    if(subConditionsToParse != "") {
      subConditionsToParse.split(";")
        .map(s => parseSubCondition(s))
        .toList
    } else {
      List()
    }
  }

  /**
   * Parses a precondition from BDD
   * @param id
   * @param label
   * @param parametersToParse
   * @param subConditionsToParse
   * @return
   */
  def parse(id: Long, label: String, parametersToParse: String, subConditionsToParse: String): Precondition = {
    Precondition(
      id,
      label,
      parseSubConditions(subConditionsToParse),
      Parameter.parseParameters(parametersToParse)
    )
  }

  val error = Precondition(-1L, "error", List(), List())
}

