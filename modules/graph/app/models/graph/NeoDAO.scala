package models.graph

import models.graph.ontology.{Instance, Relation, Concept}
import org.anormcypher._


/**
 * Model for the NeoDAO class.
 */
object NeoDAO {

  // Setup the Rest Client
  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  /*###################*/
  /* Cypher statements */
  /*###################*/
  val getAllConceptsStatement: CypherStatement = Cypher(
    """
    MATCH n
    WHERE n.type = "CONCEPT"
    RETURN n.label as concept_label, n.properties as concept_prop
    """
  )

  val clearDBStatement: CypherStatement = Cypher(
    """
      |MATCH (n)
      |OPTIONAL MATCH (n)-[r]-()
      |DELETE n,r
    """.stripMargin)

  /**
   * Create a cypher statement to delete a concept and all its connected relations.
   * @author Thomas GIOVANNINI
   * @param conceptId the concept to remove
   * @return a cypher statement to execute
   */
  def removeConceptStatement(conceptId: Int): CypherStatement = {
    Cypher("MATCH (n {id: {id1} }) " +
      "OPTIONAL MATCH (n)-[r]-() " +
      "DELETE n, r;")
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
  def createRelationStatement(sourceNodeId: Int, relation: Relation, destNodeId: Int) : CypherStatement = {
    val id1 = sourceNodeId
    val rel = relation.label
    val id2 = destNodeId
    Cypher( "MATCH (n1 {id: "+ id1 +"}), (n2 {id: "+ id2 +"})\n" +
      "CREATE (n1)-[r:"+ rel +"]->(n2)")
  }

  /**
   * Create a cypher statement to delete a relation in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param conceptSrc the source concept of the relation to delete
   * @param relation the relation to delete
   * @param conceptDest the destination concept of the relation to delete
   * @return a cypher statement deleting the desired relation
   */
  def deleteRelationStatement(conceptSrc: Concept, relation: Relation, conceptDest: Concept) : CypherStatement = {
    val id1 = conceptSrc.hashCode()
    val rel = relation.label
    val id2 = conceptDest.hashCode()
    Cypher( "MATCH (n1 {id: "+ id1 +"})-[r:"+rel+"]-(n2 {id: "+ id2 +"}) DELETE r")
  }

  /**
   * Create a cypher statement to get the relations and related concepts to a given concept.
   * @author Thomas GIOVANNINI
   * @param concept the source concept
   * @return a cypher statement returning the relation types and concepts labels and properties
   */
  def getRelationsOf(concept: Concept) = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})-[r]-(n2)
        |RETURN type(r) as rel_type, n2.label as concept_label, n2.properties as concept_prop, n2.type as node_type
      """.stripMargin)
      .on("id" -> concept.hashCode)
  }

  /**
   * Create a cypher statement to create an instance node in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param instance the instance to add
   * @return a cypher statement
   */
  def createInstanceStatement(instance: Instance) = {
    Cypher("CREATE " + instance.toNodeString + ";")
  }

  /**
   * Create a cypher statement to delete a relation in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param instance to delete
   * @return a cypher statement
   */
  def deleteInstancesStatement(instance: Instance) = {
    Cypher("MATCH (n {id: {id} }) " +
      "OPTIONAL MATCH (n)-[r]-() " +
      "DELETE n, r;")
      .on("id" -> instance.hashCode)
  }

  /**
   * Create a cypher statement to get the instances related to a given concept
   * @author Thomas GIOVANNINI
   * @param concept the source concept
   * @return a cypher statement
   */
  def getInstancesStatement(concept: Concept) = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})-[r:INSTANCE_OF]-(n2)
        |RETURN n2.label as inst_label, n2.properties as inst_prop, n2.coordinate_x as inst_coordx, n2.coordinate_y as inst_coordy
      """.stripMargin)
      .on("id" -> concept.hashCode)
  }

  def getParentConceptsStatement(concept: Concept) = {
    Cypher(
      """
        |MATCH (n1 {id: {id}})-[r:SUBTYPE_OF]->(n2)
        |RETURN n2.label as concept_label, n2.properties as concept_prop
      """.stripMargin)
      .on("id" -> concept.hashCode)
  }


  /*##################*/
  /* Database queries */
  /*##################*/
  /**
   * Execute a cypher query to write a list of concepts into the DB.
   * @author Thomas GIOVANNINI
   * @param concept concept to write into the DB
   * @return the cypher query
   */
  def addConceptToDB(concept: Concept) = {
    val statement = Cypher("create " + concept.toNodeString + ";")
    //println(statement)
    statement.execute
  }

  /**
   * Execute a cypher query to remove a concept from the DB.
   * @author Thomas GIOVANNINI
   * @param concept to remove
   */
  def removeConceptFromDB(concept: Concept) = {
    val statement = removeConceptStatement(concept.hashCode())
    //println(statement)
    statement.execute
  }

  /**
   * Read all the concepts of the ontology
   * @author Thomas GIOVANNINI
   * @return a list of all the concepts read.
   */
  def readConcepts : List[Concept]= {
    getAllConceptsStatement.apply()
      .toList
      .map{ row => Concept.parseRow(row) }
  }

  /**
   * Create a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to add, containing the source concept, the relation name and the destination concept
   */
  def addRelationToDB(sourceId: Int, relation: Relation, destId: Int) = {
    val statement = createRelationStatement(sourceId, relation, destId)
    statement.execute()
  }

  /**
   * Remove a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to remove, containing the source concept, the relation name and the destination concept
   */
  def removeRelationFromDB(conceptSrc: Concept, relation: Relation, conceptDest: Concept) = {
    val statement = deleteRelationStatement(conceptSrc, relation, conceptDest)
    val result = statement.apply().toList.foreach(println)
    //if (result) println("Relation deleted") else println("Uh oh...relation not deleted.")
  }

  /**
   * Get all the relations from or to a given concept
   * @author Thomas GIOVANNINI
   * @param concept the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelations(concept: Concept): List[(Relation, Concept)] = {
    getRelationsOf(concept).apply()
      .toList
      .filter{ row => row[String]("node_type") != "INSTANCE" }
      .map{ row => (Relation.parseRow(row), Concept.parseRow(row))}
  }

  /**
   * Method to add an instance of a given concept (doing the relation) to the Neo4J Graph
   * @author Thomas GIOVANNINI
   * @param instance to add to the graph
   */
  def addInstance(instance: Instance): Unit = {
    if(instance.isValid) {
      val statement = createInstanceStatement(instance)
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
    val statement = deleteInstancesStatement(instance)
    statement.execute
  }

  /**
   * Method to retrieve all the instances of a given concept
   * @param concept of the desired instances
   * @return a liste of the desired instances
   */
  def getInstancesOf(concept:Concept) : List[Instance] = {
    val statement = getInstancesStatement(concept)
    statement.apply()
      .toList
      .map{ row => Instance.parseRowGivenConcept(row, concept)}
  }

  def getParentsConceptOf(concept: Concept): List[Concept] = {
    val statement = getParentConceptsStatement(concept)
    statement.apply().toList.map{ row => Concept.parseRow(row)}
  }

  def getAllPossibleActions(concept: Concept): List[(Relation, Concept)] = {
    val relations = getRelations(concept)
      .filter(t => t._1 != Relation("SUBTYPE_OF"))
    relations ::: getParentsConceptOf(concept)
      .map(c => getAllPossibleActions(c))
      .flatten
  }

}
