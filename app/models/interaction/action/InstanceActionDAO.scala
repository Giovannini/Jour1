package models.interaction.action

import anorm.SqlParser._
import anorm.{RowParser, ~}
import controllers.Application
import models.interaction.effect.Effect
import models.interaction.{InteractionDAO, InteractionStatement}
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Distance Access Object for instance's actions
 */
object InstanceActionDAO {

  implicit val connection = Application.connection

  private var mappingId = collection.mutable.Map.empty[Long, InstanceAction]
  private var mappingName = collection.mutable.Map.empty[String, InstanceAction]

  /**
   * Parse an action of an instance from database
   * @author Thomas GIOVANNINI
   */
  private val actionParser: RowParser[InstanceAction] = {
    get[Long]("id") ~
      get[String]("label") ~
      get[String]("param") ~
      get[String]("precond") ~
      get[String]("content") map {
      case id ~ label ~ param ~ precond ~ content => InstanceAction.parse(id, label, param, precond, content)
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clear(): Boolean = {
    clearCache()
    InteractionDAO.clearDB()
  }

  def clearCache(): Unit = {
    mappingId = mappingId.empty
    mappingName = mappingName.empty
  }

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def getAll: List[InstanceAction] = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.getAll
      statement.as(actionParser *)
    }
  }

  /**
   * Get all the effects in the rules table
   * @author Julien PRADET
   * @return all effects
   */
  def getAllEffects: List[Effect] = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.getAllEffects
      statement.as(actionParser *).map(_.toEffect)
    }
  }

  /**
   * Save rule in database
   * @author Aurélie LORGEOUX
   * @param action rule to put in the database
   * @return true if the rule saved
   *         false else
   */
  def save(action: InstanceAction): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.add(action)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      action.withId(id)
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def getById(id: Long): InstanceAction = {
    mappingId.getOrElse(id, {
      val action = DB.withConnection { implicit connection =>
        val statement = InteractionStatement.get(id)
        Try {
          statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
        } match {
          case Failure(e) =>
            Console.println("Error while trying to retrieve an interaction by its ID from the DB:")
            Console.println(e.getStackTrace)
            InstanceAction.error
          case Success(instanceAction) =>
            instanceAction
        }
      }

      if (action != InstanceAction.error) {
        mappingId += id -> action
        mappingName += action.label -> action
      }

      action
    })
  }

  /**
   * Get one rule saved in database with its name
   * @author Thomas GIOVANNINI
   * @param name of the rule
   * @return rule identified by id
   */
  def getByName(name: String): InstanceAction = {
    mappingName.getOrElse(name, {
      DB.withConnection { implicit connection =>
        val statement = InteractionStatement.getByName(name)

        val action = Try {
          statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
        } match {
          case Success(a) => a
          case Failure(e) =>
            Console.println("Error while trying to retrieve an interaction by its name from the DB:")
            Console.println(e.getStackTrace)
            InstanceAction.error
        }

        if(action != InstanceAction.error) {
          mappingId += action.id -> action
          mappingName += name -> action
        }
        action
      }
    })
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param action rule identified by id
   */
  def update(id: Long, action: InstanceAction): Int = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.set(id, action)
      statement.executeUpdate
    }
  }

  /**
   * Delete a rule in database
   * @author Thomas GIOVANNINI
   * @param id id of the rule
   */
  def delete(id: Long): Int = InteractionDAO.delete(id)


}
