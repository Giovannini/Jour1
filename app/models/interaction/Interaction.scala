package models.interaction

import models.graph.ontology.Instance
import models.interaction.action.HardCodedAction
import models.interaction.parameter.{Parameter, ParameterReference, ParameterValue}
import models.interaction.precondition.Precondition
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import scala.language.postfixOps

/**
 * Model of interaction
 * @author Thomas GIOVANNINI
 */
trait Interaction {

  val id: Long
  val label: String
  val preconditions: List[(Precondition, Map[ParameterReference, Parameter])]
  val subActions: List[(Interaction, Map[ParameterReference, Parameter])]
  val parameters: List[ParameterReference]

  def subConditions: List[(Precondition, Map[ParameterReference, Parameter])] = {
    subActions.flatMap(_._1.preconditions) ++ subActions.flatMap(_._1.subConditions )
  }

  /*#################
    Json parsing
  #################*/
  /**
   * Parse the instance action to a Json object
   * @author Thomas GIOVANNINI
   * @return a json object representing the instance action
   */
  def toJson: JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "preconditions" -> preconditions.map(item => item._1.toJsonWithParameters(item._2)),
      "subActions" -> subActions.map(item => item._1.toJsonWithParameters(item._2)),
      "parameters" -> parameters.map(_.toJson)
    )
  }

  def toJsonWithParameters(referenceToParameter: Map[ParameterReference, Parameter]): JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "parameters" -> referenceToParameter.map(item => {
        Parameter.toJsonWithIsParam(item._1, item._2)
      })
    )
  }


  /*#################
    Executions
  #################*/
  /**
   * Get the instances that validate all the preconditions of a given action
   * @author Thomas GIOVANNINI
   * @param sourceInstance the source of the action
   * @param instances list of instances to validate
   * @return a list of instances under JSON format
   */
  def getDestinationList(sourceInstance: Instance, instances: List[Instance]): List[Instance] = {
    //TODO problem: only checking preconditions of the action but none of the subactions
    println(sourceInstance.id + " destinations: " + (preconditions ++ subConditions).map(_._1.label).mkString(", "))
    val result = (preconditions ++ subConditions)
      .map(_._1.instancesThatFill(sourceInstance, instances))
      .foldRight(instances.toSet)(_ intersect _)
      .toList
    result
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
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(arguments: Map[ParameterReference, ParameterValue]): Boolean = {
    val preconditionCheck = checkPreconditions(arguments)

    if (preconditionCheck) {
      this.label match {
        /*case "createInstance" =>
          HardCodedAction.createInstance(arguments)
          true*/
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
        case _ =>
          subActions.forall(subAction => subAction._1.execute(takeGoodArguments(subAction._2, arguments)))
      }
    } else {
      println("Precondition not filled for action " + this.label + ".")
      false
    }
  }

  /**
   * LOG a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def log(arguments: Map[ParameterReference, ParameterValue]): List[LogInteraction] = {
    val preconditionCheck = checkPreconditions(arguments)

    if (preconditionCheck) {
      this.label match {
        case "addInstanceAt" =>
          val instanceId = arguments(ParameterReference("instanceToAdd", "Long")).value.asInstanceOf[Long]
          val groundId = arguments(ParameterReference("groundWhereToAddIt", "Long")).value.asInstanceOf[Long]
          List(LogInteraction("ADD " + instanceId + " " + groundId))
        case "removeInstanceAt" =>
          val instanceId = arguments(ParameterReference("instanceToRemove", "Long")).value.asInstanceOf[Long]
          List(LogInteraction("REMOVE " + instanceId))
        case "addToProperty" =>
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
          val valueToAdd = arguments(ParameterReference("valueToAdd", "Int")).value.asInstanceOf[String]
          List(LogInteraction("ADD_TO_PROPERTY " + instanceId + " " + propertyString + " " + valueToAdd))
        case "modifyProperty" =>
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
          val newValue = arguments(ParameterReference("propertyValue", "Int")).value.asInstanceOf[String]
          List(LogInteraction("MODIFY_PROPERTY " + instanceId + " " + propertyString + " " + newValue))
        case _ =>
          subActions.flatMap(subAction => subAction._1.log(takeGoodArguments(subAction._2, arguments)))
      }
    } else {
      println("Precondition not filled for action " + this.label + ".")
      List(LogInteraction.nothing)
    }
  }

  /**
   * Take the good argument list from the list of arguments of sur-action
   * @author Thomas GIOVANNINI
   * @return a reduced argument list
   */
  def takeGoodArguments(parameters: Map[ParameterReference, Parameter], arguments: Map[ParameterReference, ParameterValue]): Map[ParameterReference, ParameterValue] = {
    parameters.mapValues {
      case reference: ParameterReference => arguments(reference.asInstanceOf[ParameterReference])
      case value: ParameterValue => value.asInstanceOf[ParameterValue]
      case e: Parameter =>
        println("Failed to match parameter " + e)
        ParameterValue.error
    }.filter(_._2 != ParameterValue.error)
  }
}

