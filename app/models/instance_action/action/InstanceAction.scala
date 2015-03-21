package models.instance_action.action

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.instance_action.Parameter
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
                          preconditions: List[Precondition],
                          subActions: List[InstanceAction],
                          parameters: List[Parameter]) {

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
      "preconditions" -> preconditions.map(_.toJson),
      "subActions" -> subActions.map(_.toJson),
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

  /**
   * Modify a parameter of the action and for all its subobjects
   * @author Thomas GIOVANNINI
   * @param oldParameter to modify
   * @param newParameter to replace the old one
   * @return an updated InstanceAction
   */
  private def modifyParameter(oldParameter: Parameter, newParameter: Parameter): InstanceAction = {
    /**
     * Modify parameters of the action
     * @author Thomas GIOVANNINI
     * @param newParameters for the action
     * @return updated list of parameters
     */
    def replaceParameterInList(newParameters: List[Parameter]): List[Parameter] = {
      newParameters match {
        case List() => List()
        case head::tail =>
          if (head == oldParameter) newParameter :: tail
          else head :: replaceParameterInList(tail)
      }
    }

    if (parameters.contains(oldParameter)) {
      val newPreconditions: List[Precondition] = preconditions.map(_.modifyParameter(oldParameter, newParameter))
      val newSubActions: List[InstanceAction] = subActions.map(_.modifyParameter(oldParameter, newParameter))
      val newParameters: List[Parameter] = replaceParameterInList(this.parameters)
      InstanceAction(id, label, newPreconditions, newSubActions, newParameters)
    } else this
  }

  /**
   * The same instance action with given parameters
   * @author Thomas GIOVANNINI
   * @param newParameters to give to the instance
   * @return an InstanceAction with those parameters
   */
  def withParameters(newParameters: List[Parameter]): InstanceAction = {
    def modifyParametersRec(parameterTuples: List[(Parameter, Parameter)], action: InstanceAction): InstanceAction = {
      parameterTuples match {
        case List() => action
        case head::tail =>
          val newAction = action.modifyParameter(head._1, head._2)
          modifyParametersRec(tail, newAction)
      }
    }
    if (this.parameters.length == newParameters.length) {
      modifyParametersRec(this.parameters.zip(newParameters), this)
    }else this
  }
}

/**
 * Model for rule
 */
object InstanceAction {
  implicit val connection = Application.connection

  val error = InstanceAction(-1, "error", List[Precondition](), List[InstanceAction](), List[Parameter]())

  lazy val form: Form[InstanceAction] = Form(mapping(
    "id" -> longNumber,
    "label" -> text,
    "preconditions" -> list(longNumber),
    "subactions" -> list(longNumber),
    "parameters" -> list(Parameter.form.mapping)
  )(InstanceAction.applyForm)(InstanceAction.unapplyForm))

  private def applyForm(
    id: Long,
    label: String,
    preconditionsToParse: List[Long],
    subActionsToParse: List[Long],
    parameters: List[Parameter]): InstanceAction = {
    val preconditions = preconditionsToParse.map(PreconditionDAO.getById)
    val subActions = subActionsToParse.map(InstanceAction.getById)
    InstanceAction(id, label, preconditions, subActions, parameters)
  }

  private def unapplyForm(ia: InstanceAction) = {
    Some((ia.id, ia.label, ia.preconditions.map(_.id), ia.subActions.map(_.id), ia.parameters))
  }

  /**
   * Parse an action from strings
   * @param id of the action
   * @param label of the action
   * @param parametersToParse to retrieve real parameters of the action
   * @param preconditionsToParse to retrieve real preconditions of the action
   * @param subActionsToParse to retrieve real sub-actions of the action
   * @return the corresponding action
   */
  def parse(id: Long, label: String, preconditionsToParse: String, subActionsToParse: String, parametersToParse: String)
  : InstanceAction = {
    var error = false
    def parseParameters(): List[Parameter] = {
      if (parametersToParse.isEmpty) List()
      else if (! parametersToParse.contains(":")) {
        error = true
        List()
      }
      else parametersToParse.split(";")
        .map(Parameter.parseArgument)
        .toList
    }
    def parsePreconditions(): List[Precondition] = {
      if (preconditionsToParse == "") List()
      else {
        preconditionsToParse.split(";")
          .map { string =>
          val splitted = string.split(" -> ")
          val precondition = PreconditionDAO.getById(splitted(0).toLong)
          val parameters = splitted(1).split(",").map(Parameter.parseArgument).toList
          precondition.withParameters(parameters)
        }.toList
      }
    }
    def parseSubActions(): List[InstanceAction] = {
      if (subActionsToParse == "") List()
      else subActionsToParse.split(";").map { s =>
        val splitted = s.split(" -> ") //Subaction is ActionID:Parameters
      val instanceAction = getById(splitted(0).toLong)
        val parameters = splitted(1).split(",").map(Parameter.parseArgument).toList
        instanceAction.withParameters(parameters)
      }.toList
    }

    val parameters = parseParameters()
    val preconditions = parsePreconditions()
    val parsedSubActions = parseSubActions()
    if (error) InstanceAction.error
    else InstanceAction(id, label, preconditions, parsedSubActions, parameters)
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
      case id ~ label ~ param ~ precond ~ content => InstanceAction.parse(id, label, precond, content, param)
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


