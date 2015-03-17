package controllers

import models.graph.NeoDAO
import models.graph.custom_types.DisplayProperty
import models.graph.ontology._
import models.graph.ontology.property.{Property, PropertyDAO}
import models.graph.ontology.relation.Relation
import models.instance_action.action.ActionManager
import models.instance_action.precondition.PreconditionManager
import models.{WorldInit, WorldMap}
import play.api.mvc._


object WorldInitialisation extends Controller {

  private var isWorldMapInitialized = false

  /**
   * Send an example of the json list of instances in the world map that will be sent to the client
   * @author Thomas GIOVANNINI
   */
  def getWorld = Action {
    val exampleMap = {
      if (isWorldMapInitialized) Application.map
      else{
        println("World is not initialized yet.")
        isWorldMapInitialized = true
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
    val result = NeoDAO.clearDB()
    //this is working
    if (result) {
      if(putInitialConceptsInDB) {
        isWorldMapInitialized = false
        Ok("Le graph a été correctement initialisé")
      }
      else Ok("Error while filling the graph")
    }else{
      Ok("Connection error to the graph")
    }
  }

  /**
   * Put the concepts of the initial graph and theirs connections in the DB, reset all the rest.
   * @author Thomas GIOVANNINI
   */
  def putInitialConceptsInDB: Boolean = {

    PropertyDAO.clear
    Relation.DBList.clear

    /*Property declaration*/
    val propertyInstanciable      = PropertyDAO.save(Property(0, "Instanciable", "Boolean", false))
    val propertyDuplicationSpeed  = PropertyDAO.save(Property(0, "DuplicationSpeed", "Int", 5))
    val propertyStrength          = PropertyDAO.save(Property(0, "Strength", "Int", 0))
    val propertyWalkingDistance   = PropertyDAO.save(Property(0, "WalkingDistance", "Int", 3))
    val propertyHunger            = PropertyDAO.save(Property(0, "Hunger", "Int", 5))
    //TODO create a property to decrease hunger level when an eater eat

    println("Declaration of concepts...")

    /*Concepts declaration*/
    val conceptMan        = Concept.create("Man",
      List(),
      List(ValuedProperty(propertyStrength,2),
        ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#E3B494", 20))
    val conceptPredator   = Concept.create("Predator",
        List(propertyHunger),
        List())
    val conceptWolf       = Concept.create("Wolf",
        List(),
        List(ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#1A1A22", 18))
    val conceptSheep      = Concept.create("Sheep",
        List(),
        List(ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#EEE9D6", 16))
    val conceptAnimal     = Concept.create("Animal",
        List(propertyWalkingDistance, propertyHunger),
        List())
    val conceptGrass      = Concept.create("Grass",
        List(propertyDuplicationSpeed),
        List(ValuedProperty(propertyStrength,40),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#62A663", 8))
    val conceptEdible     = Concept.create("Edible", List(), List())
    val conceptApple      = Concept.create("Apple",
        List(),
        List(ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#A83B36", 20))
    val conceptBush       = Concept.create("Bush",
        List(),
        List(ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#2A6E37", 4))
    val conceptAppleTree  = Concept.create("AppleTree",
        List(),
        List(ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#2F1C13", 9))
    val conceptTree       = Concept.create("Tree",
        List(),
        List(ValuedProperty(propertyStrength,4),
          ValuedProperty(propertyInstanciable,true)),
      DisplayProperty("#483431", 7))
    val conceptFir        = Concept.create("Fir",
        List(),
        List(ValuedProperty(propertyStrength,4)),
        DisplayProperty("#221D1D", 8))
    val conceptVegetable  = Concept.create("Vegetable", List(), List())
    val conceptGround     = Concept.create("Ground", List(), List())
    val conceptWater      = Concept.create("Water",
        List(),
        List(ValuedProperty(propertyStrength,2),
          ValuedProperty(propertyInstanciable,true)),
        DisplayProperty("#86B6B6", 0))
    val conceptEarth      = Concept.create("Earth",
        List(),
        List(ValuedProperty(propertyStrength,3),
          ValuedProperty(propertyInstanciable,true)),
        DisplayProperty("#878377", 1))

    PreconditionManager.initialization
    ActionManager.initialization()

    println("Relations declaration...")

    /*Relations declaration*/
    val relationSubtypeOf   = Relation.DBList.save("SUBTYPE_OF")
    val relationEat         = Relation.DBList.save("ACTION_EAT")
    val relationCut         = Relation.DBList.save("ACTION_CUT")
    val relationMove        = Relation.DBList.save("ACTION_MOVE")
    val relationFlee        = Relation.DBList.save("ACTION_FLEE")
    val relationProduces    = Relation.DBList.save("ACTION_PRODUCE")
    val relationLiveOn      = Relation.DBList.save("LIVE_ON")

    NeoDAO.clearDB()
    println("Adding concepts to graph...")
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
    println("Adding relations to graph...")
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

    val result = addConceptVerification && addRelationVerification && addRelationVerification2
    println("Initialization of the graph completed: " + result)
    result
  }
}