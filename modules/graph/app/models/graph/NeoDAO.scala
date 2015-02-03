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
   * Execute a cypher query to write a list of concepts into the DB.
   * @author Thomas GIOVANNINI
   * @param concept concept to write into the DB
   * @return the cypher query
   */
  def addConceptToDB(concept: Concept) =
  {
    val statement = Statement.createConcept(concept)
    statement.execute
  }

  /**
   * Execute a cypher query to remove a concept from the DB.
   * @author Thomas GIOVANNINI
   * @param concept to remove
   */
  def removeConceptFromDB(concept: Concept) = {
    val statement = Statement.deleteConcept(concept.hashCode())
    statement.execute
  }

  /**
   * Create a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to add, containing the source concept, the relation name and the destination concept
   */
  def addRelationToDB(sourceId: Int, relation: Relation, destId: Int) = {
    val statement = Statement.createRelation(sourceId, relation, destId)
    statement.execute()
  }

  /**
   * Remove a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to remove, containing the source concept, the relation name and the destination concept
   */
  def removeRelationFromDB(sourceId: Int, relation: Relation, destId: Int) = {
    val statement = Statement.deleteRelation(sourceId, relation, destId)
    statement.apply()
      .toList
      .foreach(println)
    //if (result) println("Relation deleted") else println("Uh oh...relation not deleted.")
  }

  /**
   * Method to add an instance of a given concept (doing the relation) to the Neo4J Graph
   * @author Thomas GIOVANNINI
   * @param instance to add to the graph
   */
  def addInstance(instance: Instance): Unit = {
    if(instance.isValid) {
      val statement = Statement.createInstance(instance)
      statement.execute
      addRelationToDB(instance.hashCode, Relation("INSTANCE_OF"), instance.concept.hashCode())
    }
  }

  /**
   * Method to remove an instance from the graph
   * @author Thomas GIOVANNINI
   * @param instance to remove
   */
  def removeInstance(instance: Instance): Unit = {
    val statement = Statement.deleteInstances(instance)
    statement.execute
  }

}
