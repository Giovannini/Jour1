package models.graph.concept

import controllers.Application
import models.graph.DisplayProperty
import models.graph.property.{Property, ValuedProperty}
import models.graph.relation.{Relation, RelationGraphStatement}
import models.intelligence.need.NeedDAO
import org.anormcypher.CypherResultRow

import scala.util.{Failure, Success, Try}

/**
 * Distance Access Object for accessing Concept objects in Neo4J DB
 */
object ConceptDAO {

  implicit val connection = Application.neoConnection

  private var mappingConceptId = collection.mutable.Map.empty[Long, Concept]
  private var mappingConceptRelationsTo = collection.mutable.Map.empty[Long, List[(Relation, Concept)]]
  private var mappingConceptRelationsFrom = collection.mutable.Map.empty[Long, List[(Relation, Concept)]]



  def clear(): Boolean = {
    clearCache()
    val statement = ConceptStatement.clearDB
    statement.execute()
  }

  def clearCache(): Unit = {
    mappingConceptId = mappingConceptId.empty
    mappingConceptRelationsTo = mappingConceptRelationsTo.empty
    mappingConceptRelationsFrom = mappingConceptRelationsFrom.empty
  }

  def removeCacheRelationFromConcept(conceptId: Long) = {
    mappingConceptRelationsFrom.remove(conceptId)
  }

  def removeCacheRelationToConcept(conceptId: Long) = {
    mappingConceptRelationsTo.remove(conceptId)
  }

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
      val properties = row[Seq[String]]("concept_prop").map(ValuedProperty.parse).toList
      val rulesProperty = row[Seq[String]]("concept_rules").map(ValuedProperty.parse).toList
      val needs = row[Seq[String]]("concept_needs").map(id => NeedDAO.getById(id.toLong)).toList
      val display = DisplayProperty.parseString(row[String]("concept_display"))
      Concept(label, properties, rulesProperty, needs, display)
    } match {
      case Success(concept) => concept
      case Failure(e) =>
        Console.println("Error while parsing row for a concept.")
        Console.println(e)
        Concept.error
    }
  }

  def parseSimplifiedRow(row: CypherResultRow): Concept = {
    parseRow(row).simplify
  }

  /**
   * Get a concept from its id
   * @author Thomas GIOVANNINI
   * @param conceptId concept id
   * @return concept if it exists, error otherwise
   */
  def getById(conceptId: Long): Concept = {
    mappingConceptId.getOrElse(conceptId, {
      val cypherResultRowStream = ConceptStatement.getConceptById(conceptId)
        .apply
      if (cypherResultRowStream.nonEmpty) {
        val concept = ConceptDAO.parseRow(cypherResultRowStream.head)
        mappingConceptId += conceptId -> concept
        concept
      } else {
        Concept.error
      }
    })
  }

  /**
   * Get a concept from its label
   * @author Thomas GIOVANNINI
   * @param label concept's label
   * @return concept if it exists, error otherwise
   */
  def getByLabel(label: String): Concept = {
    val cypherResultRowStream = ConceptStatement.getConceptByLabel(label)
      .apply
    if (cypherResultRowStream.nonEmpty) {
      ConceptDAO.parseRow(cypherResultRowStream.head)
    } else {
      Concept.error
    }
  }

  /**
   * Get all the concepts existing in the db
   * @author Thomas GIOVANNINI
   * @return a list of the existing concepts
   */
  def getAll: List[Concept] = {
    ConceptStatement.getAllConcepts.apply()
      .map(row => ConceptDAO.getById(row[Long]("concept_id")))
      .toList
  }

  /*########################
      Basic DB transactions
   ########################*/

  /**
   * Add a concept into the DB if it wasn't in it yet
   * @author Julien Pradet
   * @param concept concept to write into the DB
   * @return true if the concept was correctly added
   *         false else
   */
  def addConceptToDB(concept: Concept): Boolean = {
    getById(concept.id) == Concept.error &&
    ConceptStatement.createConcept(concept).execute()
  }

  /**
   * Update a concept with a full set of new properties
   * @author Julien PRADET
   * @param originalConcept the concept that is meant to be changed
   * @param concept the new concept
   * @return the new concept as it exists in the db
   */
  def updateConcept(originalConcept: Concept, concept: Concept): Concept = {
    val statement = ConceptStatement.updateConcept(originalConcept, concept)
    val cypherResultRowStream = statement.apply
    if (cypherResultRowStream.nonEmpty) {
      val concept = ConceptDAO.parseRow(cypherResultRowStream.head)
      mappingConceptId.remove(concept.id)
      mappingConceptId += concept.id -> concept
      concept
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
    val statement = ConceptStatement.deleteConcept(concept.id)
    statement.execute
  }

  /*########################
      Relations
   ########################*/

  /**
   * Get all the relations from a given source concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsFrom(conceptId: Long): List[(Relation, Concept)] = {
    mappingConceptRelationsFrom.getOrElse(conceptId, {
      val result = RelationGraphStatement.getRelationsFrom(conceptId)
        .apply
        .map { row =>
          val relation = Relation.parseRow(row)
          val concept = ConceptDAO.getById(row[Long]("concept_id"))
          (relation, concept)
        }.toList
      mappingConceptRelationsFrom += conceptId -> result
      result
    })
  }

  /**
   * Get all the relations to a given destination concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsTo(conceptId: Long): List[(Relation, Concept)] = {
    mappingConceptRelationsTo.getOrElse(conceptId, {
      val result = RelationGraphStatement.getRelationsTo(conceptId)
        .apply
        .map { row =>
          val relation = Relation.parseRow(row)
          val concept = ConceptDAO.getById(row[Long]("concept_id"))
          (relation, concept)
        }.toList
      mappingConceptRelationsTo += conceptId -> result
      result
    })
  }

  /**
   * Get all the parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId concept id of the concept child
   * @return a list of the parents of the concept
   */
  def getParents(conceptId: Long): List[Concept] = {
    val statement = RelationGraphStatement.getParentConcepts(conceptId)
    statement.apply
      .map(row => ConceptDAO.getById(row[Long]("concept_id")))
      .toList
  }

  /**
   * Method to retrieve all the children of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getChildren(conceptId: Long): List[Concept] = {
    val statement = RelationGraphStatement.getChildrenConcepts(conceptId)
    statement.apply
      .map(row => ConceptDAO.getById(row[Long]("concept_id")))
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
    val statement = ConceptStatement.addPropertyToConcept(concept, property)
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
  def removePropertyFromConcept(concept: Concept, property: Property): Boolean = {
    val statement = ConceptStatement.removePropertyFromConcept(concept, property)
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
    val statement = ConceptStatement.addRuleToConcept(concept, rule)
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
    val statement = ConceptStatement.removeRuleFromConcept(concept, rule)
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
    val conceptRelations = getRelationsFrom(conceptId)//Slow
    val parentsRelations = ConceptDAO.getById(conceptId)//Slow
      .parents
      .flatMap(getReachableRelations)
    conceptRelations ::: parentsRelations
  }

  /** SUCRE
    * Method to retrieve all the possible actions for a given concept
    * @author Thomas GIOVANNINI
    * @param concept the concept
    * @return a list of relations and concepts
    */
  def getReachableRelations(concept: Concept): List[(Relation, Concept)] = getReachableRelations(concept.id)
}
