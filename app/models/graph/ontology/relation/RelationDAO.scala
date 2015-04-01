package models.graph.ontology.relation

import anorm.{~, RowParser}
import anorm.SqlParser._
import controllers.Application
import models.graph.NeoDAO
import models.interaction.action.{InstanceAction, InstanceActionDAO}
import play.api.db.DB
import play.api.Play.current

import scala.language.postfixOps

/**
 * Object for connection with the relational database
 */
object RelationDAO {
  implicit val connection = Application.connection

  private val mapping = collection.mutable.Map.empty[Long, Relation]

  /**
   * Parse relation to interact with database
   * @author Aurélie LORGEOUX
   */
  private val relationParser: RowParser[Relation] = {
    get[Long]("id") ~
    get[String]("label") map {
      case id ~ label => Relation(id, label)
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of relations deleted
   */
  def clear: Int = {
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.clearDB
      statement.executeUpdate
    }
  }

  /**
   * Get all relations saved in database
   * @author Aurélie LORGEOUX
   * @return all relations
   */
  def getAll: List[Relation] = {
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.getAll
      statement.as(relationParser *)
    }
  }

  /**
   * Save relation in database
   * @author Aurélie LORGEOUX
   * @param relationName relation to put in the database
   * @return true if the relation saved
   *         false else
   */
  def save(relationName: String): Long = {
    val id = InstanceActionDAO.getByName(relationName).id
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.add(id, relationName)
      val optionId: Option[Long] = statement.executeInsert()
      optionId.getOrElse(-1L)
    }
  }

  /**
   * Get one relation saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return relation identified by id
   */
  def getById(id: Long): Relation = {
    mapping.getOrElse(id, {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.getById(id)
        val relation = statement.as(relationParser.singleOpt).getOrElse(Relation.error)
        if(relation != Relation.error){
          mapping += id -> relation
        }
        relation
      }
    })
  }

  /**
   * Get id of action associated to a relation
   * @author Thomas GIOVANNINI
   * @param id id of the relation
   * @return if of action
   */
  def getActionIdFromRelationId(id: Long): Long = {
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.getActionId(id)
      val optionResult = statement.apply.map(row => row[Long]("actionId")).headOption
      val result = optionResult.getOrElse(-1L)
      result
    }
  }

  /**
   * Get action associated to a relation
   * @author Thomas GIOVANNINI
   * @param id id of the relation
   * @return if of action
   */
  def getActionFromRelationId(id: Long): InstanceAction = {
    val actionID = getActionIdFromRelationId(id)
    InstanceActionDAO.getById(actionID)
  }

  /**
   * Get one relation saved in database with its id
   * @author Aurélie LORGEOUX
   * @param name of the relation
   * @return relation identified by id
   */
  def getByName(name: String): Relation = {
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.getByName(name)
      statement.as(relationParser.singleOpt).getOrElse(Relation.error)
    }
  }

  /**
   * Update a relation in database
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @param relationName to update
   */
  def update(id: Long, relationName: String): Int = {
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.rename(id, relationName)
      statement.executeUpdate
    }
  }

  /**
   * Delete a relation in the Neo4J graph
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   */
  def delete(id: Long): Int = {
    NeoDAO.removeTypeRelationFromDB(id)
    DB.withConnection { implicit connection =>
      val statement = RelationSQLStatement.remove(id)
      statement.executeUpdate
    }
  }
}
