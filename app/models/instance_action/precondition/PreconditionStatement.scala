package models.instance_action.precondition

import anorm._
import models.instance_action.parameter.{Parameter, ParameterReference}

/**
 * All values from this objects are SQLStatements
 */
object PreconditionStatement {

  /**
   * Request to clear the database
   * @author Thomas GIOVANNINI
   * @return a sql statement        
   */
  val clearDB = {
    SQL("DELETE FROM preconditions")
  }

  /**
   * Request to select all elements from database
   * @author Thomas GIOVANNINI
   * @return a sql statement        
   */
  val getAll = {
    SQL("SELECT * FROM preconditions")
  }


  private def parametersString(precondition: Precondition): String = {
    precondition.parameters.map(_.toString).mkString(",")
  }

  private def subconditionString(precondition: Precondition): String = {
    precondition.subConditions.map(
      item => {
        val precondition = item._1
        val parameters = item._2
        precondition.label + "" + "(" + parameters.map(
          (entry) => entry._2.toDBString
        ).mkString(",") + ")"
      }
    ).mkString(";")
  }

  /**
   * Add a precondition to database
   * @author Thomas GIOVANNINI
   * @param precondition precondition to add
   * @return a sql statement
   */
  def add(precondition: Precondition): SimpleSql[Row] = {
    SQL("""
          INSERT INTO preconditions(label, parameters, subconditions)
          VALUES({label}, {parameters}, {subconditions})
        """).on(
      'label -> precondition.label,
      'parameters -> parametersString(precondition),
      'subconditions -> subconditionString(precondition)
    )
  }

  /**
   * Get a preconditions from database
   * @author Thomas GIOVANNINI
   * @param id id of the precondition
   * @return a sql statement
   */
  def getById(id: Long) = {
    SQL("SELECT * from preconditions WHERE id = {id}")
      .on('id -> id)
  }

  /**
   * Update a precondition in database
   * @author Thomas GIOVANNINI
   * @param id id of the precondition
   * @param precondition new precondition with changes
   * @return a sql statement
   */
  def update(id: Long, precondition: Precondition) = {
    SQL("""
          UPDATE preconditions
          SET label = {label}, parameters = {param}, subconditions = {precond}
          WHERE id = {id}
        """).on(
        'id -> id,
        'label -> precondition.label,
        'parameters -> parametersString(precondition),
        'subconditions -> subconditionString(precondition)
      )
  }

  /**
   * Remove a PRECONDITION from database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("DELETE FROM preconditions where id = {id}")
      .on('id -> id)
  }
}