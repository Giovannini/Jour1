package models.graph

import models.graph.custom_types.Statement
import models.graph.ontology.{Instance, Relation, Concept}
import org.anormcypher._


/**
 * Model for the NeoDAO class.
 */
object NeoDAO {

  // Setup the Rest Client
  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  /**
   * Execute a cypher query to write a list of concepts into the DB.
   * @author Thomas GIOVANNINI
   * @param concept concept to write into the DB
   * @return the cypher query
   */
  def addConceptToDB(concept: Concept) =
  {
    val statement = Statement.createConcept(concept)
    statement.execute
  }

  /**
   * Execute a cypher query to remove a concept from the DB.
   * @author Thomas GIOVANNINI
   * @param concept to remove
   */
  def removeConceptFromDB(concept: Concept) = {
    val statement = Statement.deleteConcept(concept.hashCode())
    //println(statement)
    statement.execute
  }

  /**
   * Read all the concepts of the ontology
   * @author Thomas GIOVANNINI
   * @return a list of all the concepts read.
   */
  def readConcepts : List[Concept]= {
    Statement.getAllConcepts.apply()
      .toList
      .map{ row => Concept.parseRow(row) }
  }

  def getConceptById(conceptId: Int): Concept = {
    val statement = Statement.getConceptById(conceptId)
    val row: CypherResultRow = statement.apply().head
    Concept.parseRow(row)
  }

  /**
   * Create a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to add, containing the source concept, the relation name and the destination concept
   */
  def addRelationToDB(sourceId: Int, relation: Relation, destId: Int) = {
    val statement = Statement.createRelation(sourceId, relation, destId)
    statement.execute()
  }

  /**
   * Remove a relation into two existing concepts in the Neo4J DB.
   * @author Thomas GIOVANNINI
   * @param relation the relation to remove, containing the source concept, the relation name and the destination concept
   */
  def removeRelationFromDB(sourceId: Int, relation: Relation, destId: Int) = {
    val statement = Statement.deleteRelation(sourceId, relation, destId)
    statement.apply()
      .toList
      .foreach(println)
    //if (result) println("Relation deleted") else println("Uh oh...relation not deleted.")
  }

  /**
   * Get all the relations from or to a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelations(conceptId: Int): List[(Relation, Concept)] = {
    Statement.getRelationsOf(conceptId).apply()
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
      val statement = Statement.createInstance(instance)
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
    val statement = Statement.deleteInstances(instance)
    statement.execute
  }

  /**
   * Method to retrieve all the instances of a given concept
   * @param conceptId of the desired instances
   * @return a liste of the desired instances
   */
  def getInstancesOf(conceptId : Int) : List[Instance] = {
    val statement = Statement.getInstances(conceptId)
    statement.apply()
      .toList
      .map{ row => Instance.parseRowGivenConcept(row, conceptId)}
  }

  def getParentsConceptsOf(conceptId: Int): List[(Relation, Concept)] = {
    val statement = Statement.getParentConcepts(conceptId)
    val subtypeRelation = Relation("SUBTYPE_OF")
    statement.apply().toList.map{ row => (subtypeRelation, Concept.parseRow(row))}
  }

  def getChildrenConceptsOf(conceptId: Int): List[(Relation, Concept)] = {
    val statement = Statement.getChildrenConcepts(conceptId)
    val parentRelation = Relation("PARENT_OF")
    statement.apply().toList.map{ row => (parentRelation, Concept.parseRow(row))}
  }

  def getAllPossibleActions(conceptId: Int): List[(Relation, Concept)] = {
    val relations = getRelations(conceptId)
      .filter(t => t._1 != Relation("SUBTYPE_OF"))
    val parentsRelation = getParentsConceptsOf(conceptId)
      .map(tuple => getAllPossibleActions(tuple._2.hashCode()))
      .flatten
    relations ::: parentsRelation
  }

}
