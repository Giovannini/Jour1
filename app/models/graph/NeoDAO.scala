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

  /* Methods to get informations from a concept */
  /**
   * Get a concept from its id
   * @author Thomas GIOVANNINI
   * @param conceptId concept id
   * @return concept if it exists, error otherwise
   */
  def getConceptById(conceptId: Long): Concept = {
    val statement = Statement.getConceptById(conceptId)
    val cypherResultRowStream = statement.apply
    if (cypherResultRowStream.nonEmpty) {
      val row = statement.apply.head
      Concept.parseRow(row)
    } else Concept.error
  }

  /**
   * Get a concept from its label
   * @author Aurélie LORGEOUX
   * @param label concept label
   * @return concept if it exists, error otherwise
   */
  def getConceptByLabel(label: String): Concept = {
    val statement = Statement.getConceptByLabel(label)
    val cypherResultRowStream = statement.apply
    if (cypherResultRowStream.nonEmpty) {
      val row = statement.apply.head
      Concept.parseRow(row)
    } else Concept.error
  }

  /**
   * Get all the concepts existing in the db
   * @author Thomas GIOVANNINI
   * @return a list of the existing concepts
   */
  def findAllConcepts(): List[Concept] = {
    Statement.getAllConcepts.apply()
      .map(Concept.parseRow)
      .toList
  }

  /**
   * Get all the parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId concept id of the concept child
   * @return a list of the parents of the concept
   */
  def findParentConcepts(conceptId: Long): List[Concept] = {
    val statement = Statement.getParentConcepts(conceptId)
    statement.apply
      .map(Concept.parseRow)
      .toList
  }

  /**
   * Method to retrieve all the children of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def findChildrenConcepts(conceptId: Long): List[Concept] = {
    val statement = Statement.getChildrenConcepts(conceptId)
    statement.apply
      .map(Concept.parseRow)
      .toList
  }

  /**
   * Get all the relations from a given source concept
   * @author Julien Pradet
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsFrom(conceptId: Long): List[(Relation, Concept)] = {
    Statement.getRelationsFrom(conceptId).apply
      .filter(Concept.noInstance)
      .map(row => (Relation.DBGraph.parseRow(row), Concept.parseRow(row)))
      .toList
  }

  /**
   * Get all the relations to a given destination concept
   * @author Julien Pradet
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsTo(conceptId: Long): List[(Relation, Concept)] = {
    Statement.getRelationsTo(conceptId).apply
      .toList
      .filter(Concept.noInstance)
      .map { row => (Relation.DBGraph.parseRow(row), Concept.parseRow(row))}
  }

  /**
   * Add a concept into the DB.
   * @author Thomas GIOVANNINI
   * @author Julien Pradet
   * @param concept concept to write into the DB
   * @return true if the concept was correctly added
   *         false else
   *
   * Edit JP : The function now checks if the concept already exists
   */
  def addConceptToDB(concept: Concept): Boolean = {
    if(getConceptById(concept.id) == Concept.error) {
      val statement = Statement.createConcept(concept)
      statement.execute()
    } else false
  }

  /**
   * Update a concept with a full set of new properties
   * @author Julien PRADET
   * @param originalConcept the concept that is meant to be changed
   * @param concept the new concept
   * @return the new concept as it exists in the db
   */
  def updateConcept(originalConcept: Concept, concept: Concept): Concept = {
    val statement = Statement.updateConcept(originalConcept, concept)
    val cypherResultRowStream = statement.apply
    if(cypherResultRowStream.nonEmpty) {
      Concept.parseRow(cypherResultRowStream.head)
    } else {
      Concept.error
    }
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
