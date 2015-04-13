package models.graph.relation

import controllers.Application
import models.graph.concept.{Concept, ConceptDAO}

import scala.util.{Failure, Success, Try}

/**
 * Distance Access Object for graph relations
 * @author Thomas GIOVANNINI
 */
object RelationGraphDAO {

  implicit val connection = Application.neoConnection

  /**
   * Method to retrieve all existing relations from the database
   * @return the list of all existing relations
   */
  def getAll: List[Relation] = {
    val statement = RelationGraphStatement.getAllRelations
    statement.apply
      .map(Relation.parseRow)
      .toList
  }

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
    val statement = RelationGraphStatement.createRelation(sourceId, relationId, destId)
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
    val statement = RelationGraphStatement.deleteRelation(sourceId, relationId, destId)
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
    val statement = RelationGraphStatement.deleteRelation(relationId)
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

  def getRelationsById(id: Long): List[(Concept, Concept)] = {
    val statement = RelationGraphStatement.getRelationsById(id)
    statement.apply().map(
      row => {
        Try {
          val source = ConceptDAO.getByLabel(row[String]("source_label"))
          val destination = ConceptDAO.getByLabel(row[String]("destination_label"))
          (source, destination)
        } match {
          case Success(result) => result
          case Failure(e) => (Concept.error, Concept.error)
        }
      }
    ).toList
  }

}
