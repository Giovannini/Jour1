package models.graph

import models.graph.custom_types.Statement
import models.graph.ontology._
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
   * Add a new property to a concept
   * @author Thomas GIOVANNINI
   * @param concept to update
   * @param property to add to the concept
   * @return true if the property was correctly added
   *         false else
   */
  def addPropertyToConcept(concept: Concept, property: Property): Boolean = {
    val statement = Statement.addPropertyToConcept(concept, property)
    statement.execute
  }

  /**
   * Remove a given property from a concept
   * @author Thomas GIOVANNINI
   * @param concept to update
   * @param property to remove from the concept
   * @return true if the property was correctly removed
   *         false else
   */
  def removePropertyFromConcept(concept: Concept, property: Property):Boolean = {
    val statement = Statement.removePropertyFromConcept(concept, property)
    statement.execute
  }

  /**
   * Add a new rule to a concept
   * @author Thomas GIOVANNINI
   * @param concept to update
   * @param rule to add to the concept
   * @return true if the rule was correctly added
   *         false else
   */
  def addRuleToConcept(concept: Concept, rule: ValuedProperty) = {
    val statement = Statement.addRuleToConcept(concept, rule)
    statement.execute
  }

  /**
   * Remove a rule from a concept
   * @author Thomas GIOVANNINI
   * @param concept to update
   * @param rule to remove from the concept
   * @return true if the rule was correctly removed
   *         false else
   */
  def removeRuleFromConcept(concept: Concept, rule: ValuedProperty) = {
    val statement = Statement.removeRuleFromConcept(concept, rule)
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
    /** TODO
      * Be careful when creating a relation SUBTYPE_OF not to insert loop.
      */
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

  def updateRelationInDB(sourceId: Int, oldRelation: Relation, newRelation: Relation, destId: Int) = {
    val statement = Statement.updateRelation(sourceId, oldRelation, newRelation, destId)
    statement.execute
  }
}
