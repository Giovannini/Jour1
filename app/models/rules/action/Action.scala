package models.rules.action

import anorm.SqlParser._
import anorm._
import models.rules.Argument
import models.rules.custom_types.RuleStatement
import models.rules.precondition.{PreconditionDAO, Precondition}
import play.api.Play.current
import play.api.db.DB

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
case class Action(id: Long,
                  label: String,
                  preconditions: List[Precondition],
                  subActions: List[Action],
                  parameters: List[Argument]) {
  def withId(id: Long): Action = {
    Action(id, this.label, this.preconditions, this.subActions, this.parameters)
  }
}

/**
 * Model for rule.
 */
object Action {
  implicit val connection = DB.getConnection()

  def identify(id: Long, label: String, preconditions: List[Long], subActions: List[Long], parameters: List[Argument]): Action = {
    Action(id, label, preconditions.map(PreconditionDAO.getById), subActions.map(getById), parameters)
  }

  def parse(id: Long, label: String, parameters: String, preconditions: String, subActions: String): Action = {
    val parsedParameters = {
      println(label)
      println("parameters: " + parameters)
      if (parameters == "") List()
      else if (! parameters.contains(":")){
        Action.delete(id)
        List()
      }
      else parameters.split(";")
        .map(_.split(":"))
        .map(array => Argument(array(0), array(1)))
        .toList
    }
    val parsedPreconditions = {
      if(preconditions == "") List()
      else if(preconditions.matches("*[A-Za-z]*")){
        Action.delete(id)
        List()
      }
      else preconditions.split(";")
        .map(_.toLong)
        .map(PreconditionDAO.getById)
        .toList
    }
    //println("Got preconditions: " + parsedPreconditions.map(_.label).mkString(", "))
    val parsedSubActions: List[Action] = {
      if (subActions == "") List()
      else subActions.split(";").map(s => getById(s.toLong)).toList
    }
    Action(id, label, parsedPreconditions, parsedSubActions, parsedParameters)
  }
  
  val error = Action(-1, "error", List[Precondition](), List[Action](), List[Argument]())

  /**
   * Parse rule to interact with database
   * @author Aurélie LORGEOUX
   */
  private val actionParser: RowParser[Action] = {
    get[Long]("id") ~
      get[String]("label") ~
      get[String]("param") ~
      get[String]("precond") ~
      get[String]("content") map {
      case id ~ label ~ param ~ precond ~ content => Action.parse(id, label, param, precond, content)
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clear: Int = {
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
  def getAll: List[Action] = {
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
  def save(action: Action): Long = {
    DB.withConnection { implicit connection =>
      val statement = RuleStatement.add(action)
      val optionId: Option[Long] = statement.executeInsert()
      /*val id = */optionId.getOrElse(-1L)
      //if (id == -1L) Action.error else action.withId(id)
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def getById(id: Long): Action = {
    DB.withConnection { implicit connection =>
        val statement = RuleStatement.get(id)
        statement.as(actionParser.singleOpt).getOrElse(Action.error)
    }
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param action rule identified by id
   */
  def update(id: Long, action: Action): Int = {
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


