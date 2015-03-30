package models.interaction.effect

import controllers.Application
import models.interaction.Interaction
import models.interaction.parameter.{Parameter, ParameterReference, ParameterValue}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param _subActions content of the rule
 * @param parameters parameters for the function
 */
case class Effect(
  id: Long,
  label: String,
  _subActions: List[(Effect, Map[ParameterReference, Parameter])],
  parameters: List[ParameterReference]) extends Interaction{

  val subInteractions = _subActions.map(tuple => (tuple._1.asInstanceOf[Effect], tuple._2))

  /**
   * Modify ID of the action
   * @author Thomas GIOVANNINI
   * @param newId that will be given to the action
   * @return a new InstanceAction looking like this one but with a new ID
   */
  def withId(newId: Long): Effect = {
    Effect(newId, this.label, this.subInteractions, this.parameters)
  }

  /*#################
    DB interactions
  #################*/
  /**
   * Save the action to database
   * @author Thomas GIOVANNINI
   * @return its ID
   */
  def save: Effect = {
    EffectDAO.save(this)
  }

  /**
   * Check if all the effects' preconditions are filled before executing it.
   * @param arguments to use to check those preconditions
   * @return true
   */
  override def checkPreconditions(arguments: Map[ParameterReference, ParameterValue]): Boolean = true
}

/**
 * Model for rule
 */
object Effect {

  implicit val connection = Application.connection

  val error = Effect(-1, "error", List(), List())

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
  : Effect = {
    def parseSubActions(subActionsToParse: String): List[(Effect, Map[ParameterReference, Parameter])] = {
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
    def parseSubAction(subActionToParse: String): (Effect, Map[ParameterReference, Parameter]) = {
      val globalPattern = "(^\\d*|\\(.*\\)$)".r
      val result = globalPattern.findAllIn(subActionToParse).toArray

      // Get the precondition
      val id = result(0).toInt
      val action = EffectDAO.getById(id)

      // Set the map of parameters
      val paramPattern = "([^\\(|,|\\)]+)".r
      val params = paramPattern.findAllIn(result(1)).toList.map(Parameter.parse)

      (action, Parameter.linkParameterToReference(action.parameters, params))
    }

    Try {
      Effect(
        id,
        label,
        parseSubActions(subActionsToParse),
        Parameter.parseParameters(parametersToParse)
      )
    } match {
      case Success(action) => action
      case Failure(e) =>
        println("Error while parsing action " + label)
        println(e)
        Effect.error
    }
  }
}


