package models.graph.ontology.concept.need

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.graph.ontology.concept.consequence.ConsequenceStep
import models.graph.ontology.property.Property
import models.instance_action.action.InstanceAction

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
      get[String]("property") ~
      get[Double]("priority") ~
      get[String]("consequencesSteps") ~
      get[String]("meansOfSatisfaction") map {
      case id ~ label ~ propertyToParse ~ priority ~ consequencesStepsToParse ~ meansOfSatisfactionToParse =>
        val property = Property.parseString(propertyToParse)
        val consequencesSteps = ConsequenceStep.parseList(consequencesStepsToParse)
        val meansOfSatisfaction = InstanceAction.retrieveFromStringOfIds(meansOfSatisfactionToParse)
        Need(id, label, property, priority, consequencesSteps, meansOfSatisfaction)
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
