package models.graph.ontology.concept.need

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.graph.ontology.concept.consequence.ConsequenceStep

import scala.language.postfixOps

/**
 * Distance Access Object for consequences DB
 */
object NeedDAO {

  implicit val connection = Application.connection

  /**
   * Parse property to interact with database
   * @author Thomas GIOVANNINI
   */
  private val needParser: RowParser[Need] = {
    get[Long]("id") ~
      get[String]("label") ~
      get[String]("consequencesSteps") map {
      case id ~ label ~ consequencesSteps =>
        Need(id, label, ConsequenceStep.parseList(consequencesSteps))
    }
  }

  def clear: Boolean = {
    val statement = NeedStatement.clearDB
    statement.execute()
  }

  def getAll: List[Need] = {
    val statement = NeedStatement.getAll
    statement.as(needParser *)
  }

  def getById(id: Long): Need = {
    val statement = NeedStatement.getById(id)
    statement.as(needParser.singleOpt).getOrElse(Need.error)
  }

  def save(consequence: Need): Boolean = {
    val statement = NeedStatement.add(consequence)
    statement.execute()
  }

  def delete(id: Long): Boolean = {
    val statement = NeedStatement.remove(id)
    statement.execute()
  }
}
