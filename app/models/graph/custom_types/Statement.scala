package models.graph.custom_types

import models.graph.ontology._
import models.graph.ontology.property.Property
import org.anormcypher.{Cypher, CypherStatement}

/**
 * All values from this objects are CypherStatements used in the Neo4J database
 */
object Statement {
  /**
   * Statement returning all the concepts in the graph database.
   * @author Thomas GIOVANNINI
   */
  val getAllConcepts: CypherStatement = Cypher(
    """
    MATCH n
    WHERE n.type = "CONCEPT"
    RETURN n.label as concept_label, n.properties as concept_prop, n.rules as concept_rules, n.display as concept_display
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

  /**
   * Generate a cypher statement to delete a concept and all its connected relations.
   * @author Thomas GIOVANNINI
   * @param conceptId the concept to remove
   * @return a cypher statement to execute
   */
  def deleteConcept(conceptId: Int): CypherStatement = {
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
  def getConceptById(conceptId: Int): CypherStatement = {
    Cypher("MATCH (n {id: {id1} }) " +
      "RETURN n.label as concept_label, n.properties as concept_prop, n.rules as concept_rules, n.display as concept_display")
      .on("id1" -> conceptId)
  }

  /**
   * Create a cypher statement to create a relation
   * @author Thomas GIOVANNINI
   * @param sourceNodeId the source of the link
   * @param relation the name of the link
   * @param destNodeId the destimation of the linkl
   * @return a cypher statement to execute
   */
  def createRelation(sourceNodeId: Int, relation: Relation, destNodeId: Int) : CypherStatement = {
    Cypher("MATCH (n1 {id: {id1}}), (n2 {id: {id2}})\nCREATE (n1)-[r:"+relation.label+"]->(n2)")
      .on("id1" -> sourceNodeId,
          "id2" -> destNodeId)
  }

  /**
   * Create a cypher statement to delete a relation in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param sourceNodeId the source concept of the relation to delete
   * @param relation the relation to delete
   * @param destNodeId the destination concept of the relation to delete
   * @return a cypher statement deleting the desired relation
   */
  def deleteRelation(sourceNodeId: Int, relation: Relation, destNodeId: Int) : CypherStatement = {
    Cypher( "MATCH (n1 {id: {id1}})-[r:"+relation.label+"]-(n2 {id:{id2}}) DELETE r")
      .on("id1" -> sourceNodeId,
      "id2" -> destNodeId)
  }

  def updateRelation(sourceId: Int, oldRelation: Relation, newRelation: Relation, destId: Int): CypherStatement = ???

  def getAllRelations: CypherStatement = {
    Cypher("match n-[r]->m return distinct type(r) as relation_label")
  }

  /**
   * Create a cypher statement to get all relation which source is a concept.
   * @author Thomas GIOVANNINI
   * @param conceptId the source concept
   * @return a cypher statement returning the relations types and concepts labels and properties
   */
  def getRelationsFrom(conceptId: Int): CypherStatement = {
    Cypher("""
             |MATCH (n1 {id: {id}})-[r]->(n2)
             |RETURN type(r) as rel_type,
             |       n2.label as concept_label,
             |       n2.properties as concept_prop,
             |       n2.type as node_type,
             |       n2.rules as concept_rules,
             |       n2.display as concept_display
           """.stripMargin)
      .on("id" -> conceptId)
  }

  /**
   * Create a cypher statement to get all relation which destination is a given concept.
   * @author Thomas GIOVANNINI
   * @param conceptId the destination concept
   * @return a cypher statement returning the relations types and concepts labels and properties
   */
  def getRelationsTo(conceptId: Int): CypherStatement = {
    Cypher("""
             |MATCH (n1 {id: {id}})<-[r]-(n2)
             |RETURN type(r) as rel_type,
             |       n2.label as concept_label,
             |       n2.properties as concept_prop,
             |       n2.type as node_type,
             |       n2.rules as concept_rules,
             |       n2.display as concept_display
           """.stripMargin)
      .on("id" -> conceptId)
  }

  /**
   * Create a cypher statement to get all the parents concepts of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId of which the parents are desired
   * @return a cypher statement
   */
  def getParentConcepts(conceptId: Int): CypherStatement = {
    Cypher("""
        |MATCH (n1 {id: {id}})-[r:SUBTYPE_OF]->(n2)
        |RETURN n2.label as concept_label,
        |       n2.properties as concept_prop,
        |       n2.rules as concept_rules,
        |       n2.display as concept_display
      """.stripMargin)
      .on("id" -> conceptId)
  }

  /**
   * Create a cypher statement to get all the children concepts of a given one
   * @author Thomas GIOVANNINI
   * @param conceptId of which the children are desired
   * @return a cypher statement
   */
  def getChildrenConcepts(conceptId: Int): CypherStatement = {
    Cypher("""
        |MATCH (n1 {id: {id}})<-[r:SUBTYPE_OF]-(n2)
        |RETURN n2.label as concept_label,
        |       n2.properties as concept_prop,
        |       n2.rules as concept_rules,
        |       n2.display as concept_display
      """.stripMargin)
      .on("id" -> conceptId)
  }
}
/* Relations */
