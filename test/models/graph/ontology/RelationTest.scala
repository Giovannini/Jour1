package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Coordinates}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Test class for object Relation
 */
class RelationTest extends FunSuite {

  val prop1 = Property("P1", "Int", 0)
  val concept1 = Concept("C1", List(prop1))
  val concept2 = Concept("C2", List(prop1))
  val relation1 = Relation("R1")

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")
  Statement.clearDB.execute

  test("method parseRow"){
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addConceptToDB(concept2)
    NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
    val statement = Statement.getRelationsOf(concept1.id)
    val row = statement.apply.head
    assert(Relation.parseRow(row) == relation1)
    NeoDAO.removeConceptFromDB(concept1)
    NeoDAO.removeConceptFromDB(concept2)
  }

}
