package models.rules.custom_types

import anorm._
import models.rules.Rule

/**
 * All values from this objects are SQLStatements
 */
object Statement {

  /**
   * Request to clear the database
   * @author Aurélie LORGEOUX
   * @return a sql statement        
   */
  val clearDB = {
    SQL("""
       DELETE FROM rules;
    """)
  }

  /**
   * Request to select all elements from database
   * @author Aurélie LORGEOUX
   * @return a sql statement        
   */
  val getAll = {
    SQL("""
      SELECT * FROM rules
    """)
  }

  /**
   * Add a rule to database
   * @author Aurélie LORGEOUX
   * @param rule rule to add
   * @return a sql statement
   */
  def add(rule: Rule) = {
    SQL("""
            INSERT INTO rules(label, param, precond, content)
            VALUES({label}, {param}, {precond}, {content})
    """).on(
      'label -> rule.label,
      'param -> rule.param.mkString(";"),
      'precond -> rule.precond.mkString(";"),
      'content-> rule.content.mkString(";")
    )
  }

  /**
   * Get a rule from database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return a sql statement
   */
  def get(id: Long) = {
    SQL("SELECT * from rules WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Set a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param rule new rule with changes
   * @return a sql statement
   */
  def set(id: Long, rule: Rule) = {
    SQL("""
      UPDATE rules SET
      label = {label},
      param = {param},
      precond = {precond},
      content = {content}
      WHERE id = {id}
    """).on(
      'id -> id,
      'label -> rule.label,
      'param -> rule.param.mkString(";"),
      'precond -> rule.precond.mkString(";"),
      'content -> rule.content.mkString(";")
    )
  }

  /**
   * Remove a rule from database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("""
      DELETE FROM rules where id = {id}
    """).on(
      'id -> id
    )
  }
}