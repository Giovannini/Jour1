package models.rules.precondition

import anorm.SqlParser._
import anorm._
import models.rules.custom_types.PreconditionStatement
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param parameters parameters for the function
 * @param subconditions preconditions for the function
 */
case class PreconditionDAO(id: Option[Long],
                           label: String,
                           parameters: Array[String],
                           subconditions: Array[String])// TODO Make a table for preconditions too => Array[Long]


/**
 * Model for rule.
 */
object PreconditionDAO {
  implicit val connection = DB.getConnection()

  /**
   * Parse rule to interact with database
   * @author Aurélie LORGEOUX
   */
  private val preconditionParser: RowParser[Precondition] = {
    get[Option[Long]]("id") ~
      get[String]("label") ~
      get[String]("parameters") ~
      get[String]("subconditions")map {
      case id ~ label ~ param ~ precond => Precondition.parse(id.get, label, param.split(";"), precond.split(";"))
    }
  }

  /**
   * Clear the database
   * @author Thomas GIOVANNINI
   * @return number of preconditions deleted
   */
  def clear: Int = {
    DB.withConnection { implicit connection =>
      val statement = PreconditionStatement.clearDB
      statement.executeUpdate
    }
  }

  /**
   * Get all preconditions saved in database
   * @author Thomas GIOVANNINI
   * @return a list of preconditions
   */
  def getAll: List[Precondition] = {
    DB.withConnection { implicit connection =>
      val statement = PreconditionStatement.getAll
      statement.as(preconditionParser *)
    }
  }

  /**
   * Save precondition in database
   * @author Thomas GIOVANNINI
   * @param precondition precondition to put in the database
   * @return true if the precondition saved
   *         false else
   */
  def save(precondition: Precondition): Long = {
    DB.withConnection { implicit connection =>
      val statement = PreconditionStatement.add(precondition)
      val optionId: Option[Long] = statement.executeInsert()
      optionId.getOrElse(-1L)
    }
  }

  /**
   * Get one precondition saved in database with its id
   * @author Thomas GIOVANNINI
   * @param id id of the precondition
   * @return precondition identified by id
   */
  def getById(id: Long): Precondition = {
    DB.withConnection { implicit connection =>
      val statement = PreconditionStatement.getById(id)
      val maybePrecondition = statement.as(preconditionParser.singleOpt)
      maybePrecondition.getOrElse(Precondition.error)
    }
  }

  /**
   * Update a precondition in database
   * @author Thomas GIOVANNINI
   * @param id id of the precondition
   * @param precondition precondition identified by id
   */
  def update(id: Long, precondition: Precondition): Int = {
    DB.withConnection { implicit connection =>
      val statement = PreconditionStatement.update(id, precondition)
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
      val statement = PreconditionStatement.remove(id)
      statement.executeUpdate
    }
  }
}


