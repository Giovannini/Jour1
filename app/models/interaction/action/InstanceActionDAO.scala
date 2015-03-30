package models.interaction.action

import anorm.SqlParser._
import anorm.{RowParser, ~}
import controllers.Application
import models.interaction.{InteractionDAO, InteractionStatement}
import play.api.db.DB
import play.api.Play.current

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Distance Access Object for instance's actions
 */
object InstanceActionDAO {

  implicit val connection = Application.connection

  /**
   * Parse an action of an instance from database
   * @author Thomas GIOVANNINI
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
  def clearDB(): Int = InteractionDAO.clearDB()

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def getAll: List[InstanceAction] = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.getAll
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
      val statement = InteractionStatement.add(action)
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
    Try {
      DB.withConnection { implicit connection =>
        val statement = InteractionStatement.get(id)
        statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
      }
    } match {
      case Success(action) => action
      case Failure(e) =>
        println("Error while trying to retrieve an interaction by its name from the DB:")
        println(e.getStackTrace)
        InstanceAction.error
    }
  }

  /**
   * Get one rule saved in database with its name
   * @author Thomas GIOVANNINI
   * @param name of the rule
   * @return rule identified by id
   */
  def getByName(name: String): InstanceAction = {
    Try {
      DB.withConnection { implicit connection =>
        val statement = InteractionStatement.getByName(name)
        statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
      }
    } match {
      case Success(interaction) => interaction
      case Failure(e) =>
        println("Error while trying to retrieve an interaction by its name from the DB:")
        println(e.getStackTrace)
        InstanceAction.error
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
      val statement = InteractionStatement.set(id, action)
      statement.executeUpdate
    }
  }

  /**
   * Delete a rule in database
   * @author Thomas GIOVANNINI
   * @param id id of the rule
   */
  def delete(id: Long): Int = InteractionDAO.delete(id)
  
}
