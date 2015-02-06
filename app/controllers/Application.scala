package controllers

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Coordinates, Label}
import models.graph.ontology._
import models.map.WorldMap
import org.anormcypher.Neo4jREST
import play.api.mvc._

object Application extends Controller {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  def index = Action {
    putInitialConceptsInDB
    val applicationMap = initialization
    Ok(applicationMap.toJson)
  }

  def initialization = {
    val map = WorldMap(Label("MapOfTheWorld"), "Test map", 15, 20)
    val conceptHerbe = Concept("Herbe", List())
    val conceptArbre = Concept("Herbe", List(Property("Size")))
    for(i <- 0 until 15; j <- 0 until 20){
      val herbe = Instance("Herbe"+i+"_"+j, Coordinates(0, 0), List(), conceptHerbe)
      val coordinates = Coordinates(i, j)
      map.addInstanceAt(herbe, coordinates)
      if(math.random > 0.98){
        val arbre = Instance(
          "Arbre"+i+"_"+j,
          Coordinates(0,0),
          List(ValuedProperty(Property("Size"), (math.random * 10).toInt.toString)),
          conceptArbre)
        map.addInstanceAt(arbre, coordinates)
      }
    }
    map
  }

  /**
   * Put the concepts of the initial graph and theirs connections in the DB, reset all the rest.
   */
  def putInitialConceptsInDB: Unit = {
    val propertyInstanciable      = Property("Instanciable")
    val propertyDuplicationSpeed  = Property("DuplicationSpeed")

    /*Concepts declaration*/
    val conceptMan        = Concept("Man", List(propertyInstanciable))
    val conceptPredator   = Concept("Predator", List())
    val conceptWolf       = Concept("Wolf", List(propertyInstanciable))
    val conceptSheep      = Concept("Sheep", List(propertyInstanciable))
    val conceptAnimal     = Concept("Animal", List(propertyInstanciable))
    val conceptGrass      = Concept("Grass", List(propertyInstanciable, propertyDuplicationSpeed))
    val conceptEdible     = Concept("Edible", List())
    val conceptApple      = Concept("Apple", List(propertyInstanciable))
    val conceptBush       = Concept("Bush", List(propertyInstanciable))
    val conceptAppleTree  = Concept("AppleTree", List(propertyInstanciable))
    val conceptTree       = Concept("Tree", List(propertyInstanciable))
    val conceptFir        = Concept("Fir", List(propertyInstanciable))
    val conceptVegetable  = Concept("Vegetable", List())
    /* Ground used for map generation only. */
    val conceptGround     = Concept("Ground", List())
    val conceptLiquid     = Concept("Liquid", List())
    val conceptSolid      = Concept("Solid", List())

    /*Relations declaration*/
    val relationSubtypeOf   = Relation("SUBTYPE_OF")
    val relationEat         = Relation("EAT")
    val relationCut         = Relation("CUT")
    val relationMove        = Relation("MOVE")
    val relationFlee        = Relation("FLEE")
    val relationGrowOn      = Relation("GROW_ON")
    val relationReproduce   = Relation("REPRODUCES_ITSELF")

    Statement.clearDB.execute
    /*Storage of the concepts*/
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