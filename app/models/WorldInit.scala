package models

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology._

import scala.util.Random

/**
 * Class with method to initialize the world
 */

object WorldInit {
  val map = Application.map
  val width = map.width
  val height = map.height
  val frequency = 20
  val octave = 35
  val persistence = 0.5f
  val smoothed = 3
  val outputSize = width

  val parameterOfEmptiness = 1

  /**
   * Generate the world map
   * @author Simon Roncière
   * @return List of Instances of the world
   */
  def worldMapGeneration(): Unit = {
    val allGroundsConcepts = getGroundConcept(Concept.findAll).getDescendance
    val instanciableConcepts = getInstanciableConcepts diff allGroundsConcepts
    println("instanciable concepts: " + instanciableConcepts.length)
    generateGround(allGroundsConcepts)
    //Take a lot of time
    instanciableConcepts.foreach(fillWorldWithInstances(map, _))
  }

  /**
   *
   * @param allGroundsConcepts
   */
  def generateGround(allGroundsConcepts: List[Concept]) {
    val layer = Layer.generateLayer(frequency, octave, persistence, smoothed, outputSize)
    val layerExtremums = layer.getExtremums
    val repartitionList = repartition(layerExtremums, allGroundsConcepts).sortBy(_._1)
    matrixToList(layer.matrix)
      .map(createInstance(_, repartitionList))
      .foreach(map.addInstance)
  }

  /**
   * Get the strength of a given concept
   * @author Thomas GIOVANNINI
   * @param concept from which the strength is needed
   * @return the strength og the concept if it has one
   *         0 else
   */
  def getStrengthOf(concept: Concept): Int = {
    val propertyStrength = Property("Strength", "Int", 0)
    concept.getRuleValue(propertyStrength).asInstanceOf[Int]
  }

  /**
   * Get the strength sum of a list of concepts
   * @author Thomas GIOVANNINI
   * @param listConcept list of concept's ids
   * @return the sum of the strength of given concepts
   */
  def computeSumStrength(listConcept: List[Concept]): Int = {
    listConcept.map(getStrengthOf).sum
  }

  /**
   * Get a list  of tuple containing whatever is "maxOccupe" and its associated concept
   * @author Thomas GIOVANNINI
   * @param matrixExtremums the lowest ground strength
   * @param listGrounds the ground list
   * @return a list of tuple containing whatever is "maxOccupe" and its associated concept
   */
  def repartition(matrixExtremums: (Int, Int), listGrounds: List[Concept]): List[(Int, Concept)] = {
    val sumStrength = computeSumStrength(listGrounds)
    val matrixMinimum = matrixExtremums._2
    repartitionStream(matrixMinimum, matrixExtremums, listGrounds, sumStrength)
      .take(listGrounds.length)
      .toList
  }

  /**
   * Create a stream that construct the repartition list
   * @author Thomas GIOVANNINI
   * @param lastBound computed for the last concept
   * @param matrixExtremums min and max values on the layer
   * @param listGround to repartite
   * @param sumStrength of all the concepts
   * @return a stream containing the repartitions values
   */
  def repartitionStream(lastBound: Int, matrixExtremums: (Int, Int), listGround: List[Concept], sumStrength: Int)
  : Stream[(Int, Concept)] = {
    val newBound = getBounds(lastBound, matrixExtremums, sumStrength, listGround)
    val newListGround = if (listGround.tail.nonEmpty) listGround.tail else List(Concept.error)
    (newBound, listGround.head) #:: repartitionStream(newBound, matrixExtremums, newListGround, sumStrength)
  }

  /**
   * Get higher bound of a concept
   * @author Thomas GIOVANNINI, Simon Roncière
   * @param lastBound lower bound free
   * @param matrixExtremums maximum and minimum value in the layer matrix
   * @param sumStrength Sum of grounds strength
   * @param remainingElements number of remaining element
   * @return max value take up by concept
   */
  def getBounds(lastBound: Int, matrixExtremums: (Int, Int), sumStrength: Int, remainingElements: List[Concept]): Int = {
    if (remainingElements.length > 1)
      lastBound + (getStrengthOf(remainingElements.head) * (matrixExtremums._1 - matrixExtremums._2) / sumStrength)
    else matrixExtremums._1
  }

