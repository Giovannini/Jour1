package models.graph

import models.graph.custom_types.{Coordinates, Label}
import models.graph.ontology._
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite


/**
 * Test class for the object NeoDAO
 */
class NeoDAO$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val propName    = Property("Name")
  val propFirstName = Property("FirstName")
  val propAge    = Property("Age")
  val conceptMan = Concept(Label("Man"), List(propName, propFirstName, propAge))
  val conceptCat = Concept(Label("Cat"), List(propName, propAge))
  val conceptCity = Concept(Label("City"), List(propName))
  val relPossess = Relation("POSSESS")
  val relLives = Relation("LIVES_IN")
  val thomas = Instance(Label("Thomas"),
    Coordinates(0,0),
    List(ValuedProperty(propName, "GIOVA"),
      ValuedProperty(propFirstName, "Thomas"),
      ValuedProperty(propAge, "21")),
    conceptMan)


  test("method addConceptToDB should add a concept in the Neo4J DB"){
    NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    assert(NeoDAO.readConcepts.contains(conceptCat))
    assert(NeoDAO.readConcepts.length == 1)
  }

  test("method removeConceptFromDB should remove an existing concept in the Neo4J DB"){
    //NeoDAO.clearDBStatement.execute()
    //NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.removeConceptFromDB(conceptCat)
    assert(! NeoDAO.readConcepts.contains(conceptCat))
    assert(NeoDAO.readConcepts.length == 0)
  }

  test("no exception should be thrown if a not existing concept is deleted"){
    //NeoDAO.clearDBStatement.execute()
    NeoDAO.removeConceptFromDB(conceptMan) //No error if deleting a not existing concept
  }

  test("method addRelationsToDB should add a relation between two existing concepts in the Neo4J DB"){
    //NeoDAO.clearDBStatement.execute()
    NeoDAO.addConceptToDB(conceptCat)
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relPossess, conceptCat.hashCode())
    assert(NeoDAO.getRelations(conceptMan).contains((relPossess, conceptCat)))
  }

  test("method removeRelationFromDB should remove a relation between two existing concepts in the Neo4J DB"){
    //NeoDAO.clearDBStatement.execute()
    //NeoDAO.addConceptToDB(conceptCat)
    //NeoDAO.addConceptToDB(conceptMan)
    //NeoDAO.addRelationToDB(conceptMan.hashCode(), relPossess, conceptCat.hashCode())
    NeoDAO.removeRelationFromDB(conceptMan, relPossess, conceptCat)
    assert(! NeoDAO.getRelations(conceptMan).contains((relPossess, conceptCat)))
    assert(NeoDAO.getRelations(conceptMan).isEmpty)
  }

  test("method getRelations should return all the relations linked to the given concept"){
    //NeoDAO.clearDBStatement.execute()
    //NeoDAO.addConceptToDB(conceptCat)
    //NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addConceptToDB(conceptCity)
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relPossess, conceptCat.hashCode())
    NeoDAO.addRelationToDB(conceptMan.hashCode(), relLives, conceptCity.hashCode())
    val relationList = NeoDAO.getRelations(conceptMan)
    assert(relationList.contains((relPossess, conceptCat)))
    assert(relationList.contains((relLives, conceptCity)))
    assert(! relationList.contains((relPossess, conceptCity)))
  }

  test("method addInstance should add instance in the graph"){
    //NeoDAO.clearDBStatement.execute()
    //NeoDAO.addConceptToDB(conceptCat)
    //NeoDAO.addConceptToDB(conceptMan)
    //NeoDAO.addConceptToDB(conceptCity)
    //NeoDAO.addRelationToDB(conceptMan.hashCode(), relPossess, conceptCat.hashCode())
    //NeoDAO.addRelationToDB(conceptMan.hashCode(), relLives, conceptCity.hashCode())
    NeoDAO.addInstance(thomas)
    assert(NeoDAO.getInstancesOf(conceptMan).contains(thomas))
  }

  test("method removeInstance should remove an instance from the graph"){
    assert(NeoDAO.getInstancesOf(conceptMan).contains(thomas))
    NeoDAO.removeInstance(thomas)
    println("Instance removed")
    assert(! NeoDAO.getInstancesOf(conceptMan).contains(thomas))
  }


}
