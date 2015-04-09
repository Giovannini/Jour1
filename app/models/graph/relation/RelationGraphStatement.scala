package models.graph.relation

import org.anormcypher.{Cypher, CypherStatement}

/**
 * Statement for graph database interactions
 * @author Thomas GIOVANNINI
 */
object RelationGraphStatement {

  /**
   * Create a cypher statement to create a relation
   * @author Thomas GIOVANNINI
   * @param sourceNodeId the source of the link
   * @param relationId the name of the link
   * @param destNodeId the destimation of the linkl
   * @return a cypher statement to execute
   */
  def createRelation(sourceNodeId: Long, relationId: Long, destNodeId: Long) : CypherStatement = {
    Cypher("MATCH (n1 {id: {id1}}), (n2 {id: {id2}})\n"+
           "CREATE (n1)-[r:R_"+relationId.toString+"]->(n2)")
      .on("id1" -> sourceNodeId,
        "id2" -> destNodeId)
  }

  /**
   * Create a cypher statement to delete a relation in the Neo4J graph
   * @author Thomas GIOVANNINI
   * @param sourceNodeId the source concept of the relation to delete
   * @param relationId the relation to delete
   * @param destNodeId the destination concept of the relation to delete
   * @return a cypher statement deleting the desired relation
   */
  def deleteRelation(sourceNodeId: Long, relationId: Long, destNodeId: Long) : CypherStatement = {
    Cypher( "MATCH (n1 {id: {id1}})-[r:R_"+relationId.toString+"]-(n2 {id:{id2}}) DELETE r")
      .on("id1" -> sourceNodeId,
        "id2" -> destNodeId)
  }

  /**
   * Create a cypher statement to delete a relation everywhere in the Neo4J graph
   * @author Aurélie LORGEOUX
   * @param relationId the relation to delete
   * @return a cypher statement deleting the desired relation
   */
  def deleteRelation(relationId: Long) : CypherStatement = {
    Cypher( "MATCH (n1)-[r:R_"+relationId.toString+"]-(n2) DELETE r")
  }

  def getAllRelations: CypherStatement = {
    Cypher("match n-[r]->m return distinct type(r) as rel_type")
  }

  def getRelationsById(id: Long): CypherStatement = {
    val query = "match n-[:R_"+id+"]->m " +
                "return " +
                "n.label as source_label, " +
                "m.label as destination_label"
    println(query)
    Cypher(query)
  }

  /**
   * Create a cypher statement to get all relation which source is a concept.
   * @author Thomas GIOVANNINI
   * @param conceptId the source concept
   * @return a cypher statement returning the relations types and concepts labels and properties
   */
  def getRelationsFrom(conceptId: Long): CypherStatement = {
    Cypher("""
             |MATCH (n1 {id: {id}})-[r]->(n2)
             |RETURN type(r) as rel_type,
             |       n2.id as concept_id
           """.stripMargin)
      .on("id" -> conceptId)
  }

  /**
   * Create a cypher statement to get all relation which destination is a given concept.
   * @author Thomas GIOVANNINI
   * @param conceptId the destination concept
   * @return a cypher statement returning the relations types and concepts labels and properties
   */
  def getRelationsTo(conceptId: Long): CypherStatement = {
    Cypher("""
             |MATCH (n1 {id: {id}})<-[r]-(n2)
             |RETURN type(r) as rel_type,
             |       n2.id as concept_id
           """.stripMargin)
      .on("id" -> conceptId)
  }

  /**
   * Create a cypher statement to get all the parents concepts of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId of which the parents are desired
   * @return a cypher statement
   */
  def getParentConcepts(conceptId: Long): CypherStatement = {
    val relationSubtypeName = RelationSqlDAO.getByName("SUBTYPE_OF")
    Cypher("""
             |MATCH (n1 {id: {id}})-[r]->(n2)
             |WHERE type(r)={relationSubtype}
             |RETURN n2.id as concept_id
           """.stripMargin)
      .on("id" -> conceptId, "relationSubtype" -> ("R_" + relationSubtypeName.id))
  }

  /**
   * Create a cypher statement to get all the children concepts of a given one
   * @author Thomas GIOVANNINI
   * @param conceptId of which the children are desired
   * @return a cypher statement
   */
  def   getChildrenConcepts(conceptId: Long): CypherStatement = {
    val relationSubtypeName = RelationSqlDAO.getByName("SUBTYPE_OF")
    Cypher("""
             |MATCH (n1 {id: {id}})<-[r]-(n2)
             |WHERE type(r)={relationSubtype}
             |RETURN n2.id as concept_id
           """.stripMargin)
      .on("id" -> conceptId, "relationSubtype" -> ("R_" + relationSubtypeName.id))
  }

}
