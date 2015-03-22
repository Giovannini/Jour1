/*
package models.graph.ontology.concept.consequence

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.instance_action.action.InstanceAction

import scala.language.postfixOps

/**
 * Distance Access Object for consequences DB
 */
object ConsequenceDAO {

  implicit val connection = Application.connection

  /**
   * Parse property to interact with database
   * @author Thomas GIOVANNINI
   */
  private val consequenceParser: RowParser[Consequence] = {
    get[Long]("id") ~
      get[String]("label") ~
      get[Long]("severity") ~
      get[String]("effects") map {
      case id ~ label ~ severity ~ effectsIDs =>
        val effects: List[InstanceAction] = InstanceAction.retrieveFromStringOfIds(effectsIDs)
        Consequence(id, label, severity, effects)
    }
  }

  def clear: Boolean = {
    val statement = ConsequenceStatement.clearDB
    statement.execute()
  }

  def getAll: List[Consequence] = {
    val statement = ConsequenceStatement.getAll
    statement.as(consequenceParser *)
  }

  def getById(id: Long): Consequence = {
    val statement = ConsequenceStatement.getById(id)
    statement.as(consequenceParser.singleOpt).getOrElse(Consequence.error)
  }

  def save(consequence: Consequence): Boolean = {
    val statement = ConsequenceStatement.add(consequence)
    statement.execute()
  }

  def delete(id: Long): Boolean = {
    val statement = ConsequenceStatement.remove(id)
    statement.execute()
  }
}
*/
