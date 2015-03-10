package models.rules

import anorm.SqlParser._
import anorm._
import models.rules.custom_types.RuleStatement
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param parameters parameters for the function
 * @param preconditions preconditions for the function
 * @param subRules content of the rule
 */
<<<<<<< HEAD:modules/rules/app/models/rules/Rule.scala
case class Rule(id: Option[Long], label: String, param: List[String], precond: List[String], content: List[String])
=======
case class Rule(id: Option[Long],
                label: String,
                parameters: Array[String],
                preconditions: Array[String],// TODO Make a table for preconditions too => Array[Long]
                subRules: Array[String])//TODO should be id of other rules => Array[Long]

>>>>>>> c75da5579694c295f3aefde7095c033170f86672:app/models/rules/Rule.scala

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
      case id ~ label ~ param ~ precond ~ content => Rule(id, label, param.split(";").toList, precond.split(";").toList, content.split(";").toList)
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
  def getAll: List[Rule] = {
    DB.withConnection { implicit connection =>
      val statement = RuleStatement.getAll
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
      val statement = RuleStatement.add(rule)
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
        val statement = RuleStatement.get(id)
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
      val statement = RuleStatement.set(id, rule)
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