  /**
   * @author Simon Roncière
   * @param matrix Matrix to transform
   * @return List of triplet (value, coordinate_x, coordinate_y)
   */
  def matrixToList(matrix: Array[Array[Int]]): List[(Int, Int, Int)] = {
    matrix.map(_.zipWithIndex)
      .zipWithIndex
      .flatMap(indexedLine => indexedLine._1.map(element => (element._1, element._2, indexedLine._2)))
      .toList
  }

  /**
   * Create a random instance in the map
   * @author Simon Ronciere
   * @param triplet tile to fill
   * @param repartitionList rules of filling
   * @return the instance
   */
  def createInstance(triplet: (Int, Int, Int), repartitionList: List[(Int, Concept)]): Instance = {
    Instance.createRandomInstanceOf(getAssociatedConcept(triplet._1, repartitionList))
      .at(Coordinates(triplet._2, triplet._3))
  }

  /**
   * Get the concept to associate with value
   * @author Simon Roncière
   * @param value value of the tile
   * @param repartitionList list of repatition rules for concepts
   * @return concept associate at value
   */
  def getAssociatedConcept(value: Int, repartitionList: List[(Int, Concept)]): Concept = {
    repartitionList.dropWhile(_._1 < value).head match {
      case (value: Int, concept: Concept) => concept
      case _ => Concept.error
    }
  }

  /**
   * Get concept where label = "Ground"
   * @param concepts List of concept to browse
   * @return the concept which label is Ground if it exists
   *         the error concept else
   */
  // TODO: pas propre
  def getGroundConcept(concepts: List[Concept]): Concept = {
    concepts.find(_.label == "Ground")
      .getOrElse(Concept.error)
  }


  ///////////////////////////:: OBJECT - RANDOM :://///////////////////////////////
  /**
   * Get id list of concept instanciable
   * @author Simon Roncière
   * @param listConcepts list concept where we get instanciables
   * @return
   */
  def getInstanciableConceptsInList(listConcepts: List[Concept]): List[Concept] = {
    val propertyInstanciable = Property("Instanciable", "Boolean", false)
    listConcepts.filter {
      _.rules.contains(ValuedProperty(propertyInstanciable, true))
    }
  }

  /**
   * Get id list of concept to instanciate which are not grounds
   * @author Simon Roncière
   * @param grounds list of grounds
   * @param concepts list of instanciable concept
   * @return list concept to instanciate
   */
  def getInstance(grounds: List[Int], concepts: List[Concept]): List[Int] = {
    concepts.map(_.id) diff grounds
  }

  /**
   * get number of tile to put an instance of concept
   * @author Simon Ronciere
   * @param instanciableConcepts list of id to instanciate, 
   * @param worldStrength strength of the world, allowing empty tiles
   * @param nbTile number of Tile in the map
   * @return list of tuple associate concept to number of instance to put
   */
  def getNumberOfInstancesForEachConcept(instanciableConcepts: List[Concept], worldStrength: Int, nbTile: Int): List[(Int, Concept)] = {
    if (worldStrength == 0) Nil
    else instanciableConcepts.filter(_ != Concept.error)
      .map { concept =>
      val strength = getStrengthOf(concept)
      (strength * nbTile / worldStrength, concept)
    }
  }

  /**
   * Instanciation of all concept with random position and values
   * @author Simon Ronciere
   * @param grounds list of grounds
   * @param height height of map
   * @param width width of map
   * @return List of Instances of the world
   */
  def randomObjectInstanciation(grounds: List[Concept], height: Int, width: Int): List[Instance] = {
    val nbTile = height * width
    val instanciableConcepts = Concept.findAll diff grounds
    val sumStr = computeSumStrength(instanciableConcepts)
    getNumberOfInstancesForEachConcept(instanciableConcepts, sumStr * 2, nbTile)
      .map(tuple => randomlyPutInstancesOfOneConcept(tuple._1, tuple._2, height, width))
      .flatten
  }

