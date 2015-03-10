package models.rules.custom_types

import anorm._
import models.rules.action.Action

/**
 * All values from this objects are SQLStatements
 */
object RuleStatement {

  /**
   * Request to clear the database
   * @author Aurélie LORGEOUX
   * @return a sql statement        
   */
  val clearDB = {
    SQL("DELETE FROM rules;")
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
   * @param action rule to add
   * @return a sql statement
   */
  def add(action: Action) = {
    SQL("""
            INSERT INTO rules(label, param, precond, content)
            VALUES({label}, {param}, {precond}, {content})
    """).on(
      'label -> action.label,
      'param -> action.parameters.map(p => p.reference + ":" + p._type).mkString(";"),
      'precond -> action.preconditions.map(_.id).mkString(";"),
      'content-> action.subActions.map(_.id).mkString(";")
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
   * @param action new rule with changes
   * @return a sql statement
   */
  def set(id: Long, action: Action) = {
    SQL("""
      UPDATE rules SET
      label = {label},
      param = {param},
      precond = {precond},
      content = {content}
      WHERE id = {id}
        """).on(
        'id -> id,
        'label -> action.label,
        'param -> action.parameters.mkString(";"),
        'precond -> action.preconditions.mkString(";"),
        'content -> action.subActions.mkString(";")
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