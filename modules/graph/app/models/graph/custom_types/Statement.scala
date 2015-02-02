package models.graph.custom_types

import models.graph.ontology.{Instance, Concept, Relation}
import org.anormcypher.{Cypher, CypherStatement}

/**
 * All values from this objects are CypherStatements
 */
object Statement {

  val getAllConcepts: CypherStatement = Cypher(
    """
    MATCH n
    WHERE n.type = "CONCEPT"
    RETURN n.label as concept_label, n.properties as concept_prop
    """
  )

  val clearDB: CypherStatement = Cypher(
    """
      |MATCH (n)
      |OPTIONAL MATCH (n)-[r]-()
      |DELETE n,r
    """.stripMargin)

  /**
   * Create a cypher statement to create a concept.
   * @param concept to create
   * @return a cypher statement
   */
  def createConcept(concept: Concept): CypherStatement =
    Cypher("create " + concept.toNodeString + ";")

  /**
   * Create a cypher statement to delete a concept and all its connected relations.
   * @author Thomas GIOVANNINI
   * @param conceptId the concept to remove
   * @return a cypher statement to execute
   */
  def deleteConcept(conceptId: Int): CypherStatement = {
    Cypher("MATCH (n {id: {id1} }) " +
      "OPTIONAL MATCH (n)-[r]-() " +
      "DELETE n, r;")
      .on("id1" -> conceptId)
  }

  def getConceptById(conceptId: Int): CypherStatement = {
    Cypher("MATCH (n {id: {id1} }) " +
      "RETURN n.label as concept_label, n.properties as concept_prop;")
      .on("id1" -> conceptId)
  }

  /* Relations */
  /**
   * Create a cypher statement to create a relation
   * @author Thomas GIOVANNINI
   * @param sourceNodeId the source of the link
   * @param relation the name of the link
   * @param destNodeId the destimation of the linkl
   * @return a cypher statement to execute
   */
  def createRelation(sourceNodeId: Int, relation: Relation, destNodeId: Int) : CypherStatement = {
    val id1 = sourceNodeId
    val rel = relation.label
    val id2 = destNodeId
    Cypher( "MATCH (n1 {id: "+ id1 +"}), (n2 {id: "+ id2 +"})\n" +
      "CREATE (n1)-[r:"+ rel +"]->(n2)")
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
    val id1 = sourceNodeId
    val rel = relation.label
    val id2 = destNodeId
    Cypher( "MATCH (n1 {id: "+ id1 +"})-[r:"+rel+"]-(n2 {id: "+ id2 +"}) DELETE r")
  }

  /**
   * Create a cypher statement to get the relations and related concepts to a given concept.
   * @author Thomas GIOVANNINI
   * @param conceptId the source concept
   * @return a cypher statement returning the relation types and concepts labels and properties
   */
  def getRelationsOf(conceptId: Int): CypherStatement = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})-[r]-(n2)
        |RETURN type(r) as rel_type, n2.label as concept_label, n2.properties as concept_prop, n2.type as node_type
      """.stripMargin)
      .on("id" -> conceptId)
  }

  /* Instances */
  /**
   * Create a cypher statement to create an instance node in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param instance the instance to add
   * @return a cypher statement
   */
  def createInstance(instance: Instance): CypherStatement = {
    Cypher("CREATE " + instance.toNodeString + ";")
  }

  /**
   * Create a cypher statement to delete a relation in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param instance to delete
   * @return a cypher statement
   */
  def deleteInstances(instance: Instance): CypherStatement = {
    Cypher("MATCH (n {id: {id} }) " +
      "OPTIONAL MATCH (n)-[r]-() " +
      "DELETE n, r;")
      .on("id" -> instance.hashCode)
  }

  /**
   * Create a cypher statement to get the instances related to a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the source concept
   * @return a cypher statement
   */
  def getInstances(conceptId: Int): CypherStatement = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})-[r:INSTANCE_OF]-(n2)
        |RETURN n2.label as inst_label, n2.properties as inst_prop, n2.coordinate_x as inst_coordx, n2.coordinate_y as inst_coordy
      """.stripMargin)
      .on("id" -> conceptId)
  }

  /**
   * Create a cypher statement to get all th parents concepts of a given concept
   * @param conceptId of which the parents are desired
   * @return a cypher statement
   */
  def getParentConcepts(conceptId: Int): CypherStatement = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})-[r:SUBTYPE_OF]->(n2)
        |RETURN n2.label as concept_label, n2.properties as concept_prop
      """.stripMargin)
      .on("id" -> conceptId)
  }

  def getChildrenConcepts(conceptId: Int): CypherStatement = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})<-[r:SUBTYPE_OF]-(n2)
        |RETURN n2.label as concept_label, n2.properties as concept_prop
      """.stripMargin)
      .on("id" -> conceptId)
  }
  
}
