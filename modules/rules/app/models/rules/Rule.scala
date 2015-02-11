package models.rules

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param param parameters for the function
 * @param rule content of the rule
 */
case class Rule(id: Option[Int], label: String, param: Array[String], rule: String)

/**
 * Model for rule.
 * http://workwithplay.com/blog/2013/05/08/persist-data-with-anorm/
 */
object Rule {
  /**
   * Parse rule to interact with database
   * @author Aurélie LORGEOUX
   */
  private val ruleParser: RowParser[Rule] = {
    get[Option[Int]]("id") ~
      get[String]("label") ~
      get[Array[String]]("param") ~
      get[String]("rule") map {
      case id ~ label ~ param ~ rule => Rule(id, label, param, rule)
    }
  }

  /**
   * Save rule in database
   * @author Aurélie LORGEOUX
   * @param rule rule to put in the database
   */
  def save(rule: Rule) {
    DB.withConnection { implicit connection =>
      SQL(
        """
            INSERT INTO rules(label, param, rule)
            VALUES({label}, {param}, {rule})
          """).on(
          'label -> rule.label,
          'param -> rule.param.mkString("[",";","]"),
          'rule -> rule.
            rule
      ).executeUpdate
    }
  }

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def list = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from rules").as(ruleParser *)
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def load(id: Int): Option[Rule] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from rules WHERE id = {id}")
        .on('id -> id)
        .as(ruleParser.singleOpt)
    }
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param rule rule identified by id
   */
  def update(id: Int, rule: Rule) {
    DB.withConnection { implicit connection =>
      SQL("""
            UPDATE rules SET
            label = {label},
            param = {param},
            rule = {rule}
            WHERE id = {id}
          """).on(
          'id -> id,
          'label -> rule.label,
          'param -> rule.param.mkString("[",";","]"),
          'rule -> rule.rule
        ).executeUpdate
    }
  }

  /**
   * Delete a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   */
  def delete(id: Int) {
    DB.withConnection { implicit connection =>
      SQL("""
          DELETE FROM rules where id = {id}
          """).on(
          'id -> id
        ).executeUpdate
    }
  }
}

