package models.rules.action

import anorm.SqlParser._
import anorm._
import models.rules.Argument
import models.rules.custom_types.RuleStatement
import models.rules.precondition.{PreconditionDAO, Precondition}
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.{JsArray, JsString, JsNumber, Json}

import scala.language.postfixOps

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
                  subActions: List[(InstanceAction, String)],
                  parameters: List[Argument]) {
  def withId(id: Long): InstanceAction = {
    InstanceAction(id, this.label, this.preconditions, this.subActions, this.parameters)
  }

  def toJson = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "preconditions" -> JsArray(preconditions.map(_.toJson)),
      "parameters" -> JsArray(parameters.map(_.toJson))
    )
  }

  def save: Long = {
    InstanceAction.save(this)
  }
}

/**
 * Model for rule
 */
object InstanceAction {
  implicit val connection = DB.getConnection()

  def identify(id: Long, label: String, preconditions: List[Long], subActions: List[(Long, String)], parameters: List[Argument]): InstanceAction = {
    InstanceAction(id, label, preconditions.map(PreconditionDAO.getById),
      subActions.map(tuple => (getById(tuple._1), tuple._2)),
      parameters)
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
  def parse(id: Long, label: String, parametersToParse: String, preconditionsToParse: String, subActionsToParse: String): InstanceAction = {
    var error = false
    def parseParameters(): List[Argument] = {
      if (parametersToParse == "") List()
      else if (! parametersToParse.contains(":")){
        error = true
        List()
      }
      else parametersToParse.split(";")
        .map(_.split(":"))
        .map(array => Argument(array(0), array(1)))
        .toList
    }
    def parsePreconditions(): List[Precondition] = {
      //println("Parsing preconditions from: " + preconditionsToParse)
      //println(PreconditionDAO.getAll.mkString(", "))
      if(preconditionsToParse == "") List()
      else if(! preconditionsToParse.matches("[0-9;]*")){
        InstanceAction.delete(id) // TODO: better than deleting, notifying that an error has occured
        List()
      }
      else{
        preconditionsToParse.split(";")
          .map(_.toLong)
          .map(PreconditionDAO.getById)
          .toList
      }
    }
    def parseSubActions(): List[(InstanceAction, String)] = {
      if (subActionsToParse == "") List()
      else subActionsToParse.split(";").map { s =>
        val splitted = s.split(":")
        (getById(splitted(0).toLong), splitted(1))
      }.toList
    }
    //println("Parsing action " + label + "...")
    val parameters = parseParameters()
    //println("Parameters: " + parameters.map(_.reference).mkString(", "))
    val preconditions = parsePreconditions()
    //println("Preconditions: " + preconditions.map(_.label).mkString(", "))
    val parsedSubActions = parseSubActions()
    //println("Subactions: " + parsedSubActions.map(_._1.label).mkString(", "))
    //println("Error? " + error)
    if(error) InstanceAction.error
    else InstanceAction(id, label, preconditions, parsedSubActions, parameters)
  }
  
  val error = InstanceAction(-1, "error", List[Precondition](), List[(InstanceAction, String)](), List[Argument]())

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
      val statement = RuleStatement.clearDB
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
      val statement = RuleStatement.getAll
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
  def save(action: InstanceAction): Long = {
    DB.withConnection { implicit connection =>
      val statement = RuleStatement.add(action)
      val optionId: Option[Long] = statement.executeInsert()
      optionId.getOrElse(-1L)
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
      val statement = RuleStatement.get(id)
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
      val statement = RuleStatement.getByName(name)
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
      val statement = RuleStatement.set(id, action)
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
      val statement = RuleStatement.remove(id)
      statement.executeUpdate
    }
  }
}


