package controllers

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Coordinates, Label}
import models.graph.ontology._
import models.map.{WorldInit, WorldMap}
import org.anormcypher.Neo4jREST
import play.api.mvc._

import scala.util.Random

object InitWorld extends Controller {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  private var isInitialized = false

  /**
   * Send an example of the json list of instances in the world map that will be sent to the client
   * @author Thomas GIOVANNINI
   */
  def getFakeInstances = Action {
    val exampleMap = {
      if (isInitialized)
        Application.map
      else{
        isInitialized = true
        fakeWorldMapGeneration
      }
    }
    Ok(exampleMap.toJson)
  }

  /**
   * Generate a fake world map
   * @author Thomas GIOVANNINI
   * @return a fake world map
   */
  def fakeWorldMapGeneration: WorldMap = {
    val map = Application.map
    val width = map.width
    val height = map.height
    WorldInit.worldMapGeneration(width,height).foreach(map.addInstance)
    //WorldInit.randomObjectInstanciation()
/*    val conceptSheep = Concept.findAll.find(concept => concept.label == "Sheep").getOrElse(Concept.error)
    Seq.fill(20)(Random.nextInt(width))
      .zip(Seq.fill(20)(Random.nextInt(height)))
      .map(coordsTuple => Coordinates(coordsTuple._1, coordsTuple._2))
      .map(coordinates => conceptSheep.createInstanceAt(coordinates))
      .foreach(map.addInstance)*/
    map
  }
  
  def initGraph = Action {
    val result = Statement.clearDB.execute
    if (result) {
      putInitialConceptsInDB()
      Ok("Le graph a été correctement initialisé")
    }else{
      Ok("Error")
    }
  }

  /**
   * Put the concepts of the initial graph and theirs connections in the DB, reset all the rest.
   * @author Thomas GIOVANNINI
   */
  def putInitialConceptsInDB(): Unit = {
    /*Property declaration*/
    val propertyInstanciable      = Property("Instanciable", "Boolean", false)
    val propertyDuplicationSpeed  = Property("DuplicationSpeed", "Int", 5)
    val propertyStrength          = Property("Strength", "Int", 0)
    val propertyZIndex            = Property("ZIndex", "Int", 0)
    
    /*Concepts declaration*/
    val conceptMan        = Concept("Man", List(), List(ValuedProperty(propertyZIndex, 20),ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#E3B494")
    val conceptPredator   = Concept("Predator", List(), List())
    val conceptWolf       = Concept("Wolf", List(), List(ValuedProperty(propertyZIndex, 18),ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#1A1A22")
    val conceptSheep      = Concept("Sheep", List(), List(ValuedProperty(propertyZIndex, 16),ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#EEE9D6")
    val conceptAnimal     = Concept("Animal", List(), List())
    val conceptGrass      = Concept("Grass", List(propertyDuplicationSpeed), List(ValuedProperty(propertyZIndex, 8),ValuedProperty(propertyStrength,40),ValuedProperty(propertyInstanciable,true)), "#62A663")
    val conceptEdible     = Concept("Edible", List(), List())
    val conceptApple      = Concept("Apple", List(), List(ValuedProperty(propertyZIndex, 6),ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#A83B36")
    val conceptBush       = Concept("Bush", List(), List(ValuedProperty(propertyZIndex, 4),ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#2A6E37")
    val conceptAppleTree  = Concept("AppleTree", List(), List(ValuedProperty(propertyZIndex, 9),ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#2F1C13")
    val conceptTree       = Concept("Tree", List(), List(ValuedProperty(propertyZIndex, 7),ValuedProperty(propertyStrength,4),ValuedProperty(propertyInstanciable,true)), "#483431")
    val conceptFir        = Concept("Fir", List(), List(ValuedProperty(propertyZIndex, 8),ValuedProperty(propertyStrength,4),ValuedProperty(propertyInstanciable,true)), "#221D1D")
    val conceptVegetable  = Concept("Vegetable", List(), List())
    val conceptGround     = Concept("Ground", List(), List())
    val conceptLiquid     = Concept("Liquid", List(), List(ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#86B6B6")
    val conceptSolid      = Concept("Solid", List(), List(ValuedProperty(propertyStrength,2),ValuedProperty(propertyInstanciable,true)), "#878377")

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