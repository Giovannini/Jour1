package models.instance_action.action

import anorm._

/**
 * All values from this objects are SQLStatements
 */
object InstanceActionStatement {

  /**
   * Request to clear the database
   * @author Aurélie LORGEOUX
   * @return a sql statement
   */
  val clearDB = {
    SQL("DELETE FROM rules")
  }


  /**
   * Request to select all elements from database
   * @author Aurélie LORGEOUX
   * @return a sql statement
   */
  val getAll = {
    SQL("SELECT * FROM rules")
  }

  private def precondString(action: InstanceAction): String = {
    action.preconditions.map(p => {
      p._1.id + " (" + p._2.unzip._2.map(_.toDBString).mkString(",")+")"
    }).mkString(";")
  }

  private def contentString(action: InstanceAction): String = {
    action.subActions.map(action => {
      action._1.id + " (" + action._2.unzip._2.map(_.toDBString).mkString(",")+")"
    }).mkString(";")
  }

  /**
   * Add a rule to database
   * @author Aurélie LORGEOUX
   * @param action rule to add
   * @return a sql statement
   */
  def add(action: InstanceAction) = {
    SQL("""
            INSERT INTO rules(label, param, precond, content)
            VALUES({label}, {param}, {precond}, {content})
    """).on(
      'label -> action.label,
      'param -> action.parameters.mkString(";"),
      'precond -> precondString(action),
      'content -> contentString(action)
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

  def getByName(name: String) = {
    SQL("SELECT * from rules WHERE label = {label}")
      .on('label -> name)
  }

  /**
   * Set a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param action new rule with changes
   * @return a sql statement
   */
  def set(id: Long, action: InstanceAction) = {
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
        'precond -> precondString(action),
        'content -> contentString(action)
      )
  }

  /**
   * Remove a rule from database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("DELETE FROM rules where id = {id}").on(
      'id -> id
    )
  }
}