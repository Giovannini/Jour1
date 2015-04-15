package controllers.map

import controllers.Application
import models.graph.DisplayProperty
import models.graph.concept.{Concept, ConceptDAO}
import models.graph.property.{Property, PropertyDAO, PropertyType, ValuedProperty}
import models.graph.relation.{RelationGraphDAO, RelationSqlDAO}
import models.intelligence.MeanOfSatisfaction
import models.intelligence.consequence.{Consequence, ConsequenceStep}
import models.intelligence.need.{Need, NeedDAO}
import models.interaction.action.{InstanceActionDAO, InstanceActionManager}
import models.interaction.effect.EffectManager
import models.interaction.precondition.PreconditionManager
import models.map.{WorldInit, WorldMap}
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
   * Clear the instances of the world to start a new one
   * @author Julien PRADET
   */
  def clearWorld: Action[AnyContent] = Action {
    Application.map.clear()
    isWorldMapInitialized = false
    Ok("World cleared")
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
      val result = {
        ConceptDAO.clear() &&
           PropertyDAO.clear() &&
           RelationSqlDAO.clear() &&
           NeedDAO.clear() &&
           InstanceActionDAO.clear()
      }
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
    //Todo mettre des valeurs judicieuses au lieu des valeurs arbitraires pour les rules et property
    /*Property declaration*/
    val propertyInstanciable = Property("Instanciable", PropertyType.Bool, 0).save
    val propertyStrength = Property("Strength", PropertyType.Int, 0).save

    val propertySense = Property("Sense", PropertyType.Int, 5).save
    val propertyDuplicationSpeed = Property("DuplicationSpeed", PropertyType.Double, 10).save
    val propertyDuplicaSpeedVal = Property("DuplicaSpeedVal", PropertyType.Double, 5).save

    val propertyWalkingDistance = Property("WalkingDistance", PropertyType.Int, 3).save
    val propertyHunger = Property("Hunger", PropertyType.Double, 5).save
    val propertyWound = Property("Wound", PropertyType.Double, 2).save
    val propertyComfort = Property("Comfort", PropertyType.Double, 3).save
//    val propertyDesire = Property("Desire", PropertyType.Double, 11).save
    val propertyDesireMax = Property("DesireMax", PropertyType.Double, 10).save

    val propertyWoundMax = Property("WoundMax", PropertyType.Double, 4).save
    val propertyFear = Property("Fear", PropertyType.Double, 0).save

    PreconditionManager.initialization()
    InstanceActionManager.initialization()
    EffectManager.initialization()

    ////////////////////////////////   Environment /////////////////////////////
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

    ///////////////////////////////  Other  ////////////////////////////////////
    val needSurvive = NeedDAO.save(Need(0L, "Survive", propertyWound, priority = 6,
      List(ConsequenceStep(4, Consequence(8, EffectManager.nameToId("death")))),
      List()))
    val conceptGrass = Concept("Grass",
      List(
        ValuedProperty(propertySense, 1),
        ValuedProperty(propertyDuplicationSpeed, 10),
        ValuedProperty(propertyDuplicaSpeedVal, 10),
        ValuedProperty(propertyWound, 0),
        ValuedProperty(propertyWoundMax, 4)
      ),
      List(
        ValuedProperty(propertyStrength, 5),
        ValuedProperty(propertyInstanciable, 1)
      ),
      List(needSurvive),
      DisplayProperty("#62A663", 8))
    val conceptEdible = Concept("Edible", List(), List(), List(), DisplayProperty())
    val conceptApple = Concept("Apple",
      List(
        ValuedProperty(propertySense, 1),
        ValuedProperty(propertyWound, 0),
        ValuedProperty(propertyWoundMax, 4)
      ),
      List(
        ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)
      ),
      List(needSurvive),
      DisplayProperty("#A83B36", 20))
    val conceptSheep = Concept("Sheep",
      _properties = List(
        ValuedProperty(propertyWound, 0),
        ValuedProperty(propertyWoundMax, 12),
        ValuedProperty(propertyFear, 0),
        ValuedProperty(propertyHunger, 4),
        ValuedProperty(propertyWalkingDistance, 4)
      ),
      _rules = List(
        ValuedProperty(propertyStrength, 1),
        ValuedProperty(propertyInstanciable, 1),
        ValuedProperty(propertyWoundMax, 12)
      ),
      List(needSurvive),
      DisplayProperty("#EEE9D6", 21)
    )

    ///

    /*################################
     ######  Creation of needs  ######
     #################################*/
    val needFood = NeedDAO.save(Need(0L, "Hunger", propertyHunger, priority = 6,
      List(ConsequenceStep(10, Consequence(8, EffectManager.nameToId("starve"))),
        ConsequenceStep(0, Consequence(2, EffectManager.nameToId("hunger")))),
      List(/*MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Eat"), conceptApple),*/
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Eat"), conceptGrass),
        /*MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), conceptApple),*/
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), conceptGrass),
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), Concept.self),
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), Concept.any))))
    val needMeat = NeedDAO.save(Need(0L, "Hunger", propertyHunger, priority = 6,
      List(ConsequenceStep(10, Consequence(8, EffectManager.nameToId("death"))),
        ConsequenceStep(0, Consequence(2, EffectManager.nameToId("hunger")))),
      List(MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Eat"), conceptSheep),
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), conceptSheep),
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), Concept.self),
        MeanOfSatisfaction(InstanceActionManager.nameToInstanceAction("Move"), Concept.any))))

    println("Declaration of concepts...")

    /*Concepts declaration*/
    /*lazy val conceptMan = Concept("Man",
      List(
        /*ValuedProperty(propertySense, 5),*/
        ValuedProperty(propertyComfort, 3),
        ValuedProperty(propertyHunger, 6),
        ValuedProperty(propertyWalkingDistance, 3),
        ValuedProperty(propertyWound, 0),
        ValuedProperty(propertyDesire, 7)
      ),
      List(
        ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)
      ),
      List(),
      DisplayProperty("#E3B494", 20))*/
    val conceptPredator = Concept("Predator",
      List(ValuedProperty(propertySense, 5)),
      List(), List(needMeat), DisplayProperty())
    val conceptWolf = Concept("Wolf",
      List(
        ValuedProperty(propertyHunger, 5),
        ValuedProperty(propertyWalkingDistance, 4),
        ValuedProperty(propertyWound, 0)
      ),
      List(
        ValuedProperty(propertyStrength, 1),
        ValuedProperty(propertyInstanciable, 1)
      ),
      List(),
      DisplayProperty("#1A1A22", 22))
    val conceptAnimal = Concept("Animal",
      List(ValuedProperty(propertySense, 5), ValuedProperty(propertyComfort, 5), ValuedProperty(propertyDesireMax, 10)),
      List(), List(needFood), DisplayProperty())
    val conceptBush = Concept("Bush",
      List(
        ValuedProperty(propertyWound, 0),
        ValuedProperty(propertyWoundMax, 6)
      ),
      List(
        ValuedProperty(propertyStrength, 1),
        ValuedProperty(propertyInstanciable, 1)
      ),
      List(),
      DisplayProperty("#2A6E37", 4))
    val conceptAppleTree = Concept("AppleTree",
      List(),
      List(ValuedProperty(propertyStrength, 1),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#2F1C13", 9))
    val conceptTree = Concept("Tree",
      List(),
      List(
        ValuedProperty(propertyStrength, 3)
      ),
      List(),
      DisplayProperty("#483431", 7))
    val conceptFir = Concept("Fir",
      List(),
      List(ValuedProperty(propertyStrength, 2),
        ValuedProperty(propertyInstanciable, 1)),
      List(),
      DisplayProperty("#93634C", 8))
    val conceptVegetable = Concept("Vegetable",
      List(),
      List(),
      List(),
      DisplayProperty())

    println("Relations declaration...")
    /*Relations declaration*/
    val relationSubtypeOfId = RelationSqlDAO.save("SUBTYPE_OF")
    val relationEatId = RelationSqlDAO.save("ACTION_EAT")
    //    val relationCutId = RelationSqlDAO.save("ACTION_CUT")
    val relationMoveId = RelationSqlDAO.save("ACTION_MOVE")
    val relationFleeId = RelationSqlDAO.save("ACTION_FLEE")
//    val relationProducesId = RelationSqlDAO.save("ACTION_PRODUCE")
    val relationLiveOnId = RelationSqlDAO.save("LIVE_ON")
    val relationFear = RelationSqlDAO.save("MOOD_FEAR")
//    val relationProcreate = RelationSqlDAO.save("ACTION_PROCREATE")
//    val relationSpread = RelationSqlDAO.save("ACTION_SPREAD")
//    val relationRegenerate = RelationSqlDAO.save("ACTION_REGENERATE")
    //    val relationCreateBow = RelationSqlDAO.save("ACTION_CREATE_BOW")

    println("Adding concepts to graph...")
    /*Storage of the concepts in DB*/
    val addConceptVerification = {
      ConceptDAO.addConceptToDB(Concept.any) &&
      ConceptDAO.addConceptToDB(Concept.self) &&
      //      ConceptDAO.addConceptToDB(conceptMan) &&
      ConceptDAO.addConceptToDB(conceptPredator) &&
      ConceptDAO.addConceptToDB(conceptAnimal) &&
//      ConceptDAO.addConceptToDB(conceptWolf) &&
      ConceptDAO.addConceptToDB(conceptSheep) &&
      ConceptDAO.addConceptToDB(conceptGrass) &&
      ConceptDAO.addConceptToDB(conceptEdible) &&
//      ConceptDAO.addConceptToDB(conceptApple) &&
//      ConceptDAO.addConceptToDB(conceptBush) &&
//      ConceptDAO.addConceptToDB(conceptAppleTree) &&
//      ConceptDAO.addConceptToDB(conceptTree) &&
//      ConceptDAO.addConceptToDB(conceptFir) &&
      ConceptDAO.addConceptToDB(conceptVegetable) &&
      ConceptDAO.addConceptToDB(conceptGround) &&
      ConceptDAO.addConceptToDB(conceptWater) &&
      ConceptDAO.addConceptToDB(conceptEarth)
    }
    /*Creation of the relations in DB*/
    println("Adding relations to graph...")
    val addRelationVerification = {
      RelationGraphDAO.addRelationToDB(conceptAnimal.id, relationMoveId, conceptEarth.id) &&
      RelationGraphDAO.addRelationToDB(conceptSheep.id, relationSubtypeOfId, conceptAnimal.id) &&
      RelationGraphDAO.addRelationToDB(conceptSheep.id, relationFleeId, conceptPredator.id) &&
      RelationGraphDAO.addRelationToDB(conceptSheep.id, relationFear, conceptPredator.id) &&
      RelationGraphDAO.addRelationToDB(conceptSheep.id, relationEatId, conceptEdible.id) &&
      RelationGraphDAO.addRelationToDB(conceptPredator.id, relationEatId, conceptSheep.id) &&
      RelationGraphDAO.addRelationToDB(conceptPredator.id, relationSubtypeOfId, conceptAnimal.id) &&
//      RelationGraphDAO.addRelationToDB(conceptWolf.id, relationSubtypeOfId, conceptPredator.id) &&
      //      RelationGraphDAO.addRelationToDB(conceptMan.id, relationSubtypeOfId, conceptPredator.id) &&
      //      RelationGraphDAO.addRelationToDB(conceptMan.id, relationEatId, conceptApple.id) &&
      //      RelationGraphDAO.addRelationToDB(conceptMan.id, relationCutId, conceptTree.id) &&
//      RelationGraphDAO.addRelationToDB(conceptFir.id, relationSubtypeOfId, conceptTree.id) &&
//      RelationGraphDAO.addRelationToDB(conceptAppleTree.id, relationSubtypeOfId, conceptTree.id) &&
//      RelationGraphDAO.addRelationToDB(conceptAppleTree.id, relationProducesId, conceptApple.id) &&
//      RelationGraphDAO.addRelationToDB(conceptApple.id, relationSubtypeOfId, conceptEdible.id) &&
//      RelationGraphDAO.addRelationToDB(conceptBush.id, relationSubtypeOfId, conceptTree.id) &&
//      RelationGraphDAO.addRelationToDB(conceptBush.id, relationSubtypeOfId, conceptEdible.id) &&
      RelationGraphDAO.addRelationToDB(conceptGrass.id, relationSubtypeOfId, conceptEdible.id) &&
      RelationGraphDAO.addRelationToDB(conceptGrass.id, relationSubtypeOfId, conceptVegetable.id) &&
//      RelationGraphDAO.addRelationToDB(conceptTree.id, relationSubtypeOfId, conceptVegetable.id) &&
      RelationGraphDAO.addRelationToDB(conceptWater.id, relationSubtypeOfId, conceptGround.id) &&
      RelationGraphDAO.addRelationToDB(conceptEarth.id, relationSubtypeOfId, conceptGround.id)
//      RelationGraphDAO.addRelationToDB(conceptMan.id, relationProcreate, conceptGround.id) &&
//      RelationGraphDAO.addRelationToDB(conceptWolf.id, relationProcreate, conceptGround.id) &&
//      RelationGraphDAO.addRelationToDB(conceptSheep.id, relationProcreate, conceptGround.id) &&
//      RelationGraphDAO.addRelationToDB(conceptGrass.id, relationSpread, conceptGround.id) &&
//      RelationGraphDAO.addRelationToDB(conceptGrass.id, relationRegenerate, conceptGrass.id)&&
//      RelationGraphDAO.addRelationToDB(conceptMan.id, relationCreateBow, conceptTree.id)
    }
    val addLiveOnRelationsVerification = {
      //      RelationGraphDAO.addRelationToDB(conceptMan.id, relationLiveOnId, conceptEarth.id) &&
//      RelationGraphDAO.addRelationToDB(conceptWolf.id, relationLiveOnId, conceptEarth.id) &&
      RelationGraphDAO.addRelationToDB(conceptSheep.id, relationLiveOnId, conceptEarth.id) &&
      RelationGraphDAO.addRelationToDB(conceptGrass.id, relationLiveOnId, conceptEarth.id)
//      RelationGraphDAO.addRelationToDB(conceptTree.id, relationLiveOnId, conceptEarth.id) &&
//      RelationGraphDAO.addRelationToDB(conceptBush.id, relationLiveOnId, conceptEarth.id) &&
//      RelationGraphDAO.addRelationToDB(conceptAppleTree.id, relationLiveOnId, conceptEarth.id) &&
//      RelationGraphDAO.addRelationToDB(conceptFir.id, relationLiveOnId, conceptEarth.id) &&
//      RelationGraphDAO.addRelationToDB(conceptApple.id, relationLiveOnId, conceptAppleTree.id) &&
    }
    val result = addConceptVerification && addRelationVerification && addLiveOnRelationsVerification
    println("Initialization of the graph completed: " + result)
    result
  }
}