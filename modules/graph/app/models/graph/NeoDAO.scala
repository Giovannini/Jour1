package models.graph

import models.graph.custom_types.Statement
import models.graph.ontology.{Instance, Relation, Concept}
import org.anormcypher._


/**
 * Model for the NeoDAO class.
 */
object NeoDAO {

  // Setup the Rest Client
  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  /**
   * Add a concept into the DB.
   * @author Thomas GIOVANNINI
   * @param concept concept to write into the DB
   * @return true if the concept was correctly added
   *         false else
   */
  def addConceptToDB(concept: Concept): Boolean = {
    val statement = Statement.createConcept(concept)
    statement.execute
  }

  /**
   * Remove a concept from the DB.
   * @author Thomas GIOVANNINI
   * @param concept to remove
   * @return true if the concept was correctly removed
   *         false else
   */
  def removeConceptFromDB(concept: Concept): Boolean = {
    val statement = Statement.deleteConcept(concept.id)
    statement.execute
  }

  /**
   * Create a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to add, containing the source concept, the relation name and the destination concept
   * @return true if the relation was correctly added
   *         false else
   */
  def addRelationToDB(sourceId: Int, relation: Relation, destId: Int): Boolean = {
    val statement = Statement.createRelation(sourceId, relation, destId)
    statement.execute
  }

  /**
   * Remove a relation into two existing concepts in the Neo4J DB.
   * @param sourceId the source of the link
   * @param relation the name of the relation
   * @param destId the destination of the link
   * @return true if the relation was correctly removed
   *         false else
   */
  def removeRelationFromDB(sourceId: Int, relation: Relation, destId: Int): Boolean = {
    val statement = Statement.deleteRelation(sourceId, relation, destId)
    statement.execute
  }

  /**
   * Method to add an instance of a given concept (doing the relation) to the Neo4J Graph
   * @author Thomas GIOVANNINI
   * @param instance to add to the graph
   * @return true if the instance was correctly added
   *         false else
   */
  def addInstance(instance: Instance): Boolean = {
    val statement = Statement.createInstance(instance)
    val result = Concept.getById(instance.concept.id).isDefined && statement.execute
    if (result) addRelationToDB(instance.hashCode, Relation("INSTANCE_OF"), instance.concept.id)
    else result
  }

  /**
   * Method to remove an instance from the graph
   * @author Thomas GIOVANNINI
   * @param instance to remove
   * @return true if the instance was correctly removed
   *         false else
   */
  def removeInstance(instance: Instance): Boolean = {
    val statement = Statement.deleteInstances(instance)
    statement.execute
  }

  /**
   * Method to update an instance from the graph
   * @author Thomas GIOVANNINI
   * @param instance the updated instance to change in the graph.
   * @return true if the instance was correctly updated
   *         false else
   */
  def updateInstance(instance: Instance): Boolean = {
    removeInstance(instance) && addInstance(instance)
  }

}
