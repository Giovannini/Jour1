package models.graph

import models.graph.custom_types.{Statement, Coordinates, Label}
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


  test("method addConceptToDB should add a concept in the Neo4J DB") {
    Statement.clearDB.execute()
    NeoDAO.addConceptToDB(conceptWoman)
    assert(NeoDAO.readConcepts.contains(conceptWoman))
    assert(NeoDAO.readConcepts.length == 1)
  }

  test("method removeConceptFromDB should remove an existing concept in the Neo4J DB") {
    //NeoDAO.clearDBStatement.execute()
    //NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.removeConceptFromDB(conceptWoman)
    assert(!NeoDAO.readConcepts.contains(conceptWoman))
    assert(NeoDAO.readConcepts.length == 0)
  }

  test("no exception should be thrown if a not existing concept is deleted") {
    //NeoDAO.clearDBStatement.execute()
    NeoDAO.removeConceptFromDB(conceptMan) //No error if deleting a not existing concept
  }

  test("method addRelationsToDB should add a relation between two existing concepts in the Neo4J DB") {
    NeoDAO.addConceptToDB(conceptWoman)
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addConceptToDB(conceptHuman)
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relLike, conceptWoman.hashCode())
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relSubtype, conceptHuman.hashCode())
    NeoDAO.addRelationToDB(conceptWoman.hashCode(), relSubtype, conceptHuman.hashCode())
    assert(NeoDAO.getRelations(conceptMan.hashCode()).contains((relLike, conceptWoman)))
    assert(NeoDAO.getRelations(conceptMan.hashCode()).contains((relSubtype, conceptHuman)))
    assert(NeoDAO.getRelations(conceptWoman.hashCode()).contains((relSubtype, conceptHuman)))
  }

  test("method removeRelationFromDB should remove a relation between two existing concepts in the Neo4J DB") {
    NeoDAO.removeRelationFromDB(conceptMan.hashCode(), relLike, conceptWoman.hashCode())
    val relationList = NeoDAO.getRelations(conceptMan.hashCode())
    assert(!relationList.contains((relLike, conceptWoman)))
    assert(relationList.contains((relSubtype, conceptHuman)))
  }

  test("method getRelations should return all the relations linked to the given concept") {
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relLike, conceptWoman.hashCode())
    val relationList = NeoDAO.getRelations(conceptMan.hashCode())
    assert(relationList.contains((relLike, conceptWoman)))
    assert(relationList.contains((relSubtype, conceptHuman)))
    assert(!relationList.contains((relLike, conceptHuman)))
  }

  test("method addInstance should add instance in the graph if it is valid") {
    NeoDAO.addInstance(thomas)
    NeoDAO.addInstance(julien)
    NeoDAO.addInstance(simon)
    NeoDAO.addInstance(aurelie)
    NeoDAO.addInstance(invalidPerson)
    assert(NeoDAO.getInstancesOf(conceptMan.hashCode()).contains(thomas))
    assert(NeoDAO.getInstancesOf(conceptMan.hashCode()).contains(simon))
    assert(!NeoDAO.getInstancesOf(conceptMan.hashCode()).contains(aurelie))
    assert(NeoDAO.getInstancesOf(conceptWoman.hashCode()).contains(aurelie))
    assert(!NeoDAO.getInstancesOf(conceptMan.hashCode()).contains(invalidPerson))
    assert(!NeoDAO.getInstancesOf(conceptWoman.hashCode()).contains(invalidPerson))
  }


  test("method removeInstance should remove an instance from the graph"){
    assert(NeoDAO.getInstancesOf(conceptMan.hashCode()).contains(julien))
    NeoDAO.removeInstance(julien)
    assert(! NeoDAO.getInstancesOf(conceptMan.hashCode()).contains(julien))
  }

  test("method getParentsConceptOf should return parent concepts of the given one"){
    assert(NeoDAO.getParentsConceptsOf(conceptMan.hashCode()).contains((relSubtype,conceptHuman)))
  }

  test("method getAllPossibleActions should give all the possible actions for a given concept"){
    NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.addRelationToDB(conceptHuman.hashCode(), relPet, conceptCat.hashCode())
    val relationList = NeoDAO.getAllPossibleActions(conceptMan.hashCode())
    assert(relationList.contains((relPet, conceptCat)))
  }

}
