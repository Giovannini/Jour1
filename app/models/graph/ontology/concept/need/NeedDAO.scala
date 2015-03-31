package models.graph.ontology.concept.need

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.graph.ontology.concept.consequence.ConsequenceStep
import models.graph.ontology.property.Property
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps

/**
 * Distance Access Object for consequences DB
 */
object NeedDAO {

  implicit val connection = Application.connection

  private var mapping = collection.mutable.Map.empty[Long, Need]

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
        val meansOfSatisfaction = MeanOfSatisfaction.parseList(meansOfSatisfactionToParse)
        Need(id, label, property, priority, consequencesSteps, meansOfSatisfaction)
    }
  }

  def clear: Boolean = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.clearDB
      statement.execute()
    }
  }

  def getAll: List[Need] = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.getAll
      statement.as(needParser *)
    }
  }

  def getById(id: Long): Need = {
    mapping.getOrElse(id, {
      DB.withConnection { implicit connection =>
        val statement = NeedStatement.getById(id)
        val need = statement.as(needParser.singleOpt).getOrElse(Need.error)
        mapping += id -> need
        need
      }
    })
  }

  def save(need: Need): Need = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.add(need)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      need.withId(id)
    }
  }

  def delete(id: Long): Boolean = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.remove(id)
      statement.execute()
    }
  }
}
