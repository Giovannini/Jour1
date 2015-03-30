package models.graph.ontology.concept

import models.graph.NeoDAO
import models.graph.custom_types.{DisplayProperty, Statement}
import models.graph.ontology.ValuedProperty
import models.graph.ontology.concept.need.NeedDAO
import models.graph.ontology.property.Property
import models.graph.ontology.relation.Relation
import org.anormcypher.CypherResultRow

import scala.util.{Failure, Success, Try}

/**
 * Distance Access Object for accessing Concept objects in Neo4J DB
 */
object ConceptDAO {

  implicit val connection = NeoDAO.connection

  /**
   * Read a Neo4J cypher result row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRow(row: CypherResultRow): Concept = {
    Try {
      val label = row[String]("concept_label")
      val properties = row[Seq[String]]("concept_prop").map(Property.parseString).toList
      val rulesProperty = row[Seq[String]]("concept_rules").map(ValuedProperty.parse).toList
      val needs = row[Seq[String]]("concept_needs").map(id => NeedDAO.getById(id.toLong)).toList
      val display = DisplayProperty.parseString(row[String]("concept_display"))
      Concept(label, properties, rulesProperty, needs, display)
    } match {
      case Success(concept) => concept
      case Failure(e) =>
        println("Error while parsing row for a concept.")
        println(e)
        Concept.error
    }
  }

  def parseSimplifiedRow(row: CypherResultRow): Concept = {
    Try {
      val label = row[String]("concept_label")
      val display = DisplayProperty.parseString(row[String]("concept_display"))
      Concept(label, List(), List(), List(), display)
    } match {
      case Success(concept) => concept
      case Failure(e) =>
        println("Error while parsing row for a concept.")
        println(e)
        Concept.error
    }
  }

  /**
   * Get a concept from its id
   * @author Thomas GIOVANNINI
   * @param conceptId concept id
   * @return concept if it exists, error otherwise
   */
  def getById(conceptId: Long): Concept = {
    val cypherResultRowStream = Statement.getConceptById(conceptId)
      .apply
    if (cypherResultRowStream.nonEmpty) {
      ConceptDAO.parseRow(cypherResultRowStream.head)
    } else Concept.error
  }

  /**
   * Get a concept from its label
   * @author Thomas GIOVANNINI
   * @param label concept's label
   * @return concept if it exists, error otherwise
   */
  def getByLabel(label: String): Concept = {
    val cypherResultRowStream = Statement.getConceptByLabel(label)
      .apply
    if (cypherResultRowStream.nonEmpty) {
      ConceptDAO.parseRow(cypherResultRowStream.head)
    } else Concept.error
  }

  /**
   * Get all the concepts existing in the db
   * @author Thomas GIOVANNINI
   * @return a list of the existing concepts
   */
  def getAll: List[Concept] = {
    Statement.getAllConcepts.apply()
      .map(ConceptDAO.parseRow)
      .toList
  }

  def getAllSimlified: List[Concept] = {
    Statement.getAllConcepts.apply()
      .map(ConceptDAO.parseSimplifiedRow)
      .toList
  }

  /*########################
      Basic DB transactions
   ########################*/
  
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
    getById(concept.id) == Concept.error &&
      Statement.createConcept(concept).execute()
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
    println(statement.toString)
    println()
    val cypherResultRowStream = statement.apply
    if(cypherResultRowStream.nonEmpty) {
      ConceptDAO.parseRow(cypherResultRowStream.head)
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

  /*########################
      Relations
   ########################*/

  /**
   * Get all the relations from a given source concept
   * @author Julien Pradet
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsFrom(conceptId: Long): List[(Relation, Concept)] = {
    Statement.getRelationsFrom(conceptId)
      .apply
      .filter(ConceptDAO.noInstance)
      .map(row => (Relation.DBGraph.parseRow(row), ConceptDAO.parseRow(row)))
      .toList
  }

  /**
   * Get all the relations to a given destination concept
   * @author Julien Pradet
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsTo(conceptId: Long): List[(Relation, Concept)] = {
    Statement.getRelationsTo(conceptId)
      .apply
      .toList
      .filter(ConceptDAO.noInstance)
      .map { row => (Relation.DBGraph.parseRow(row), ConceptDAO.parseRow(row))}
  }

  /**
   * Get all the parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId concept id of the concept child
   * @return a list of the parents of the concept
   */
  def getParents(conceptId: Long): List[Concept] = {
    val statement = Statement.getParentConcepts(conceptId)
    statement.apply
      .map(ConceptDAO.parseRow)
      .toList
  }

  /**
   * Method to retrieve all the children of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getChildren(conceptId: Long): List[Concept] = {
    val statement = Statement.getChildrenConcepts(conceptId)
    statement.apply
      .map(ConceptDAO.parseRow)
      .toList
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
   * Get all the relations given a concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the concept
   * @return (relations from, relations to)
   */
  def getRelationsFromAndTo(conceptId: Long): (List[(Relation, Concept)], List[(Relation, Concept)]) = {
    (getRelationsFrom(conceptId), getRelationsTo(conceptId))
  }

  /*########################
      Properties
   ########################*/
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

  /*########################
      Rules
   ########################*/
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

  /*########################
      Actions
   ########################*/

  /**
   * Method to retrieve all the possible actions for a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getReachableRelations(conceptId: Long): List[(Relation, Concept)] = {
    val conceptRelations = getRelationsFrom(conceptId)
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

  /*########################
      Predicates
   ########################*/

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
}
