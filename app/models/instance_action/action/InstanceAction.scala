package models.instance_action.action

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.instance_action.parameter.{ParameterReference, Parameter}
import models.instance_action.precondition.{Precondition, PreconditionDAO}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

//TODO comment
/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param preconditions preconditions for the function
 * @param subActions content of the rule
 * @param parameters parameters for the function
 */
case class InstanceAction(id: Long,
                          label: String,
                          preconditions: List[(Precondition, Map[ParameterReference, Parameter])],
                          subActions: List[(InstanceAction, Map[ParameterReference, Parameter])],
                          parameters: List[ParameterReference]) {


  /**
   * Modify ID of the action
   * @author Thomas GIOVANNINI
   * @param newId that will be given to the action
   * @return a new InstanceAction looking like this one but with a new ID
   */
  def withId(newId: Long): InstanceAction = {
    InstanceAction(newId, this.label, this.preconditions, this.subActions, this.parameters)
  }

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
      "parameters" -> referenceToParameter.map(item => Parameter.toJsonWithIsParam(item._2))
    )
  }

  def toSimpleJson: JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "parameters" -> parameters.map(_.toJson)
    )
  }

  /**
   * Save the action to database
   * @author Thomas GIOVANNINI
   * @return its ID
   */
  def save: InstanceAction = {
    InstanceAction.save(this)
  }
}

/**
 * Model for rule
 */
object InstanceAction {
  implicit val connection = Application.connection

  val error = InstanceAction(-1, "error", List(), List(), List())

  /**
   * Parse an action from strings
   * @param id of the action
   * @param label of the action
   * @param parametersToParse to retrieve real parameters of the action
   * @param preconditionsToParse to retrieve real preconditions of the action
   * @param subActionsToParse to retrieve real sub-actions of the action
   * @return the corresponding action
   */
  def parse(id: Long, label: String, parametersToParse: String, preconditionsToParse: String, subActionsToParse: String): InstanceAction = {
    InstanceAction(
      id,
      label,
      Precondition.parseSubConditions(preconditionsToParse),
      parseSubActions(subActionsToParse),
      Parameter.parseParameters(parametersToParse)
    )
  }

  def parseSubActions(subActionsToParse: String): List[(InstanceAction, Map[ParameterReference, Parameter])] = {
    if(subActionsToParse != "") {
      subActionsToParse.split(";")
        .map(s => parseSubAction(s))
        .toList
    } else {
      List()
    }
  }

  def parseSubAction(subActionToParse: String) : (InstanceAction, Map[ParameterReference, Parameter]) = {
    val globalPattern = "(^\\d*|\\(.*\\)$)".r
    val result = globalPattern.findAllIn(subActionToParse).toArray

    // Get the precondition
    val id = result(0).toInt
    val action = InstanceAction.getById(id)

    // Set the map of parameters
    val paramPattern = "([^\\(|,|\\)]+)".r
    val params = paramPattern.findAllIn(result(1)).toList.map(Parameter.parse)

    (action, Parameter.linkParameterToReference(action.parameters, params))
  }

  def retrieveFromStringOfIds(stringOfIds: String): List[InstanceAction] = {
    Try{
      stringOfIds.split(";")
        .map{ id =>
          getById(id.toLong)
        }.toList
    } match {
      case Success(list) => list
      case Failure(e) =>
        println("Error occured while executing function retrieveFromStringOfIds in class InstanceAction")
        println(e)
        List()
    }
  }

  /**
   * Parse rule to interact with database
   * @author Aurélie LORGEOUX
   */
  private val actionParser: RowParser[InstanceAction] = {
    get[Long]("id") ~
      get[String]("label") ~
      get[String]("param") ~
      get[String]("precond") ~
      get[String]("content") map {
      case id ~ label ~ param ~ precond ~ content => InstanceAction.parse(id, label, param, precond, content)
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clearDB: Int = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.clearDB
      statement.executeUpdate
    }
  }

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def getAll: List[InstanceAction] = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.getAll
      statement.as(actionParser *)
    }
  }

  /**
   * Save rule in database
   * @author Aurélie LORGEOUX
   * @param action rule to put in the database
   * @return true if the rule saved
   *         false else
   */
  def save(action: InstanceAction): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.add(action)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      action.withId(id)
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def getById(id: Long): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.get(id)
      statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
    }
  }

  /**
   * Get one rule saved in database with its name
   * @author Thomas GIOVANNINI
   * @param name of the rule
   * @return rule identified by id
   */
  def getByName(name: String): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.getByName(name)
      statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
    }
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param action rule identified by id
   */
  def update(id: Long, action: InstanceAction): Int = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.set(id, action)
      statement.executeUpdate
    }
  }

  /**
   * Delete a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   */
  def delete(id: Long): Int = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.remove(id)
      statement.executeUpdate
    }
  }

}


