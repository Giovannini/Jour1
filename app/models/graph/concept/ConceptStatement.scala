package models.graph.concept

import models.graph.property.{ValuedProperty, Property}
import org.anormcypher.{Cypher, CypherStatement}

/**
 * All values from this objects are CypherStatements used in the Neo4J database
 */
object ConceptStatement {
  /**
   * Statement returning all the concepts in the graph database.
   * @author Thomas GIOVANNINI
   */
  val getAllConcepts: CypherStatement = Cypher(
    """
    MATCH n
    WHERE n.type = "CONCEPT"
    RETURN  n.id as concept_id
    """
  )


  /**
   * Statement to clear the whole graph database.
   * @author Thomas GIOVANNINI
   */
  val clearDB: CypherStatement = Cypher(
    """
      |MATCH (n)
      |OPTIONAL MATCH (n)-[r]-()
      |DELETE n,r
    """.stripMargin)

  /**
   * Generate a cypher statement to create a concept.
   * @author Thomas GIOVANNINI
   * @param concept to create
   * @return a cypher statement
   */
  def createConcept(concept: Concept): CypherStatement = {
    val nodeToCreate = concept.toNodeString
    Cypher("CREATE " + nodeToCreate)
  }

  def updateConcept(originalConcept: Concept, concept: Concept): CypherStatement = {
    val nodeToUpdate = concept.toNodePropertiesString
    /* WARNING : If it's switch to Cyper().on(...) it fails - be sure to find a way to fix it before doing so */
    Cypher("""
             |MATCH (n {id: """.stripMargin + originalConcept.id.toString + """})
             |SET n = """.stripMargin + nodeToUpdate + """
             |RETURN  n.label as concept_label,
             |        n.properties as concept_prop,
             |        n.rules as concept_rules,
             |        n.needs as concept_needs,
             |        n.display as concept_display
           """.stripMargin)
  }

  /**
   * Generate a cypher statement to delete a concept and all its connected relations.
   * @author Thomas GIOVANNINI
   * @param conceptId the concept to remove
   * @return a cypher statement to execute
   */
  def deleteConcept(conceptId: Long): CypherStatement = {
    Cypher("MATCH (n {id: {id1} }) " +
      "OPTIONAL MATCH (n)-[r]-() " +
      "OPTIONAL MATCH (n)-[r2]-(n2 {type: {type}}) " +
      "DELETE n, r, r2, n2;")
      .on("id1" -> conceptId, "type" -> "INSTANCE")
  }

  /**
   * Generate a statement to add a new property to a concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property should be added
   * @param property to add to the concept
   * @return a cypher statement
   */
  def addPropertyToConcept(concept: Concept, property: Property): CypherStatement = {
    val newPropertiesList = property :: concept.properties
    Cypher("MATCH (n {id: {id1} }) " +
      "SET n.properties = ["+newPropertiesList.mkString(",")+"]")
      .on("id1" -> concept.id)
  }

  /**
   * Create a cypher statement to add a new property to a concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property should be added
   * @param property to add to the concept
   * @return a cypher statement
   */
  def removePropertyFromConcept(concept: Concept, property: Property): CypherStatement = {
    val newPropertiesList = concept.properties diff List(property)
    Cypher("MATCH (n {id: {id1} }) " +
      "SET n.properties = ["+newPropertiesList.mkString(",")+"]")
      .on("id1" -> concept.id)
  }

  /**
   * Create a cypher statement to add a new rule to a concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property should be added
   * @param rule to add to the concept
   * @return a cypher statement
   */
  def addRuleToConcept(concept: Concept, rule: ValuedProperty): CypherStatement =   {
    val newRulesList = rule :: concept.properties
    Cypher("MATCH (n {id: {id1}}) " +
      "SET n.rules = ["+newRulesList.mkString(",")+"]")
      .on("id1" -> concept.id)
  }

  /**
   * Create a cypher statement to remove a given rule from a concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property should be removed
   * @param rule to remove from the concept
   * @return a cypher statement
   */
  def removeRuleFromConcept(concept: Concept, rule: ValuedProperty): CypherStatement =   {
    val newRulesList = concept.properties diff List(rule)
    Cypher("MATCH (n {id: {id1}}) " +
      "SET n.rules = ["+newRulesList.mkString(",")+"]")
      .on("id1" -> concept.id)
  }

  /**
   * Create a cypher statement returning a concept from its id.
   * @author Thomas GIOVANNINI
   * @param conceptId the id of the concept to get
   * @return a cypher statement to execute
   */
  def getConceptById(conceptId: Long): CypherStatement = {
    Cypher("""
            |MATCH (n {id: {id1} })
            |RETURN n.label as concept_label,
            |       n.properties as concept_prop,
            |       n.rules as concept_rules,
            |       n.needs as concept_needs,
            |       n.display as concept_display
           """.stripMargin)
      .on("id1" -> conceptId)
  }

  /**
   * Create a cypher statement returning a concept from its label
   * @author Thomas GIOVANNINI
   * @param label lbal of the concept to get
   * @return a cypher statement to execute
   */
  def getConceptByLabel(label: String): CypherStatement = {
    Cypher("""
             |MATCH (n {label: {label1}})
             |RETURN n.label as concept_label,
             |       n.properties as concept_prop,
             |       n.rules as concept_rules,
             |       n.needs as concept_needs,
             |       n.display as concept_display
           """.stripMargin)
      .on("label1" -> label)
  }
}
