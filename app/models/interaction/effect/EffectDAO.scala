package models.interaction.effect

import anorm.SqlParser._
import anorm.{RowParser, ~}
import controllers.Application
import models.interaction.{InteractionDAO, InteractionStatement}
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Distance Access Object for instance's actions
 */
object EffectDAO {

  implicit val connection = Application.connection

  /**
   * Parse an action of an instance from database
   * @author Thomas GIOVANNINI
   */
  private val actionParser: RowParser[Effect] = {
    get[Long]("id") ~
    get[String]("label") ~
    get[String]("param") ~
    get[String]("precond") ~
    get[String]("content") map {
      case id ~ label ~ param ~ precond ~ content => Effect.parse(id, label, param, precond, content)
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clearDB(): Boolean = InteractionDAO.clearDB()

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def getAll: List[Effect] = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.getAll
      statement.as(actionParser *)
    }
  }

  /**
   * Save rule in database
   * @author Aurélie LORGEOUX
   * @param effect rule to put in the database
   * @return true if the rule saved
   *         false else
   */
  def save(effect: Effect): Effect = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.add(effect)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      effect.withId(id)
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def getById(id: Long): Effect = {
    Try {
      DB.withConnection { implicit connection =>
        val statement = InteractionStatement.get(id)
        statement.as(actionParser.singleOpt).getOrElse(Effect.error)
      }
    } match {
      case Success(effect) => effect
      case Failure(e) =>
        println("Error while trying to retrieve an interaction by its name from the DB:")
        println(e.getStackTrace)
        Effect.error
    }
  }

  /**
   * Get one rule saved in database with its name
   * @author Thomas GIOVANNINI
   * @param name of the rule
   * @return rule identified by id
   */
  def getByName(name: String): Effect = {
    Try {
      DB.withConnection { implicit connection =>
        val statement = InteractionStatement.getByName(name)
        statement.as(actionParser.singleOpt).getOrElse(Effect.error)
      }
    } match {
      case Success(effect) => effect
      case Failure(e) =>
        println("Error while trying to retrieve an interaction by its name from the DB:")
        println(e.getStackTrace)
        Effect.error
    }
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param effect rule identified by id
   */
  def update(id: Long, effect: Effect): Int = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.set(id, effect)
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
