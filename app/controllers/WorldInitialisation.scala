package controllers

import models.graph.NeoDAO
import models.graph.custom_types.Statement
import models.graph.ontology._
import models.{WorldInit, WorldMap}
import org.anormcypher.Neo4jREST
import play.Play
import play.api.mvc._


object WorldInitialisation extends Controller {

  implicit val connection = Neo4jREST(Play.application.configuration.getString("serverIP"), 7474, "/db/data/")

  private var isInitialized = false

  /**
   * Send an example of the json list of instances in the world map that will be sent to the client
   * @author Thomas GIOVANNINI
   */
  def getWorld = Action {
    val exampleMap = {
      if (isInitialized) Application.map
      else{
        println("World is not initialized yet.")
        isInitialized = true
        worldGeneration
      }
    }
    Ok(exampleMap.toJson)
  }

  /**
   * Generate a fake world map
   * @author Thomas GIOVANNINI
   * @return a fake world map
   */
  def worldGeneration: WorldMap = {
    val map = Application.map
    val t1 = System.currentTimeMillis()
    WorldInit.worldMapGeneration()
    val t2 = System.currentTimeMillis()
    println("Generation took " + (t2 - t1) + "ms.")
    map
  }
  
  def initGraph = Action {
    val result = Statement.clearDB.execute
    //this is working
    if (result) {
      if(putInitialConceptsInDB) {
        isInitialized = false
        Ok("Le graph a été correctement initialisé")
      }
      else Ok("Error while filling the graph")
    }else{
      Ok("Error")
    }
  }

