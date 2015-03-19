package models.graph

import models.graph.custom_types.Coordinates
import models.graph.ontology._
import models.graph.ontology.concept.Concept
import models.graph.ontology.property.Property
import models.graph.ontology.relation.Relation
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite


/**
 * Test class for the object NeoDAO
 */
class NeoDAO$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val prop1 = Property(0L, "P1", "Int", 0)
  val prop2 = Property(1L, "P2", "String", "Hello")
  val prop3 = Property(2L, "P3", "Boolean", false)
  val concept1 = Concept("C1", List(prop1, prop2), List())
  val concept2 = Concept("C2", List(prop1, prop3), List())
  val concept3 = Concept("C3", List(prop1), List())
  val concept4 = Concept("C4", List(prop1), List())
  val relation1 = Relation("R1")
  val relation2 = Relation("R2")
  val relSubtype = Relation("SUBTYPE_OF")
  val thomas = Instance(1, "Thomas",
    Coordinates(0, 0),
    List(ValuedProperty(prop1, "GIOVA"),
      ValuedProperty(prop2, "Thomas")),
    concept1)

  /* Concept */
  test("method addConceptToDB should add a concept in the Neo4J DB") {
    assert(NeoDAO.addConceptToDB(concept1))
    assert(Concept.findAll.contains(concept1))
    NeoDAO.removeConceptFromDB(concept1)
  }

  test("method removeConceptFromDB should remove an existing concept in the Neo4J DB") {
    assert(NeoDAO.addConceptToDB(concept1))
    assert(NeoDAO.removeConceptFromDB(concept1))
    assert(!Concept.findAll.contains(concept1))
  }

  test("no exception should be thrown if a not existing concept is deleted") {
    assert(NeoDAO.removeConceptFromDB(concept1))
  }

  /* Relation */
  /*test("method addRelationsToDB should add a relation between two existing concepts in the Neo4J DB") {
    assert(NeoDAO.addConceptToDB(concept1))
    assert(NeoDAO.addConceptToDB(concept2))
    assert(NeoDAO.addConceptToDB(concept3))
    assert(NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id))
    assert(NeoDAO.addRelationToDB(concept1.id, relSubtype, concept3.id))
    assert(NeoDAO.addRelationToDB(concept2.id, relSubtype, concept3.id))
    assert(Concept.getRelationsFrom(concept1.id).contains((relation1, concept2)))
    assert(Concept.getRelationsFrom(concept1.id).contains((relSubtype, concept3)))
    assert(Concept.getRelationsFrom(concept2.id).contains((relSubtype, concept3)))
    assert(NeoDAO.removeConceptFromDB(concept1))
    assert(NeoDAO.removeConceptFromDB(concept2))
    assert(NeoDAO.removeConceptFromDB(concept3))
  }*/

  /*test("method removeRelationFromDB should remove a relation between two existing concepts in the Neo4J DB") {
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addConceptToDB(concept2)
    NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
    NeoDAO.addRelationToDB(concept1.id, relation2, concept2.id)
    NeoDAO.removeRelationFromDB(concept1.id, relation2, concept2.id)
    val relationList = Concept.getRelationsFrom(concept1.id)
    assert(relationList.contains((relation1, concept2)))
    assert(! relationList.contains((relation2, concept2)))
    NeoDAO.removeConceptFromDB(concept1)
    NeoDAO.removeConceptFromDB(concept2)
  }*/

}
