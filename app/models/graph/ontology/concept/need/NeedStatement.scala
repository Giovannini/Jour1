package models.graph.ontology.concept.need

import anorm._

/**
 * Statement to make transaction with Needs DB
 * @author Thomas GIOVANNINI
 */
object NeedStatement {

  /**
   * Request to clear the database
   * @author Thomas GIOVANNINI
   * @return a sql statement
   */
  val clearDB = {
    SQL("DELETE FROM needs;")
  }

  /**
   * Request to select all elements from database
   * @author Thomas GIOVANNINI
   * @return a sql statement
   */
  val getAll = {
    SQL("SELECT * FROM needs")
  }

  /**
   * Add a need to database
   * @author Thomas GIOVANNINI
   * @param need to add to the DB
   * @return a sql statement
   */
  def add(need: Need) = {
    SQL("""
            INSERT INTO needs(label, property, consequencesSteps, meansOfSatisfaction)
            VALUES({label}, {property}, {consequencesSteps}, {meansOfSatisfaction})
        """).on(
        'label -> need.label,
        'property -> need.affectedProperty.toString,
        'consequencesSteps -> need.consequencesSteps.map(_.toDB).mkString(";"),
        'meansOfSatisfaction -> need.meansOfSatisfaction.map(_.id).mkString(";")
      )
  }

  /**
   * Get a need from database
   * @author Thomas GIOVANNINI
   * @param id id of the need
   * @return a sql statement
   */
  def getById(id: Long) = {
    SQL("SELECT * from needs WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Rename a need in database
   * @author Thomas GIOVANNINI
   * @param id id of the need
   * @param newNeedName new need name
   * @return a sql statement
   */
  def rename(id: Long, newNeedName: String) = {
    SQL("""
      UPDATE needs SET
      label = {label}
      WHERE id = {id}
        """).on(
        'id -> id,
        'label -> newNeedName
      )
  }

  /**
   * Remove a need from database
   * @author Thomas GIOVANNINI
   * @param id id of the need
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("DELETE FROM needs where id = {id}").on(
        'id -> id
      )
  }

}
