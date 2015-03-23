package models.instance_action.precondition

import models.graph.ontology.Instance
import models.instance_action.parameter.{Parameter, ParameterReference, ParameterValue}
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

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
      parameters.map({
        case (ref, param) =>
          (ref, param match {
            case key: ParameterReference => arguments(key)
            case _ => param.asInstanceOf[ParameterValue]
          })
      })
    }
    val args = retrieveGoodArguments()

    this.label match {
      // First test if it's among the Hard Coded preconditions
      case "isNextTo" => HCPrecondition.isNextTo(args)
      case "isOnSameTile" => HCPrecondition.isOnSameTile(args)
      case "isAtWalkingDistance" => HCPrecondition.isAtWalkingDistance(args)
      case "hasProperty" => HCPrecondition.hasProperty(args)
      case "propertyIsHigherThan" => HCPrecondition.isHigherThan(args)
      case "propertyIsLowerThan" => ! HCPrecondition.isHigherThan(args)
      // It's a user-created precondition
      case _ =>
        this.subConditions.forall(current => current._1.isFilled(current._2, arguments))
    }
  }

  /*
   * Get the argument list needed to execute an action
   * @param args the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
  /*def getArgumentsList(args: List[ParameterValue]): Map[ParameterReference, ParameterValue] = {
    parameters.zip(args).toMap
  }*/

  def instancesThatFill(source: Instance, instancesList: List[Instance]): Set[Instance] = {
    this.label match {
      case "isNextTo" =>
        PreconditionFiltering.isNextTo(source, instancesList).toSet
      case "isOnSameTile" =>
        PreconditionFiltering.isOnSameTile(source, instancesList).toSet
      case "isAtWalkingDistance" =>
        PreconditionFiltering.isAtWalkingDistance(source, instancesList).toSet
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

