package models.intelligence.need

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.intelligence.MeanOfSatisfaction
import models.intelligence.consequence.ConsequenceStep
import models.graph.property.Property
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
    mapping = mapping.empty
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

  def getNeedsWhereConceptUsed(conceptId: Long): List[Need] = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.getNeedsWhereConceptUsed(conceptId)
      statement.as(needParser *)
    }
  }

  def save(need: Need): Need = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.add(need)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      need.withId(id)
    }
  }

  def update(needId: Long, need: Need): Need = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.update(needId, need)
      val numberRowUpdated = statement.executeUpdate()
      if(numberRowUpdated < 1) {
        Need.error
      } else {
        val newNeed = NeedDAO.getById(needId)
        mapping.remove(needId)
        mapping += needId -> newNeed
        newNeed
      }
    }
  }

  def updateNeedsUsingConcept(oldId: Long, newId: Long): Unit = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.updateNeedsUsingConcept(oldId, newId)
      val numberRowUpdated = statement.executeUpdate()
    }
  }

  def delete(id: Long): Boolean = {
    DB.withConnection { implicit connection =>
      val statement = NeedStatement.remove(id)
      statement.execute()
    }
  }
}
