package models.intelligence.need

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
            INSERT INTO needs(label, property, priority, consequencesSteps, meansOfSatisfaction)
            VALUES({label}, {property}, {priority}, {consequencesSteps}, {meansOfSatisfaction})
        """).on(
        'label -> need.label,
        'property -> need.affectedProperty.toString,
        'priority -> need.priority,
        'consequencesSteps -> need.consequencesSteps.map(_.toDB).mkString(";"),
        'meansOfSatisfaction -> need.meansOfSatisfaction.map(_.toString).mkString(";")
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

  def getNeedsWhereConceptUsed(conceptId: Long) = {
    SQL("SELECT * FROM NEEDS WHERE meansOfSatisfaction LIKE {concept}")
      .on("concept" -> ("%" + conceptId + "%"))
  }

  /**
   * Updates a need in database
   * @param id of the need
   * @param need after update
   * @return a sql statement
   */
  def update(id: Long, need: Need) = {
    SQL("""
        |UPDATE  needs
        |SET
        |    label = {label},
        |    property = {property},
        |    priority = {priority},
        |    consequencesSteps = {consequencesSteps},
        |    meansOfSatisfaction = {meansOfSatisfaction}
        |WHERE id = {id}
        """.stripMargin)
      .on(
        'label -> need.label,
        'property -> need.affectedProperty.toString,
        'priority -> need.priority,
        'consequencesSteps -> need.consequencesSteps.map(_.toDB).mkString(";"),
        'meansOfSatisfaction -> need.meansOfSatisfaction.map(_.toString).mkString(";")
      )
  }

  def updateNeedsUsingConcept(oldId: Long, newId: Long) = {
    SQL(
      """
        |UPDATE needs
        |SET
        | meansOfSatisfaction = REPLACE(meansOfSatisfaction, {oldConcept}, {newConcept})
        | WHERE meansOfSatisfaction LIKE {likeConcept}
      """.stripMargin)
    .on(
      'oldConcept -> oldId.toString,
      'newConcept -> newId.toString,
      'likeConcept -> ("%"+oldId+"%")
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
