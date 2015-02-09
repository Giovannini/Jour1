package controllers

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Coordinates, Label}
import models.graph.ontology._
import models.map.WorldMap
import org.anormcypher.Neo4jREST
import play.api.mvc._


object InitWorld extends Controller {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  /**
   * Send an example of the json representing the world map that will be sent to the client
   * @author Thomas GIOVANNINI
   */
  def getFakeInstances = Action {
    val exampleMap = fakeWorldMapGeneration(50,30)
    Ok(exampleMap.toJson)
  }

  /**
   * Generate a fake world map
   * @author Thomas GIOVANNINI
   * @return a fake world map
   */
  def fakeWorldMapGeneration(width: Int, height: Int) = {
    val map = WorldMap(Label("MapOfTheWorld"), "Test map", width, height)
    val conceptGrass = Concept("Grass", List(), List())
    val conceptTree = Concept("Tree", List(Property("Size", "Int", 5)), List())
    for(i <- 0 until width; j <- 0 until height){
      val id = (i * width + j) * 2
      val grass = Instance(id, "Grass"+i+"_"+j, Coordinates(0, 0), List(), conceptGrass)
      val coordinates = Coordinates(i, j)
      map.addInstanceAt(grass, coordinates)
      if(math.random > 0.9){
        val tree = Instance(id+1, "Tree"+i+"_"+j, Coordinates(0,0),
          List(ValuedProperty(Property("Size", "Int", 5), (math.random * 10).toInt.toString)),
          conceptTree)
        map.addInstanceAt(tree, coordinates)
      }
    }
    map
  }
  
  def initGraph = Action {
    Statement.clearDB.execute
    putInitialConceptsInDB()
    Ok("Le graph a été correctement initialisé")
  }

  /**
   * Put the concepts of the initial graph and theirs connections in the DB, reset all the rest.
   * @author Thomas GIOVANNINI
   */
  def putInitialConceptsInDB(): Unit = {
    /*Property declaration*/
    val propertyInstanciable      = Property("Instanciable", "Boolean", false)
    val propertyDuplicationSpeed  = Property("DuplicationSpeed", "Int", 5)

    /*Concepts declaration*/
    val conceptMan        = Concept("Man", List(propertyInstanciable), List())
    val conceptPredator   = Concept("Predator", List(), List())
    val conceptWolf       = Concept("Wolf", List(propertyInstanciable), List())
    val conceptSheep      = Concept("Sheep", List(propertyInstanciable), List())
    val conceptAnimal     = Concept("Animal", List(propertyInstanciable), List())
    val conceptGrass      = Concept("Grass", List(propertyInstanciable, propertyDuplicationSpeed), List(), "#00ff00")
    val conceptEdible     = Concept("Edible", List(), List())
    val conceptApple      = Concept("Apple", List(propertyInstanciable), List())
    val conceptBush       = Concept("Bush", List(propertyInstanciable), List())
    val conceptAppleTree  = Concept("AppleTree", List(propertyInstanciable), List())
    val conceptTree       = Concept("Tree", List(propertyInstanciable), List(), "#55ff55")
    val conceptFir        = Concept("Fir", List(propertyInstanciable), List())
    val conceptVegetable  = Concept("Vegetable", List(), List())
    val conceptGround     = Concept("Ground", List(), List())
    val conceptLiquid     = Concept("Liquid", List(), List(), "#0000ff")
    val conceptSolid      = Concept("Solid", List(), List(), "#ffff00")

    /*Relations declaration*/
    val relationSubtypeOf   = Relation("SUBTYPE_OF")
    val relationEat         = Relation("EAT")
    val relationCut         = Relation("CUT")
    val relationMove        = Relation("MOVE")
    val relationFlee        = Relation("FLEE")
    val relationGrowOn      = Relation("GROW_ON")

    Statement.clearDB.execute
    /*Storage of the concepts in DB*/
    NeoDAO.addConceptToDB(conceptMan)
    NeoDAO.addConceptToDB(conceptPredator)
    NeoDAO.addConceptToDB(conceptAnimal)
    NeoDAO.addConceptToDB(conceptWolf)
    NeoDAO.addConceptToDB(conceptSheep)
    NeoDAO.addConceptToDB(conceptGrass)
    NeoDAO.addConceptToDB(conceptEdible)
    NeoDAO.addConceptToDB(conceptApple)
    NeoDAO.addConceptToDB(conceptBush)
    NeoDAO.addConceptToDB(conceptAppleTree)
    NeoDAO.addConceptToDB(conceptTree)
    NeoDAO.addConceptToDB(conceptFir)
    NeoDAO.addConceptToDB(conceptVegetable)
    NeoDAO.addConceptToDB(conceptGround)
    NeoDAO.addConceptToDB(conceptLiquid)
    NeoDAO.addConceptToDB(conceptSolid)

    /*Creation of the relations in DB*/
    NeoDAO.addRelationToDB(conceptAnimal.id, relationMove, conceptAnimal.id)
    NeoDAO.addRelationToDB(conceptSheep.id, relationSubtypeOf, conceptAnimal.id)
    NeoDAO.addRelationToDB(conceptSheep.id, relationFlee, conceptPredator.id)
    NeoDAO.addRelationToDB(conceptSheep.id, relationEat, conceptEdible.id)
    NeoDAO.addRelationToDB(conceptPredator.id, relationEat, conceptSheep.id)
    NeoDAO.addRelationToDB(conceptPredator.id, relationSubtypeOf, conceptAnimal.id)
    NeoDAO.addRelationToDB(conceptWolf.id, relationSubtypeOf, conceptPredator.id)
    NeoDAO.addRelationToDB(conceptMan.id, relationSubtypeOf, conceptPredator.id)
    NeoDAO.addRelationToDB(conceptMan.id, relationEat, conceptApple.id)
    NeoDAO.addRelationToDB(conceptMan.id, relationCut, conceptTree.id)
    NeoDAO.addRelationToDB(conceptFir.id, relationSubtypeOf, conceptTree.id)
    NeoDAO.addRelationToDB(conceptAppleTree.id, relationSubtypeOf, conceptTree.id)
    NeoDAO.addRelationToDB(conceptApple.id, relationGrowOn, conceptAppleTree.id)
    NeoDAO.addRelationToDB(conceptApple.id, relationSubtypeOf, conceptEdible.id)
    NeoDAO.addRelationToDB(conceptBush.id, relationSubtypeOf, conceptTree.id)
    NeoDAO.addRelationToDB(conceptBush.id, relationSubtypeOf, conceptEdible.id)
    NeoDAO.addRelationToDB(conceptGrass.id, relationSubtypeOf, conceptEdible.id)
    NeoDAO.addRelationToDB(conceptGrass.id, relationSubtypeOf, conceptVegetable.id)
    NeoDAO.addRelationToDB(conceptTree.id, relationSubtypeOf, conceptVegetable.id)
    NeoDAO.addRelationToDB(conceptLiquid.id, relationSubtypeOf, conceptGround.id)
    NeoDAO.addRelationToDB(conceptSolid.id, relationSubtypeOf, conceptGround.id)
  }
}