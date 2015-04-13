package models.interaction

import anorm._
import models.interaction.action.InstanceAction

/**
 * All values from this objects are SQLStatements
 */
object InteractionStatement {

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

  /**
   * Request to select all the effects in the rules table
   * @author Julien PRADET
   * @return a sql statement
   */
  def getAllEffects = {
    SQL("SELECT * FROM rules WHERE label LIKE 'EFFECT_%'")
  }



  private def parametersString(interaction: Interaction): String = {
    interaction.parameters.map(_.toString).mkString(",")
  }

  private def precondString(interaction: Interaction): String = interaction match {
    case action: InstanceAction =>
      action.preconditions.map(precondition => {
        precondition._1.id + " (" + precondition._2.unzip._2.map(_.toDBString).mkString(",") + ")"
      }).mkString(";")
    case _ => ""
  }

  private def contentString(interaction: Interaction): String = {
    interaction.subInteractions.map(action => {
      action._1.id + " (" + action._2.unzip._2.map(_.toDBString).mkString(",")+")"
    }).mkString(";")
  }

  /**
   * Add a rule to database
   * @author Aurélie LORGEOUX
   * @param interaction rule to add
   * @return a sql statement
   */
  def add(interaction: Interaction) = {
    SQL("""
            INSERT INTO rules(label, param, precond, content)
            VALUES({label}, {param}, {precond}, {content})
    """).on(
      'label -> interaction.label,
      'param -> parametersString(interaction),
      'precond -> precondString(interaction),
      'content -> contentString(interaction)
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
   * @param interaction new rule with changes
   * @return a sql statement
   */
  def set(id: Long, interaction: Interaction) = {
    SQL("""
      UPDATE rules SET
      label = {label},
      param = {param},
      precond = {precond},
      content = {content}
      WHERE id = {id}
        """).on(
        'id -> id,
        'label -> interaction.label,
        'param -> parametersString(interaction),
        'precond -> precondString(interaction),
        'content -> contentString(interaction)
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