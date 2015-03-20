package models.graph

import models.graph.custom_types.Statement
import org.anormcypher._
import play.Play


/**
 * Model for the NeoDAO class.
 */
object NeoDAO {
  // Setup the Rest Client
  implicit val connection = Neo4jREST(Play.application.configuration.getString("serverIP"), 7474, "/db/data/")

  def clearDB(): Boolean = {
    val statement = Statement.clearDB
    statement.execute()
  }

  /* Methods to get informations from a concept */

  /**
   * Create a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param sourceId the source of the link
   * @param relationId the relation to add, containing the source concept, the relation name and the destination concept
   * @param destId the destination of the link
   * @return true if the relation was correctly added
   *         false else
   */
  def addRelationToDB(sourceId: Long, relationId: Long, destId: Long): Boolean = {
    /* TODO
      * Be careful when creating a relation SUBTYPE_OF not to insert loop.
      */
    val statement = Statement.createRelation(sourceId, relationId, destId)
    statement.execute
  }

  /**
   * Remove a relation into two existing concepts in the Neo4J DB
   * @author Thomas GIOVANNINI
   * @param sourceId the source of the link
   * @param relationId the id of the relation
   * @param destId the destination of the link
   * @return true if the relation was correctly removed
   *         false else
   */
  def removeRelationFromDB(sourceId: Long, relationId: Long, destId: Long): Boolean = {
    val statement = Statement.deleteRelation(sourceId, relationId, destId)
    statement.execute
  }

  /**
   * Remove a relation everywhere in the Neo4J DB
   * @author Aurélie LORGEOUX
   * @param relationId the id of the relation
   * @return true if the relation was correctly removed
   *         false else
   */
  def removeTypeRelationFromDB(relationId: Long) = {
    val statement = Statement.deleteRelation(relationId)
    statement.execute
  }

  /**
   * Update a relation into two existing concepts in the Neo4J DB
   * @author Aurélie LORGEOUX
   * @param sourceId the source of the link
   * @param oldRelationId id of the relation to delete
   * @param newRelationId id of the relation to add
   * @param destId the destination of the link
   * @return true if the relation was correctly updated
   *         false else
   */
  def updateRelationInDB(sourceId: Long, oldRelationId: Long, newRelationId: Long, destId: Long) = {
    removeRelationFromDB(sourceId, oldRelationId, destId) && addRelationToDB(sourceId, newRelationId, destId)
  }

}
