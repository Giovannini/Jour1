package controllers

import models.graph.NeoDAO
import models.graph.custom_types.DisplayProperty
import models.graph.ontology._
import models.graph.ontology.concept.consequence.{Consequence, ConsequenceStep}
import models.graph.ontology.concept.need.{MeanOfSatisfaction, Need, NeedDAO}
import models.graph.ontology.concept.{Concept, ConceptDAO}
import models.graph.ontology.property.{Property, PropertyDAO, PropertyType}
import models.graph.ontology.relation.RelationDAO
import models.interaction.action.InstanceActionManager
import models.interaction.precondition.PreconditionManager
import models.{WorldInit, WorldMap}
import play.api.mvc._


object WorldInitialisation extends Controller {

  private var isWorldMapInitialized = false

  /**
   * Send an example of the json list of instances in the world map that will be sent to the client
   * @author Thomas GIOVANNINI
   */
  def getWorld: Action[AnyContent] = Action {
    val exampleMap = {
      if (isWorldMapInitialized) {
        println("Getting a world already initialized.")
        Application.map
      }
      else {
        println("World is not initialized yet.")
        isWorldMapInitialized = true
        worldGeneration()
      }
    }
    Ok(exampleMap.toJson)
  }

  /**
   * Generate a fake world map
   * @author Thomas GIOVANNINI
   * @return a fake world map
   */
  def worldGeneration(): WorldMap = {
    val map = Application.map
    val t1 = System.currentTimeMillis()
    WorldInit.worldMapGeneration()
    val t2 = System.currentTimeMillis()
    println("Generation took " + (t2 - t1) + "ms.")
    map
  }

  /**
   * Clear all databases and redirect to the map for a new world
   * @author
   */
  def initialization: Action[AnyContent] = {
    Action {
      val result = NeoDAO.clearDB()
      //this is working
      if (result) {
        if (putInitialConceptsInDB) {
          Application.map.clear()
          isWorldMapInitialized = false
          Redirect(routes.MapController.show())
        }
        else {
          Ok("Error while filling the graph")
        }
      } else {
        Ok("Connection error to the graph")
      }
    }
  }

