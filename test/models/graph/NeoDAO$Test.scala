package models.graph

import models.graph.custom_types.{Statement, Coordinates}
import models.graph.ontology._
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite


/**
 * Test class for the object NeoDAO
 */
class NeoDAO$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val propName = Property("Name")
  val propFirstName = Property("FirstName")
  val propAge = Property("Age")
  val conceptMan = Concept("Man", List(propName, propFirstName))
  val conceptWoman = Concept("Woman", List(propName, propAge))
  val conceptHuman = Concept("Human", List(propName))
  val conceptCat = Concept("Cat", List(propName))
  val relPet = Relation("PET")
  val relLike = Relation("LIKE")
  val relSubtype = Relation("SUBTYPE_OF")
  val thomas = Instance("Thomas",
    Coordinates(0, 0),
    List(ValuedProperty(propName, "GIOVA"),
      ValuedProperty(propFirstName, "Thomas")),
    conceptMan)
  val julien = Instance("Julien",
    Coordinates(0, 0),
    List(ValuedProperty(propName, "PRADET"),
      ValuedProperty(propFirstName, "Julien")),
    conceptMan)
  val simon = Instance("Simon",
    Coordinates(0, 0),
    List(ValuedProperty(propName, "RONCIERE"),
      ValuedProperty(propFirstName, "Simon")),
    conceptMan)
  val aurelie = Instance("Aurelie",
    Coordinates(0, 0),
    List(ValuedProperty(propName, "LORGEOUX"),
      ValuedProperty(propAge, "22")),
    conceptWoman)
  val invalidPerson = Instance("Truc",
    Coordinates(0, 0),
    List(ValuedProperty(propName, "MACHIN"),
      ValuedProperty(propAge, "22")),
    conceptMan)

  /* Concept */
  test("method addConceptToDB should add a concept in the Neo4J DB") {
    Statement.clearDB.execute()
    NeoDAO.addConceptToDB(conceptWoman)
    assert(Concept.findAll.contains(conceptWoman))
    assert(Concept.findAll.length == 1)
  }

  test("method removeConceptFromDB should remove an existing concept in the Neo4J DB") {
    NeoDAO.removeConceptFromDB(conceptWoman)
    assert(!Concept.findAll.contains(conceptWoman))
    assert(Concept.findAll.length == 0)
  }

  test("no exception should be thrown if a not existing concept is deleted") {
    NeoDAO.removeConceptFromDB(conceptMan) //No error if deleting a not existing concept
  }

  /* Relation */
  test("method addRelationsToDB should add a relation between two existing concepts in the Neo4J DB") {
    NeoDAO.addConceptToDB(conceptWoman)
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addConceptToDB(conceptHuman)
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relLike, conceptWoman.hashCode())
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relSubtype, conceptHuman.hashCode())
    NeoDAO.addRelationToDB(conceptWoman.hashCode(), relSubtype, conceptHuman.hashCode())
    assert(Concept.getRelations(conceptMan.hashCode()).contains((relLike, conceptWoman)))
    assert(Concept.getRelations(conceptMan.hashCode()).contains((relSubtype, conceptHuman)))
    assert(Concept.getRelations(conceptWoman.hashCode()).contains((relSubtype, conceptHuman)))
  }

  test("method removeRelationFromDB should remove a relation between two existing concepts in the Neo4J DB") {
    NeoDAO.removeRelationFromDB(conceptMan.hashCode(), relLike, conceptWoman.hashCode())
    val relationList = Concept.getRelations(conceptMan.hashCode())
    assert(!relationList.contains((relLike, conceptWoman)))
    assert(relationList.contains((relSubtype, conceptHuman)))
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relLike, conceptWoman.hashCode())
  }

  /* Instance */
  test("method addInstance should add instance in the graph if it is valid") {
    NeoDAO.addInstance(thomas)
    NeoDAO.addInstance(julien)
    NeoDAO.addInstance(simon)
    NeoDAO.addInstance(aurelie)
    NeoDAO.addInstance(invalidPerson)
    assert(Concept.getInstancesOf(conceptMan.hashCode()).contains(thomas))
    assert(Concept.getInstancesOf(conceptMan.hashCode()).contains(simon))
    assert(!Concept.getInstancesOf(conceptMan.hashCode()).contains(aurelie))
    assert(Concept.getInstancesOf(conceptWoman.hashCode()).contains(aurelie))
    assert(!Concept.getInstancesOf(conceptMan.hashCode()).contains(invalidPerson))
    assert(!Concept.getInstancesOf(conceptWoman.hashCode()).contains(invalidPerson))
  }

  test("method removeInstance should remove an instance from the graph"){
    assert(Concept.getInstancesOf(conceptMan.hashCode()).contains(julien))
    NeoDAO.removeInstance(julien)
    assert(! Concept.getInstancesOf(conceptMan.hashCode()).contains(julien))
  }

}
