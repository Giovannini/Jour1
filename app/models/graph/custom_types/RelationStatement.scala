package models.graph.custom_types

import anorm._
import models.graph.ontology.Relation
import play.api.db.DB

/**
 * All values from this objects are SQLStatements
 */
object RelationStatement {
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
   * @param relation relation to add
   * @return a sql statement
   */
  def add(relation: Relation) = {
    SQL("""
            INSERT INTO relation(label, src, dest)
            VALUES({label}, {src}, {dest})
        """).on(
        'label -> relation.label,
        'src -> relation.src,
        'dest -> relation.dest
      )
  }

  /**
   * Get a relation from database
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return a sql statement
   */
  def get(id: Long) = {
    SQL("SELECT * from relations WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Set a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @param relation new relation with changes
   * @return a sql statement
   */
  def set(id: Long, relation: Relation) = {
    SQL("""
      UPDATE rules SET
      label = {label},
      src = {src},
      dest = {dest}
      WHERE id = {id}
        """).on(
        'id -> id,
        'label -> relation.label,
        'src -> relation.src,
        'dest -> relation.dest
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