  /**
   *
   * @author Simon Roncière
   * @param nbTile number of Tile to fill
   * @param concept concept to instanciate
   * @param heigth heigth of map
   * @param width width of map
   * @return List of instances of the concept
   */
  def randomlyPutInstancesOfOneConcept(nbTile: Int, concept: Concept, heigth: Int, width: Int): List[Instance] = {
    val r = scala.util.Random
    Seq.fill(nbTile) {
      Instance.createRandomInstanceOf(concept)
        .at(Coordinates(r.nextInt(heigth),
        r.nextInt(width)))
    }.toList
  }

  ///////////////////////////:: OBJECT - Good world :://///////////////////////////////

  /**
   * Fill the world with concepts
   * @param worldMap map with instances fill yet
   * @param concept concept to instanciate
   */
  def fillWorldWithInstances(worldMap: WorldMap, concept: Concept): Unit = {
    //instanciate first concept
    val instances = createInstances(worldMap, concept)
    println("instances: " + instances.length)
    instances.foreach(worldMap.addInstance)
    //instanciate rest

  }

  /**
   * Instanciate a specific concept
   * @param worldMap map with instances fill yet
   * @param conceptToInstanciate concept to instanciate
   * @return List of instances of the concept
   */
  def createInstances(worldMap: WorldMap, conceptToInstanciate: Concept): List[Instance] = {
    val livingPlaces = getLivingPlacesIdsFor(conceptToInstanciate.id)
    livingPlaces.flatMap { livingPlaceConceptId =>
      val instancesOfLivingPlace = worldMap.getInstancesOf(livingPlaceConceptId)
      val listConceptLivingOnSameGrounds = getConceptsLivingOn(livingPlaceConceptId)
      buildInstancesList(instancesOfLivingPlace, listConceptLivingOnSameGrounds, conceptToInstanciate)
    }
  }

  /**
   * Build list of Instances of the concept
   * @param instancesOfLivingPlace list of ground instances
   * @param conceptsLivingOnSameGrounds concept living in the same environment
   * @param conceptToInstanciate concept to instanciate
   * @return list of instances of concept
   */
  def buildInstancesList(instancesOfLivingPlace: List[Instance], conceptsLivingOnSameGrounds: List[Concept], conceptToInstanciate: Concept): List[Instance] = {
    if (conceptsLivingOnSameGrounds.nonEmpty) {
      val strengthSum = computeSumStrength(conceptsLivingOnSameGrounds)
      val strengthOfConceptToInstanciate = getStrengthOf(conceptToInstanciate)
      val nbTile = (strengthOfConceptToInstanciate * instancesOfLivingPlace.size) / (strengthSum * parameterOfEmptiness)
      val result = (0 until nbTile).map(_ => putInstanciesOfOneConcept(conceptToInstanciate, instancesOfLivingPlace.map(_.coordinates))).toList
      result
    } else List()
  }

  /**
   * Get list of life place of a concept
   * @param conceptId concept whose we search the life evironment
   * @return list of Concept Id's life place of a concept
   */
  def getLivingPlacesIdsFor(conceptId: Int): List[Int] = {
    Concept.getRelationsFrom(conceptId)
      .filter(_._1 == Relation("LIVE_ON"))
      .map(_._2.id)
  }

  /**
   * Get list of concepts living on a concept
   * @param conceptId environment whose we search population
   * @return list of Concept Id's living on
   */
  def getConceptsLivingOn(conceptId: Int): List[Concept] = {
    Concept.getRelationsTo(conceptId)
      .filter(tuple => tuple._1.label == "LIVE_ON")
      .map(tuple => tuple._2)
  }

  /**
   * Build list of random instance for on concept without two instances on same tile
   * @param concept concept to instanciate
   * @param instancesOfLivingPlace list of tile where we put the concept
   * @return
   */
  def putInstanciesOfOneConcept(concept: Concept, instancesOfLivingPlace: List[Coordinates]): Instance = {
    // TODO shuffle take a lot of time and this is where the problem is
    val randomCoordinate = Random.shuffle(instancesOfLivingPlace).head
      Instance.createRandomInstanceOf(concept).at(randomCoordinate)
  }


