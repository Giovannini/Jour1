package models.graph

import models.graph.custom_types.Label
import models.graph.ontology.{Relation, Concept, Property}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite


/**
 * Test class for the object NeoDAO
 */
class NeoDAO$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val propName    = Property(Label("Name"))
  val propFirstName = Property(Label("FirstName"))
  val propAge    = Property(Label("Age"))
  val conceptMan = Concept(Label("Man"), List(propName, propFirstName, propAge))
  val conceptCat = Concept(Label("Cat"), List(propName, propAge))
  val conceptCity = Concept(Label("City"), List(propName))
  val relPossess = Relation(Label("POSSESS"))
  val relLives = Relation(Label("LIVES_IN"))


  test("method addConceptToDB should add a concept in the Neo4J DB"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    assert(NeoDAO.readConcepts.head == conceptCat)
  }

  test("method removeConceptFromDB should remove an existing concept in the Neo4J DB"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    assert(NeoDAO.readConcepts.contains(conceptCat))
    NeoDAO.removeConceptFromDB(conceptCat)
    assert(! NeoDAO.readConcepts.contains(conceptCat))
    assert(NeoDAO.readConcepts.length == 0)
  }

  test("no exception should be thrown if a not existing concept is deleted"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.removeConceptFromDB(conceptMan) //No error if deleting a not existing concept
  }

  test("method addRelationsToDB should add a relation between two existing concepts in the Neo4J DB"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addRelationToDB(conceptMan, relPossess, conceptCat)
    assert(NeoDAO.getRelations(conceptMan).contains((relPossess, conceptCat)))
  }

  test("method removeRelationFromDB should remove a relation between two existing concepts in the Neo4J DB"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addRelationToDB(conceptMan, relPossess, conceptCat)
    NeoDAO.removeRelationFromDB(conceptMan, relPossess, conceptCat)
    assert(! NeoDAO.getRelations(conceptMan).contains((relPossess, conceptCat)))
    assert(NeoDAO.getRelations(conceptMan).isEmpty)
  }

  test("method getRelations should return all the relations linked to the given concept"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addConceptToDB(conceptCity)
    NeoDAO.addRelationToDB(conceptMan, relPossess, conceptCat)
    NeoDAO.addRelationToDB(conceptMan, relLives, conceptCity)
    val relationList = NeoDAO.getRelations(conceptMan)
    assert(relationList.contains((relPossess, conceptCat)))
    assert(relationList.contains((relLives, conceptCity)))
    assert(! relationList.contains((relPossess, conceptCity)))
  }

}
