package models.graph.custom_types.consequence

import anorm._

/**
 * Statement to make transaction with Consequences DB
 * @author Thomas GIOVANNINI
 */
object ConsequenceStatement {

  /**
   * Request to clear the database
   * @author Thomas GIOVANNINI
   * @return a sql statement
   */
  val clearDB = {
    SQL("DELETE FROM consequences;")
  }

  /**
   * Request to select all elements from database
   * @author Thomas GIOVANNINI
   * @return a sql statement
   */
  val getAll = {
    SQL("SELECT * FROM consequences")
  }

  /**
   * Add a consequence to database
   * @author Thomas GIOVANNINI
   * @param consequence to add to the DB
   * @return a sql statement
   */
  def add(consequence: Consequence) = {
    SQL("""
            INSERT INTO consequences(label, severity, effect)
            VALUES({label}, {severity}, {effect})
        """).on(
        'label -> consequence.label,
        'severity -> consequence.severity,
        'effect -> consequence.effect.id
      )
  }

  /**
   * Get a consequence from database
   * @author Thomas GIOVANNINI
   * @param id id of the consequence
   * @return a sql statement
   */
  def getById(id: Long) = {
    SQL("SELECT * from consequences WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Rename a consequence in database
   * @author Thomas GIOVANNINI
   * @param id id of the consequence
   * @param newConsequenceName new consequence name
   * @return a sql statement
   */
  def rename(id: Long, newConsequenceName: String) = {
    SQL("""
      UPDATE consequences SET
      label = {label}
      WHERE id = {id}
        """).on(
        'id -> id,
        'label -> newConsequenceName
      )
  }

  /**
   * Remove a consequence from database
   * @author Thomas GIOVANNINI
   * @param id id of the consequence
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("DELETE FROM consequences where id = {id}").on(
        'id -> id
      )
  }

}
