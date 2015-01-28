package models.graph

import models.graph.ontology.{Relation, Concept}
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
    RETURN n.label as label, n.properties as prop
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
   * @param conceptSrc the source of the link
   * @param relation the name of the link
   * @param conceptDest the destimation of the linkl
   * @return a cypher statement to execute
   */
  def createRelationStatement(conceptSrc: Concept, relation: Relation, conceptDest: Concept) : CypherStatement = {
    val id1 = conceptSrc.hashCode()
    val rel = relation.label.content.toUpperCase
    val id2 = conceptDest.hashCode()
    Cypher( "MATCH (n1 {id: "+ id1 +"}), (n2 {id: "+ id2 +"})\n" +
      "CREATE (n1)-[r:"+ rel +"]->(n2)" +
      "RETURN r;")
  }

  /**
   * Create a cypher statement to delete a relation in the Neo4J grap
   * @author Thomas GIOVANNINI
   * @param conceptSrc the source concept of the relation to delete
   * @param relation the relation to delete
   * @param conceptDest the destination concept of the relation to delete
   * @return a cypher statement deleting the desired relation
   */
  def deleteRelationStatement(conceptSrc: Concept, relation: Relation, conceptDest: Concept) : CypherStatement = {
    val id1 = conceptSrc.hashCode()
    val rel = relation.label.content.toUpperCase
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
        |RETURN type(r) as type, n2.label as label, n2.properties as prop
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
   * Create a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to add, containing the source concept, the relation name and the destination concept
   */
  def addRelationToDB(conceptSrc: Concept, relation: Relation, conceptDest: Concept) = {
    val statement = createRelationStatement(conceptSrc, relation, conceptDest)
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
   * Get all the relations from or to a given concept
   * @author Thomas GIOVANNINI
   * @param concept the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelations(concept: Concept): List[(Relation, Concept)] = {
    getRelationsOf(concept).apply()
      .toList
      .map{ row => (Relation.parseRow(row), Concept.parseRow(row))}
  }

}
