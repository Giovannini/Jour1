package models.graph.ontology

import models.graph.DisplayProperty
import models.graph.concept.{ConceptStatement, Concept}
import models.graph.property.{PropertyType, Property}
import models.graph.relation.Relation
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Test class for object Relation
 */
class RelationTest extends FunSuite {

  val prop1 = Property("P1", PropertyType.Int, 0)
  val concept1 = Concept("C1", List(prop1.defaultValuedProperty), List(), List(), DisplayProperty("#aaaaaa", 0))
  val concept2 = Concept("C2", List(prop1.defaultValuedProperty), List(), List(), DisplayProperty("#aaaaaa", 0))
  val relation1 = Relation("R1")

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")
  ConceptStatement.clearDB.execute

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
