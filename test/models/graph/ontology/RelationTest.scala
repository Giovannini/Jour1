package models.graph.ontology

import models.graph.custom_types.Statement
import models.graph.ontology.concept.Concept
import models.graph.ontology.property.Property
import models.graph.ontology.relation.Relation
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Test class for object Relation
 */
class RelationTest extends FunSuite {

  val prop1 = Property(0L, "P1", "Int", 0)
  val concept1 = Concept("C1", List(prop1), List())
  val concept2 = Concept("C2", List(prop1), List())
  val relation1 = Relation("R1")

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")
  Statement.clearDB.execute

  /*test("method parseRow"){
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addConceptToDB(concept2)
    NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
    val statement = Statement.getRelationsFrom(concept1.id)
    val row = statement.apply.head
    assert(Relation.parseRow(row) == relation1)
    NeoDAO.removeConceptFromDB(concept1)
    NeoDAO.removeConceptFromDB(concept2)
  }*/

}
