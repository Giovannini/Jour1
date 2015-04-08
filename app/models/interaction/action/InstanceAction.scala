package models.interaction.action

import controllers.Application
import models.graph.ontology.Instance
import models.interaction.Interaction
import models.interaction.parameter.{Parameter, ParameterReference, ParameterValue}
import models.interaction.precondition.Precondition
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param preconditions preconditions for the function
 * @param _subActions content of the rule
 * @param parameters parameters for the function
 */
case class InstanceAction(
  id: Long,
  label: String,
  preconditions: List[(Precondition, Map[ParameterReference, Parameter])],
  _subActions: List[(Interaction, Map[ParameterReference, Parameter])],
  parameters: List[ParameterReference]) extends Interaction{

  val subInteractions = _subActions.map(tuple => (tuple._1.asInstanceOf[InstanceAction], tuple._2))

  def subConditions: List[(Precondition, Map[ParameterReference, Parameter])] = {
    subInteractions.flatMap(_._1.preconditions) ++ subInteractions.flatMap(_._1.subConditions )
  }

  def isError: Boolean = this == InstanceAction.error

  /**
   * Parse the instance action to a Json object
   * @author Thomas GIOVANNINI
   * @return a json object representing the instance action
   */
  override def toJson: JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "preconditions" -> preconditions.map(item => item._1.toJsonWithParameters(item._2)),
      "subActions" -> subInteractions.map(item => item._1.toJsonWithParameters(item._2)),
      "parameters" -> parameters.map(_.toJson)
    )
  }

  /**
   * Check if all the action's preconditions are filled before executing it.
   * @param arguments to use to check those preconditions
   * @return true if all the preconditions are filled
   *         false else
   */
  def checkPreconditions(arguments: Map[ParameterReference, ParameterValue]): Boolean = {
    preconditions.forall(item => item._1.isFilled(item._2, arguments))
  }

  /**
   * Get the instances that validate all the preconditions of a given action
   * @author Thomas GIOVANNINI
   * @param sourceInstance the source of the action
   * @param instances list of instances to validate
   * @return a list of instances under JSON format
   */
  def getDestinationList(sourceInstance: Instance, instances: List[Instance]): List[Instance] = {
    val result = (preconditions ++ subConditions)
      .map(_._1.instancesThatFill(sourceInstance, instances))
      .foldRight(instances.toSet)(_ intersect _)
      .toList
    result
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def executeGoodAction(arguments: Map[ParameterReference, ParameterValue]): Boolean = {
    this.label match {
      case "addInstanceAt" =>
        HardCodedAction.addInstanceAt(arguments)
        true
      case "removeInstanceAt" =>
        HardCodedAction.removeInstanceAt(arguments)
        true
      case "addToProperty" =>
        HardCodedAction.addToProperty(arguments)
        true
      case "modifyProperty" =>
        HardCodedAction.modifyProperty(arguments)
        true
      case "modifyPropertyWithParam" =>
        HardCodedAction.modifyPropertyWithParam(arguments)
        true
      case _ =>
        subInteractions.forall(subAction => subAction._1.execute(takeGoodArguments(subAction._2, arguments)))
    }
  }

  /**
   * Modify ID of the action
   * @author Thomas GIOVANNINI
   * @param newId that will be given to the action
   * @return a new InstanceAction looking like this one but with a new ID
   */
  def withId(newId: Long): InstanceAction = {
    InstanceAction(newId, this.label, this.preconditions, this.subInteractions, this.parameters)
  }

  /*#################
    DB interactions
  #################*/
  /**
   * Save the action to database
   * @author Thomas GIOVANNINI
   * @return its ID
   */
  def save: InstanceAction = {
    InstanceActionDAO.save(this)
  }

  def isValid() : Boolean = {
    _subActions.forall(item => {
      item._1.id != this.id && item._1.label != this.label
    })
  }
}

/**
 * Model for rule
 */
object InstanceAction {

  implicit val connection = Application.connection

  val error = InstanceAction(-1, "error", List(), List(), List())

  /*######################
    Parsing
  ######################*/
  /**
   * Parse an action from strings
   * @param id of the action
   * @param label of the action
   * @param parametersToParse to retrieve real parameters of the action
   * @param preconditionsToParse to retrieve real preconditions of the action
   * @param subActionsToParse to retrieve real sub-actions of the action
   * @return the corresponding action
   */
  def parse(id: Long, label: String, parametersToParse: String, preconditionsToParse: String, subActionsToParse: String)
  : InstanceAction = {
    def parseSubActions(subActionsToParse: String): List[(InstanceAction, Map[ParameterReference, Parameter])] = {
      Try {
        if (subActionsToParse.nonEmpty) {
          subActionsToParse.split(";")
            .map(s => parseSubAction(s))
            .toList
        } else {
          List()
        }
      } match {
        case Success(list) => list
        case Failure(e) =>
          println("Error while parsing sub-actions from string " + subActionsToParse)
          println(e)
          List()
      }
    }
    def parseSubAction(subActionToParse: String): (InstanceAction, Map[ParameterReference, Parameter]) = {
      val globalPattern = "(^\\d*|\\(.*\\)$)".r
      val result = globalPattern.findAllIn(subActionToParse).toArray

      // Get the precondition
      val id = result(0).toInt
      val action = InstanceActionDAO.getById(id)

      // Set the map of parameters
      val paramPattern = "([^\\(|,|\\)]+)".r
      val params = paramPattern.findAllIn(result(1)).toList.map(Parameter.parse)

      (action, Parameter.linkParameterToReference(action.parameters, params))
    }

    Try {
      InstanceAction(
        id,
        label,
        Precondition.parseSubConditions(preconditionsToParse),
        parseSubActions(subActionsToParse),
        Parameter.parseParameters(parametersToParse)
      )
    } match {
      case Success(action) => action
      case Failure(e) =>
        println("Error while parsing action " + label)
        println(e)
        InstanceAction.error
    }
  }
}


