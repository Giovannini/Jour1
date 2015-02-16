package models.rules

import anorm._
import anorm.SqlParser._
import models.rules.custom_types.Statement
import play.api.db.DB
import play.api.Play.current

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param param parameters for the function
 * @param precond preconditions for the function
 * @param content content of the rule
 */
case class Rule(id: Option[Long], label: String, param: Array[String], precond: Array[String], content: Array[String])

/**
 * Model for rule.
 */
object Rule {
  implicit val connection = DB.getConnection()

  /**
   * Parse rule to interact with database
   * @author Aurélie LORGEOUX
   */
  private val ruleParser: RowParser[Rule] = {
    get[Option[Long]]("id") ~
      get[String]("label") ~
      get[String]("param") ~
      get[String]("precond") ~
      get[String]("content") map {
      case id ~ label ~ param ~ precond ~ content => Rule(id, label, param.split(";"), precond.split(";"), content.split(";"))
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clear: Int = {
    DB.withConnection { implicit connection =>
      val statement = Statement.clearDB
      statement.executeUpdate
    }
  }

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def list: List[Rule] = {
    DB.withConnection { implicit connection =>
      val statement = Statement.getAll
      statement.as(ruleParser *)
    }
  }

  /**
   * Save rule in database
   * @author Aurélie LORGEOUX
   * @param rule rule to put in the database
   * @return true if the rule saved
   *         false else
   */
  def save(rule: Rule): Option[Long] = {
    DB.withConnection { implicit connection =>
      val statement = Statement.add(rule)
      statement.executeInsert()
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def load(id: Long): Option[Rule] = {
    DB.withConnection { implicit connection =>
        val statement = Statement.get(id)
        statement.as(ruleParser.singleOpt)
    }
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param rule rule identified by id
   */
  def update(id: Long, rule: Rule): Int = {
    DB.withConnection { implicit connection =>
      val statement = Statement.set(id, rule)
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
      val statement = Statement.remove(id)
      statement.executeUpdate
    }
  }
}


