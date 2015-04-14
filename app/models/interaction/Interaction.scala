package models.interaction

import models.interaction.InteractionType.InteractionType
import models.interaction.action.InstanceAction
import models.interaction.effect.Effect
import models.interaction.parameter.{Parameter, ParameterReference, ParameterValue}
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import scala.language.postfixOps

/**
 * Model of interaction
 * @author Thomas GIOVANNINI
 */
trait Interaction {

  val id: Long
  val label: String
  val subInteractions: List[(Interaction, Map[ParameterReference, Parameter])]
  val parameters: List[ParameterReference]

  def isValid: Boolean = {
    subInteractions.forall(item => {
      item._1.id != this.id && item._1.label != this.label
    })
  }

  def isValid(_type: InteractionType): Boolean = {
    this.isValid && {
      _type match {
        case InteractionType.Action =>
          (this.parameters.length == 2
            && this.parameters.forall(p => p._type == "Long")
            && this.label.startsWith("ACTION_"))
        case InteractionType.Effect =>
          (this.parameters.length == 1
            && this.parameters.head._type == "Long"
            && this.label.startsWith("EFFECT_"))
        case InteractionType.Mood =>
          (this.parameters.length == 2
            && this.parameters.forall(p => p._type == "Long")
            && this.label.startsWith("MOOD_"))
        case _ =>
          val types = Set("ACTION_", "EFFECT_", "MOOD_")
          types.forall(string => !this.label.startsWith(string))
      }
    }
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
      "subActions" -> subInteractions.map(item => item._1.toJsonWithParameters(item._2)),
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
    Other parsing
  #################*/
  def toAction: InstanceAction = {
    InstanceAction(id, label, List(), subInteractions, parameters)
  }

  def toEffect: Effect = {
    val subEffects = subInteractions.map{ tuple =>
      (tuple._1.toEffect, tuple._2)
    }
    Effect(id, label, subEffects, parameters)
  }

  /*#################
    Executions
  #################*/
  /**
   * Check if all the action's preconditions are filled before executing it.
   * @param arguments to use to check those preconditions
   * @return true if all the preconditions are filled
   *         false else
   */
  def checkPreconditions(arguments: Map[ParameterReference, ParameterValue]): Boolean

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(arguments: Map[ParameterReference, ParameterValue]): Boolean = this match {
    case action: InstanceAction =>
      val arePreconditionsChecked = action.checkPreconditions(arguments)
      if (arePreconditionsChecked) {
        action.executeGoodAction(arguments)
      } else {
//        println("Precondition not filled for " + this.label + ".")
        false
      }
    case effect: Effect => //Problem with effects
      effect.execute(arguments)
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

    if (preconditionCheck) { //Problem with effects
      this.label match {
        case "addInstanceAt" =>
          val instanceId = arguments(ParameterReference("instanceToAdd", "Long")).value//.asInstanceOf[Long]
          val groundId = arguments(ParameterReference("groundWhereToAddIt", "Long")).value//.asInstanceOf[Long]
          List(LogInteraction("ADD " + instanceId + " " + groundId, 3))
        case "createInstanceAt" =>
          val instanceId = arguments(ParameterReference("conceptID", "Long")).value//.asInstanceOf[Long]
          val groundId = arguments(ParameterReference("groundWhereToAddIt", "Long")).value//.asInstanceOf[Long]
          List(LogInteraction("CREATE " + instanceId + " " + groundId, 5))
        case "removeInstanceAt" =>
          val instanceId = arguments(ParameterReference("instanceToRemove", "Long")).value//.asInstanceOf[Long]
          List(LogInteraction("REMOVE " + instanceId, 4))
        case "addToProperty" =>
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value//.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value//.asInstanceOf[String]
          val valueToAdd = arguments(ParameterReference("valueToAdd", "Int")).value//.asInstanceOf[String]
          List(LogInteraction("ADD_TO_PROPERTY " + instanceId + " " + propertyString + " " + valueToAdd, 2))
        case "consume" => //TODO Shouldn't be here
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value//.asInstanceOf[Long]
        val propertyString = arguments(ParameterReference("propertyName", "Property")).value//.asInstanceOf[String]
        val valueToAdd = arguments(ParameterReference("valueToAdd", "Int")).value//.asInstanceOf[String]
          List(LogInteraction("CONSUME " + instanceId + " " + propertyString + " " + valueToAdd, 2))
        case "modifyProperty" =>
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value//.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value//.asInstanceOf[String]
          val newValue = arguments(ParameterReference("propertyValue", "Int")).value//.asInstanceOf[String]
          List(LogInteraction("MODIFY_PROPERTY " + instanceId + " " + propertyString + " " + newValue, 1))
        case "modifyPropertyWithParam" => //TODO not sure this should be here neither
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value//.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value//.asInstanceOf[String]
          val newValue = arguments(ParameterReference("propertyValue", "Property")).value//.asInstanceOf[String]
          List(LogInteraction("MODIFY_PROPERTY_WITH_PARAM " + instanceId + " " + propertyString + " " + newValue, 1))
        case otherLabel =>
          subInteractions.flatMap(subAction => subAction._1.log(takeGoodArguments(subAction._2, arguments)))
      }
    } else {
//      println("Precondition not filled for action " + this.label + ".")
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
      case reference: ParameterReference => arguments(reference)
      case value: ParameterValue => value
      case e =>
        println("Failed to match parameter " + e)
        ParameterValue.error
    }.filter(_._2 != ParameterValue.error)
  }
}

