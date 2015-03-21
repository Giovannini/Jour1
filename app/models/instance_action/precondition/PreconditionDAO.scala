package models.instance_action.precondition

import anorm.SqlParser._
import anorm._
import controllers.Application
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps


/**
 * Model for rule.
 */
object PreconditionDAO {

  implicit val connection = Application.connection

  /**
    * Parse rule to interact with database
   * @author Aurélie LORGEOUX
   */
  private val preconditionParser: RowParser[Precondition] = {
    get[Long]("id") ~
    get[String]("label") ~
    get[String]("parameters") ~
    get[String]("subconditions")map {
      case id ~ label ~ param ~ precond => Precondition.parse(id, label, param, precond)
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
  def save(precondition: Precondition): Precondition = {
    DB.withConnection { implicit connection =>
      val statement = PreconditionStatement.add(precondition)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      precondition.withId(id)
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