  ///////////////////////////:: Util - Tools :://///////////////////////////////
  /**
   * Function to get list of Object instanciables in the world, sorted by order of appearance
   * @author Simon Roncière
   * @return list of Object instanciables in the world, sort by order of appearance
   */
  def getInstanciableConcepts: List[Concept] = {
    val instanciableConcepts = getInstanciableConceptsInList(Concept.findAll)
    val listLiveOnRelationTriplets = getLiveOnRelationTriplets(instanciableConcepts)
    getConceptsByAppearanceOrder(listLiveOnRelationTriplets, instanciableConcepts)
  }

  /**
   * Function which return for each instanciable concept the list of relation "LIVE_ON" and the two concepts associates
   * @author Simon Roncière
   * @param listOfInstanciable List of instanciable of in the world
   * @return list of tuplet (Concept, (relation,Concept))
   */
  def getLiveOnRelationTriplets(listOfInstanciable: List[Concept]): List[(Concept, List[(Relation, Concept)])] = {
    listOfInstanciable.map(c => (c, Concept.getRelationsFrom(c.id)))
      .map(tuple => (tuple._1, tuple._2.filter(tuple => tuple._1.label == "LIVE_ON")))
  }

  /**
   * Build the map ordering appearance of concepts in the world
   * @param listLiveOnRelationTriplets list of tuplet (Concept, (relation,Concept)) where relation is LIVE_ON
   * @param instanciableConceptsList List of instanciable of in the world
   * @return map of order concept
   */
  def getConceptsByAppearanceOrder(listLiveOnRelationTriplets: List[(Concept, List[(Relation, Concept)])], instanciableConceptsList: List[Concept]): List[Concept] = {
    val conceptToApparitionOrderMap = collection.mutable.Map.empty[Concept, Int]
    instanciableConceptsList.foreach(conceptToApparitionOrderMap(_) = 0)
    listLiveOnRelationTriplets.foreach(relationTriplet => computeAppearanceOrder(relationTriplet._1, listLiveOnRelationTriplets, conceptToApparitionOrderMap))
    conceptToApparitionOrderMap.toList.sortBy(_._2).map(_._1)
  }

  /**
   * Compute order of a Concept
   * @param concept Concept to evaluate
&   * @param mapOrder map with every order
   * @return value of the concept
   */
  def computeAppearanceOrder(concept: Concept, listLiveOnRelationTriplets: List[(Concept, List[(Relation, Concept)])], mapOrder: collection.mutable.Map[Concept, Int]): Int = {
    listLiveOnRelationTriplets.find(_._1 == concept) match {
      case Some(relationTriplet) => getOrderOfConcept(relationTriplet, listLiveOnRelationTriplets, mapOrder)
      case _ => 0
    }
  }

  /**
   * Calculate Order Value of on Concept after matching
   * @param relation relation of the concept
   * @param listLiveOnRelationTriplets list of concept to find preceding concept to instanciate
   * @param mapOrder map with every order
   * @return value of the concept
   */
  def getOrderOfConcept(relation: (Concept, List[(Relation, Concept)]), listLiveOnRelationTriplets: List[(Concept, List[(Relation, Concept)])], mapOrder: collection.mutable.Map[Concept, Int]): Int = {
    mapOrder(relation._1) =
      if (relation._2.isEmpty) math.max(mapOrder(relation._1), 1)
      else maxOrdre(relation._2.map(_._2), listLiveOnRelationTriplets, mapOrder) + 1
    mapOrder(relation._1)
  }

  /**
   * get highest value order
   * @param listConceptPrecedent list of every concept which previous the concept that whose we want know the order
   * @param listLiveOnRelationTriplets list of concept to find preceding concept to instanciate
   * @param mapOrder map with every order
   * @return value of the concept
   */
  def maxOrdre(listConceptPrecedent: List[Concept], listLiveOnRelationTriplets: List[(Concept, List[(Relation, Concept)])], mapOrder: collection.mutable.Map[Concept, Int]): Int = {
    listConceptPrecedent.map(concept => (concept, computeAppearanceOrder(concept, listLiveOnRelationTriplets, mapOrder))).sortBy(-_._2).head._2
  }

}