  /**
   * Put the concepts of the initial graph and theirs connections in the DB, reset all the rest.
   * @author Thomas GIOVANNINI
   */
  def putInitialConceptsInDB: Boolean = {
    /*Property declaration*/
    val propertyInstanciable      = Property("Instanciable", "Boolean", false)
    val propertyDuplicationSpeed  = Property("DuplicationSpeed", "Int", 5)
    val propertyStrength          = Property("Strength", "Int", 0)
    val propertyZIndex            = Property("ZIndex", "Int", 0)
    val propertyWalkingDistance   = Property("WalkingDistance", "Int", 3)

    /*Concepts declaration*/
    val conceptMan        = Concept("Man",
      List(),
      List(ValuedProperty(propertyZIndex, 20),
        ValuedProperty(propertyStrength,2),
        ValuedProperty(propertyInstanciable,true)),
      "#E3B494")
    val conceptPredator   = Concept("Predator",
        List(),
        List())
    val conceptWolf       = Concept("Wolf",
        List(),
        List(ValuedProperty(propertyZIndex, 18),
          ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        "#1A1A22")
    val conceptSheep      = Concept("Sheep",
        List(propertyWalkingDistance),
        List(ValuedProperty(propertyZIndex, 16),
          ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        "#EEE9D6")
    val conceptAnimal     = Concept("Animal",
        List(propertyWalkingDistance),
        List())
    val conceptGrass      = Concept("Grass",
        List(propertyDuplicationSpeed),
        List(ValuedProperty(propertyZIndex, 8),
          ValuedProperty(propertyStrength,40),
          ValuedProperty(propertyInstanciable,true)),
        "#62A663")
    val conceptEdible     = Concept("Edible", List(), List())
    val conceptApple      = Concept("Apple",
        List(),
        List(ValuedProperty(propertyZIndex, 20),
          ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        "#A83B36")
    val conceptBush       = Concept("Bush",
        List(),
        List(ValuedProperty(propertyZIndex, 4),
          ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        "#2A6E37")
    val conceptAppleTree  = Concept("AppleTree",
        List(),
        List(ValuedProperty(propertyZIndex, 9),
          ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        "#2F1C13")
    val conceptTree       = Concept("Tree",
        List(),
        List(ValuedProperty(propertyZIndex, 7),
          ValuedProperty(propertyStrength,4),
          ValuedProperty(propertyInstanciable,true)),
        "#483431")
    val conceptFir        = Concept("Fir",
        List(),
        List(ValuedProperty(propertyZIndex, 8),
          ValuedProperty(propertyStrength,4)),
        "#221D1D")
    val conceptVegetable  = Concept("Vegetable", List(), List())
    val conceptGround     = Concept("Ground", List(), List())
    val conceptWater      = Concept("Water",
        List(),
        List(ValuedProperty(propertyZIndex, 0),
          ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        "#86B6B6")
    val conceptEarth      = Concept("Earth",
        List(),
        List(ValuedProperty(propertyZIndex, 1),
          ValuedProperty(propertyStrength,3),
          ValuedProperty(propertyInstanciable,true)),
        "#878377")

    /*Relations declaration*/
    val relationSubtypeOf   = Relation("SUBTYPE_OF")
    val relationEat         = Relation("ACTION_EAT")
    val relationCut         = Relation("ACTION_CUT")
    val relationMove        = Relation("ACTION_MOVE")
    val relationFlee        = Relation("ACTION_FLEE")
    val relationProduces    = Relation("ACTION_PRODUCE")

    val relationLiveOn      = Relation("LIVE_ON")

//    Statement.clearDB.execute
    /*Storage of the concepts in DB*/
    val addConceptVerification = NeoDAO.addConceptToDB(conceptMan) &&
      NeoDAO.addConceptToDB(conceptPredator) &&
      NeoDAO.addConceptToDB(conceptAnimal) &&
      NeoDAO.addConceptToDB(conceptWolf) &&
      NeoDAO.addConceptToDB(conceptSheep) &&
      NeoDAO.addConceptToDB(conceptGrass) &&
      NeoDAO.addConceptToDB(conceptEdible) &&
      NeoDAO.addConceptToDB(conceptApple) &&
      NeoDAO.addConceptToDB(conceptBush) &&
      NeoDAO.addConceptToDB(conceptAppleTree) &&
      NeoDAO.addConceptToDB(conceptTree) &&
      NeoDAO.addConceptToDB(conceptFir) &&
      NeoDAO.addConceptToDB(conceptVegetable) &&
      NeoDAO.addConceptToDB(conceptGround) &&
      NeoDAO.addConceptToDB(conceptWater) &&
      NeoDAO.addConceptToDB(conceptEarth)
    /*Creation of the relations in DB*/
    val addRelationVerification =
      NeoDAO.addRelationToDB(conceptAnimal.id, relationMove, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationSubtypeOf, conceptAnimal.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationFlee, conceptPredator.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationEat, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptPredator.id, relationEat, conceptSheep.id) &&
      NeoDAO.addRelationToDB(conceptPredator.id, relationSubtypeOf, conceptAnimal.id) &&
      NeoDAO.addRelationToDB(conceptWolf.id, relationSubtypeOf, conceptPredator.id) &&
      NeoDAO.addRelationToDB(conceptMan.id, relationSubtypeOf, conceptPredator.id) &&
      NeoDAO.addRelationToDB(conceptMan.id, relationEat, conceptApple.id) &&
      NeoDAO.addRelationToDB(conceptMan.id, relationCut, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptFir.id, relationSubtypeOf, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptAppleTree.id, relationSubtypeOf, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptAppleTree.id, relationProduces, conceptApple.id) &&
      NeoDAO.addRelationToDB(conceptApple.id, relationSubtypeOf, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptBush.id, relationSubtypeOf, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptBush.id, relationSubtypeOf, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptGrass.id, relationSubtypeOf, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptGrass.id, relationSubtypeOf, conceptVegetable.id) &&
      NeoDAO.addRelationToDB(conceptTree.id, relationSubtypeOf, conceptVegetable.id) &&
      NeoDAO.addRelationToDB(conceptWater.id, relationSubtypeOf, conceptGround.id) &&
      NeoDAO.addRelationToDB(conceptEarth.id, relationSubtypeOf, conceptGround.id)
    //live
    val addRelationVerification2 =
    NeoDAO.addRelationToDB(conceptMan.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptWolf.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptSheep.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptGrass.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptTree.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptBush.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptAppleTree.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptFir.id, relationLiveOn, conceptEarth.id) &&
    NeoDAO.addRelationToDB(conceptApple.id, relationLiveOn, conceptAppleTree.id) &&
    NeoDAO.addRelationToDB(conceptSheep.id, relationLiveOn, conceptEarth.id)

    addConceptVerification && addRelationVerification && addRelationVerification2
  }
}