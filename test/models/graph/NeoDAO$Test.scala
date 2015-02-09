package models.graph

import models.graph.custom_types.Coordinates
import models.graph.ontology._
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite


/**
 * Test class for the object NeoDAO
 */
class NeoDAO$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val prop1 = Property("P1")
  val prop2 = Property("P2")
  val prop3 = Property("P3")
  val concept1 = Concept("C1", List(prop1, prop2))
  val concept2 = Concept("C2", List(prop1, prop3))
  val concept3 = Concept("C3", List(prop1))
  val concept4 = Concept("C4", List(prop1))
  val relation1 = Relation("R1")
  val relation2 = Relation("R2")
  val relSubtype = Relation("SUBTYPE_OF")
  val thomas = Instance("Thomas",
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
  test("method addRelationsToDB should add a relation between two existing concepts in the Neo4J DB") {
    assert(NeoDAO.addConceptToDB(concept1))
    assert(NeoDAO.addConceptToDB(concept2))
    assert(NeoDAO.addConceptToDB(concept3))
    assert(NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id))
    assert(NeoDAO.addRelationToDB(concept1.id, relSubtype, concept3.id))
    assert(NeoDAO.addRelationToDB(concept2.id, relSubtype, concept3.id))
    assert(Concept.getRelations(concept1.id).contains((relation1, concept2)))
    assert(Concept.getRelations(concept1.id).contains((relSubtype, concept3)))
    assert(Concept.getRelations(concept2.id).contains((relSubtype, concept3)))
    assert(NeoDAO.removeConceptFromDB(concept1))
    assert(NeoDAO.removeConceptFromDB(concept2))
    assert(NeoDAO.removeConceptFromDB(concept3))
  }

  test("method removeRelationFromDB should remove a relation between two existing concepts in the Neo4J DB") {
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addConceptToDB(concept2)
    NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
    NeoDAO.addRelationToDB(concept1.id, relation2, concept2.id)
    NeoDAO.removeRelationFromDB(concept1.id, relation2, concept2.id)
    val relationList = Concept.getRelations(concept1.id)
    assert(relationList.contains((relation1, concept2)))
    assert(! relationList.contains((relation2, concept2)))
    NeoDAO.removeConceptFromDB(concept1)
    NeoDAO.removeConceptFromDB(concept2)
  }

  /* Instance */
  test("method addInstance should add instance in the graph if it is valid") {
    assert(! NeoDAO.addInstance(thomas))
    assert(! Concept.getInstancesOf(concept1.id).contains(thomas))
    assert(NeoDAO.addConceptToDB(concept1))
    assert(NeoDAO.addInstance(thomas))
    assert(Concept.getInstancesOf(concept1.hashCode()).contains(thomas))
    NeoDAO.removeConceptFromDB(concept1)
  }

  test("method removeInstance should remove an instance from the graph"){
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addInstance(thomas)
    assert(Concept.getInstancesOf(concept1.id).contains(thomas))
    NeoDAO.removeInstance(thomas)
    assert(! Concept.getInstancesOf(concept1.id).contains(thomas))
    NeoDAO.removeConceptFromDB(concept1)
  }

  test("method updateInstance should update an existing instance from the graph"){
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addInstance(thomas)
    val newValuedProperty = ValuedProperty(prop1, "BOURGUIGNON")
    val updatedThomas = Instance.update(thomas, newValuedProperty)
    NeoDAO.updateInstance(updatedThomas)
    assert(Concept.getInstancesOf(concept1.id).head.hashCode == thomas.hashCode)
    assert(! Concept.getInstancesOf(concept1.id).contains(thomas))
    assert(Concept.getInstancesOf(concept1.id).contains(updatedThomas))
    assert(Concept.getInstancesOf(concept1.id).head.properties.contains(newValuedProperty))
    NeoDAO.removeConceptFromDB(concept1)
  }

}
