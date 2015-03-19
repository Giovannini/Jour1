package models.graph.ontology.concept

import models.graph.NeoDAO
import models.graph.custom_types.DisplayProperty
import models.graph.ontology.ValuedProperty
import models.graph.ontology.property.{Property, PropertyDAO}
import models.graph.ontology.relation.Relation
import org.anormcypher.CypherResultRow

/**
 * Distance Access Object for accessing Concept objects in Neo4J DB
 */
object ConceptDAO {

  /**
   * Read a Neo4J cypher result row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRow(row: CypherResultRow): Concept = {
    val label = row[String]("concept_label")
    val properties = row[Seq[Long]]("concept_prop").map(PropertyDAO.getById).toList
    val rulesProperty = ValuedProperty.rowToPropertiesList(row, "concept_rules")
    val display = DisplayProperty.parseString(row[String]("concept_display"))
    Concept(label, properties, rulesProperty, display)
  }

  /**
   * Method to add a property to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param property to be added
   * @return the concept with the given property added
   */
  def addPropertyToConcept(concept: Concept, property: Property): Concept = {
    NeoDAO.addPropertyToConcept(concept, property)
    Concept(concept.label, property :: concept.properties, concept.rules, DisplayProperty())
  }

  /**
   * Method to remove a property from a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be removed
   * @param property to be removed
   * @return the concept without the given property
   */
  def removePropertyFromConcept(concept: Concept, property: Property): Concept = {
    if (concept.properties.contains(property)) {
      NeoDAO.removePropertyFromConcept(concept, property)
      Concept(concept.label, concept.properties diff List(property), concept.rules, DisplayProperty())
    } else concept
  }

  /**
   * Method to add a rule to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param rule to be added
   * @return the concept with the given property added
   */
  def addRuleToConcept(concept: Concept, rule: ValuedProperty): Concept = {
    NeoDAO.addRuleToConcept(concept, rule)
    Concept(concept.label, concept.properties, rule :: concept.rules, DisplayProperty())
  }

  /**
   * Method to remove a rule from a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be removed
   * @param rule to be removed
   * @return the concept without the given rule
   */
  def removeRuleFromConcept(concept: Concept, rule: ValuedProperty): Concept = {
    if (concept.rules.contains(rule)) {
      NeoDAO.removeRuleFromConcept(concept, rule)
      Concept(concept.label, concept.properties, concept.rules diff List(rule), DisplayProperty())
    } else concept
  }

  /**
   * Method to get a concept from the graph by its ID
   * @author Julien Pradet
   * @param conceptId the ID of the desired concept
   * @return the desired concept if exists
   */
  def getById(conceptId: Long): Concept = {
    NeoDAO.getConceptById(conceptId)
  }

  /**
   * Method to get a concept from the graph by its label
   * @author Aurélie LORGEOUX
   * @param label the label of the desired concept
   * @return the desired concept if exists
   */
  def getByLabel(label: String): Concept = {
    NeoDAO.getConceptByLabel(label)
  }

  /**
   * Method to get all the concepts in the graph database
   * @author Julien Pradet
   * @return a list of concepts
   */
  def findAll: List[Concept] = {
    NeoDAO.findAllConcepts()
  }

  /**
   * Method to retrieve all the parents of a given concept
   * @author Julien Pradet
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getParents(conceptId: Long): List[Concept] = {
    NeoDAO.findParentConcepts(conceptId)
  }

  /**
   * Method to retrieve all the children of a given concept
   * @author Julien Pradet
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getChildren(conceptId: Long): List[Concept] = {
    NeoDAO.findChildrenConcepts(conceptId)
  }

  /**
   * Get all the child node of a concept recursively
   * @author Thomas GIOVANNINI
   * @param conceptId of the desired concept
   * @return a list of concepts which are the given concept's descendance
   */
  def getDescendance(conceptId: Long): List[Concept] = {
    getChildren(conceptId).flatMap(concept => concept :: getDescendance(concept.id))
  }

  /**
   * Get all the relations from a given source concept
   * @author Julien Pradet
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsFrom(conceptId: Long): List[(Relation, Concept)] = {
    NeoDAO.getRelationsFrom(conceptId)
  }

  /**
   * Get all the relations to a given destination concept
   * @author Julien Pradet
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsTo(conceptId: Long): List[(Relation, Concept)] = {
    NeoDAO.getRelationsTo(conceptId)
  }

  /**
   * Get all the relations given a concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the concept
   * @return (relations from, relations to)
   */
  def getRelationsFromAndTo(conceptId: Long): (List[(Relation, Concept)], List[(Relation, Concept)]) = {
    (getRelationsFrom(conceptId), getRelationsTo(conceptId))
  }

  /**
   * Method to know if a row represents an instance or not
   * @author Thomas GIOVANNINI
   * @param row to test
   * @return true if the row doesn't represent an instance
   *         false else
   */
  def noInstance(row: CypherResultRow): Boolean = {
    row[String]("node_type") != "INSTANCE"
  }

  /**
   * Method to retrieve all the possible actions for a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getReachableRelations(conceptId: Long): List[(Relation, Concept)] = {
    val conceptRelations = getRelationsFrom(conceptId) /*.filter(notASubtype)*/
    val parentsRelations = getParentsRelations(conceptId)
    conceptRelations ::: parentsRelations
  }

  /**
   * Get the relations of parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the concept from which the parent relations are desired
   * @return a list of relations and concepts
   */
  def getParentsRelations(conceptId: Long): List[(Relation, Concept)] = {
    getParents(conceptId).flatMap {
      parent => getReachableRelations(parent.id)
    }
  }
}
