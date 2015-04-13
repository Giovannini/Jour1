package models.graph.relation

import anorm._

/**
 * All values from this objects are SQLStatements
 */
object RelationSQLStatement {

  /**
   * Request to clear the database
   * @author Aurélie LORGEOUX
   * @return a sql statement
   */
  val clearDB = {
    SQL("DELETE FROM relations;")
  }

  /**
   * Request to select all elements from database
   * @author Aurélie LORGEOUX
   * @return a sql statement
   */
  val getAll = {
    SQL("""
      SELECT * FROM relations
        """)
  }

  /**
   * Add a rule to database
   * @author Aurélie LORGEOUX
   * @param relationName relation to add
   * @return a sql statement
   */
  def add(actionID: Long, relationName: String) = {
    SQL("""
            INSERT INTO relations(actionId, label)
            VALUES({actionId}, {label})
        """).on(
        'actionId -> actionID,
        'label -> relationName
      )
  }

  /**
   * Get a relation from database
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return a sql statement
   */
  def getById(id: Long) = {
    SQL("SELECT * from relations WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Get a relation from database
   * @author Aurélie LORGEOUX
   * @param name of the relation
   * @return a sql statement
   */
  def getByName(name: String) = {
    SQL("SELECT * from relations WHERE label = {label}")
      .on('label -> name)
  }

  /**
   * Get a relation from database
   * @author Aurélie LORGEOUX
   * @param id of the relation
   * @return a sql statement
   */
  def getActionId(id: Long) = {
    SQL("SELECT actionId from relations WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Set a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @param relationName new relation name
   * @return a sql statement
   */
  def rename(id: Long, relationName: String) = {
    SQL("""
      UPDATE relations SET
      label = {label}
      WHERE id = {id}
        """).on(
        'id -> id,
        'label -> relationName
      )
  }

  /**
   * Remove a relation from database
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("""
      DELETE FROM relations where id = {id}
        """).on(
        'id -> id
      )
  }
}