  /**
   * Put the concepts of the initial graph and theirs connections in the DB, reset all the rest.
   * @author Thomas GIOVANNINI
   */
  def putInitialConceptsInDB: Boolean = {
    PropertyDAO.clear
    RelationDAO.clear
    NeedDAO.clear

    /*Property declaration*/
    val propertyInstanciable = Property("Instanciable", PropertyType.Bool, 0).save
    val propertyStrength = Property("Strength", PropertyType.Int, 0).save

    val propertySense = Property("Sense", PropertyType.Int, 5).save
    val propertyDuplicationSpeed = Property("DuplicationSpeed", PropertyType.Double, 10).save
    val propertyWalkingDistance = Property("WalkingDistance", PropertyType.Int, 3).save
    val propertyHunger = Property("Hunger", PropertyType.Double, 5).save
    val propertyFeed = Property("Feed", PropertyType.Double, 6).save
    val propertyComfort = Property("Comfort", PropertyType.Double, 3).save
    val propertyDesire = Property("Desire",PropertyType.Double,11).save
    val propertyFear = Property("Fear", PropertyType.Double, 0).save

    PreconditionManager.initialization()
    InstanceActionManager.initialization()

    val conceptGround = Concept("Ground", List(), List(), List(), DisplayProperty())
    val conceptWater = Concept("Water",
      List(),
      List(ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#86B6B6", 0))
    val conceptEarth = Concept("Earth",
      List(),
      List(ValuedProperty(propertyStrength, 3),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#878377", 1))
    val conceptGrass = Concept("Grass",
      List(propertyDuplicationSpeed),
      List(ValuedProperty(propertyStrength, 40),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#62A663", 8))
    val conceptEdible = Concept("Edible", List(propertyFeed), List(), List(), DisplayProperty())
    val conceptApple = Concept("Apple",
      List(),
      List(ValuedProperty(propertyStrength, 2), ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#A83B36", 20))

    val conceptSheep = Concept("Sheep",
      _properties = List(propertySense,propertyFeed, propertyFear),
      _rules = List(ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#EEE9D6", 16)
    )

    /* Creation of needs */
    val needFood = NeedDAO.save(Need(0L, "Hunger", propertyHunger, priority = 6,
      List(ConsequenceStep(10, Consequence(8, InstanceActionManager.nameToId("_removeInstanceAt").toEffect))),
      List(MeanOfSatisfaction(InstanceActionManager.nameToId("Eat"), conceptSheep),
        MeanOfSatisfaction(InstanceActionManager.nameToId("Eat"), conceptApple),
        MeanOfSatisfaction(InstanceActionManager.nameToId("Move"), conceptSheep),
        MeanOfSatisfaction(InstanceActionManager.nameToId("Move"), conceptApple),
        MeanOfSatisfaction(InstanceActionManager.nameToId("Move"), Concept.any))))
    val needSeaAir = NeedDAO.save(Need(0L, "SeaAir", propertyComfort, priority = 5,
      List(ConsequenceStep(5, Consequence(5, InstanceActionManager.nameToId("_addToProperty").toEffect))),
      List(MeanOfSatisfaction(InstanceActionManager.nameToId("Move"), Concept.any))))

    println("Declaration of concepts...")

    /*Concepts declaration*/
    lazy val conceptMan = Concept("Man",
      List(propertySense, propertyComfort),
      List(ValuedProperty(propertyStrength, 2), ValuedProperty(propertyInstanciable, 1)),
      List(needSeaAir),
      DisplayProperty("#E3B494", 20))
    val conceptPredator = Concept("Predator",
      List(propertyHunger, propertySense),
      List(), List(), DisplayProperty())
    val conceptWolf = Concept("Wolf",
      List(propertySense),
      List(ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#1A1A22", 18))
    val conceptAnimal = Concept("Animal",
      List(propertyWalkingDistance, propertyHunger, propertyFeed, propertySense, propertyDesire),
      List(), List(needFood), DisplayProperty())
    val conceptBush = Concept("Bush",
      List(),
      List(ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#2A6E37", 4))
    val conceptAppleTree = Concept("AppleTree",
      List(),
      List(ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#2F1C13", 9))
    val conceptTree = Concept("Tree",
      List(),
      List(ValuedProperty(propertyStrength, 4),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#483431", 7))
    val conceptFir = Concept("Fir",
      List(),
      List(ValuedProperty(propertyStrength, 4)),
      List(),
      DisplayProperty("#221D1D", 8))
    val conceptVegetable = Concept("Vegetable", List(), List(), List(), DisplayProperty())

    println("Relations declaration...")

    /*Relations declaration*/
    val relationSubtypeOfId = RelationDAO.save("SUBTYPE_OF")
    val relationEatId = RelationDAO.save("ACTION_EAT")
    val relationCutId = RelationDAO.save("ACTION_CUT")
    val relationMoveId = RelationDAO.save("ACTION_MOVE")
    val relationFleeId = RelationDAO.save("ACTION_FLEE")
    val relationProducesId = RelationDAO.save("ACTION_PRODUCE")
    val relationLiveOnId = RelationDAO.save("LIVE_ON")
    val relationFear = RelationDAO.save("MOOD_FEAR")
    val relationProcreate = RelationDAO.save("ACTION_PROCREATE")
    val relationSpread = RelationDAO.save("ACTION_SPREAD")


    println("Adding concepts to graph...")
    /*Storage of the concepts in DB*/
    val addConceptVerification = {
      ConceptDAO.addConceptToDB(conceptMan) &&
      ConceptDAO.addConceptToDB(conceptPredator) &&
      ConceptDAO.addConceptToDB(conceptAnimal) &&
      ConceptDAO.addConceptToDB(conceptWolf) &&
      ConceptDAO.addConceptToDB(conceptSheep) &&
      ConceptDAO.addConceptToDB(conceptGrass) &&
      ConceptDAO.addConceptToDB(conceptEdible) &&
      ConceptDAO.addConceptToDB(conceptApple) &&
      ConceptDAO.addConceptToDB(conceptBush) &&
      ConceptDAO.addConceptToDB(conceptAppleTree) &&
      ConceptDAO.addConceptToDB(conceptTree) &&
      ConceptDAO.addConceptToDB(conceptFir) &&
      ConceptDAO.addConceptToDB(conceptVegetable) &&
      ConceptDAO.addConceptToDB(conceptGround) &&
      ConceptDAO.addConceptToDB(conceptWater) &&
      ConceptDAO.addConceptToDB(conceptEarth)
    }
    /*Creation of the relations in DB*/
    println("Adding relations to graph...")
    val addRelationVerification = {
      NeoDAO.addRelationToDB(conceptAnimal.id, relationMoveId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationSubtypeOfId, conceptAnimal.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationFleeId, conceptPredator.id) &&
        NeoDAO.addRelationToDB(conceptSheep.id, relationFear, conceptPredator.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationEatId, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptPredator.id, relationEatId, conceptSheep.id) &&
      NeoDAO.addRelationToDB(conceptPredator.id, relationSubtypeOfId, conceptAnimal.id) &&
      NeoDAO.addRelationToDB(conceptWolf.id, relationSubtypeOfId, conceptPredator.id) &&
      NeoDAO.addRelationToDB(conceptMan.id, relationSubtypeOfId, conceptPredator.id) &&
      NeoDAO.addRelationToDB(conceptMan.id, relationEatId, conceptApple.id) &&
      NeoDAO.addRelationToDB(conceptMan.id, relationCutId, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptFir.id, relationSubtypeOfId, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptAppleTree.id, relationSubtypeOfId, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptAppleTree.id, relationProducesId, conceptApple.id) &&
      NeoDAO.addRelationToDB(conceptApple.id, relationSubtypeOfId, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptBush.id, relationSubtypeOfId, conceptTree.id) &&
      NeoDAO.addRelationToDB(conceptBush.id, relationSubtypeOfId, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptGrass.id, relationSubtypeOfId, conceptEdible.id) &&
      NeoDAO.addRelationToDB(conceptGrass.id, relationSubtypeOfId, conceptVegetable.id) &&
      NeoDAO.addRelationToDB(conceptTree.id, relationSubtypeOfId, conceptVegetable.id) &&
      NeoDAO.addRelationToDB(conceptWater.id, relationSubtypeOfId, conceptGround.id) &&
      NeoDAO.addRelationToDB(conceptEarth.id, relationSubtypeOfId, conceptGround.id) &&
        NeoDAO.addRelationToDB(conceptMan.id, relationProcreate, conceptGround.id) &&
        NeoDAO.addRelationToDB(conceptWolf.id, relationProcreate, conceptGround.id) &&
        NeoDAO.addRelationToDB(conceptSheep.id, relationProcreate, conceptGround.id) &&
        NeoDAO.addRelationToDB(conceptGrass.id, relationSpread, conceptGround.id)



    }
    val addLiveOnRelationsVerification = {
      NeoDAO.addRelationToDB(conceptMan.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptWolf.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptGrass.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptTree.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptBush.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptAppleTree.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptFir.id, relationLiveOnId, conceptEarth.id) &&
      NeoDAO.addRelationToDB(conceptApple.id, relationLiveOnId, conceptAppleTree.id) &&
      NeoDAO.addRelationToDB(conceptSheep.id, relationLiveOnId, conceptEarth.id)
    }
    val result = addConceptVerification && addRelationVerification && addLiveOnRelationsVerification
    println("Initialization of the graph completed: " + result)
    result
  }
}