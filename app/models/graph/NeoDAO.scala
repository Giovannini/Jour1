package models.graph

import models.graph.custom_types.Statement
import models.graph.ontology._
import models.graph.ontology.property.Property
import models.graph.ontology.relation.Relation
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

  /**
   * Add a concept into the DB.
   * @author Thomas GIOVANNINI
   * @param concept concept to write into the DB
   * @return true if the concept was correctly added
   *         false else
   */
  def addConceptToDB(concept: Concept): Boolean = {
    val statement = Statement.createConcept(concept)
    statement.execute()
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
   * Remove a relation into two existing concepts in the Neo4J DB.
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

  def updateRelationInDB(sourceId: Long, oldRelation: Relation, newRelation: Relation, destId: Long) = {
    val statement = Statement.updateRelation(sourceId, oldRelation, newRelation, destId)
    statement.execute
  }

}
