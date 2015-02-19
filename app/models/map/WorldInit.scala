package models.map

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology._

/**
 * Class with method to initialize the world
 */

object WorldInit {

  /**
   * Get the sum of the strength of all the ground concepts.
   * @author Thomas GIOVANNINI
   * @param groundIdsList the list of the ground strength
   * @return the sum of the strength of all the ground concepts
   */
  def getStrengthSum(groundIdsList: List[Int]): Int = {
    groundIdsList.map(Concept.getById)
      .map {
      case Some(concept) => getStrengthOf(concept)
      case _ => 0
    }.sum
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
    concept.rules
      .find(_.property == propertyStrength)
      .getOrElse(ValuedProperty(propertyStrength, 0))
      .value.asInstanceOf[Int]
  }

  /**
   * Get a list  of tuple containing whatever is "maxOccupe" and its associated concept
   * @author Thomas GIOVANNINI
   * @param lowestMatrixValue the lowest ground strength
   * @param biggestMatrixValue the biggest ground strength
   * @param listGrounds the ground list
   * @return a list of tuple containing whatever is "maxOccupe" and its associated concept
   */
  def repartition(lastBound: Int, lowestMatrixValue: Int, biggestMatrixValue: Int, listGrounds: List[Int], sumStr: Int): List[(Int, Concept)] = {
    listGrounds match {
      case conceptId :: tail => buildRepartitionOfOneConcept(conceptId,tail,lastBound,lowestMatrixValue,biggestMatrixValue,sumStr)
      case _ => List()

    }
  }

  /**
   *  of tuple containing whatever is "maxOccupe" and its associated concept
   * @author Simmon Roncière
   * @param conceptId concept's id put in the map
   * @param listConceptId List of concept's id not put
   * @param lastBound biggest (and so lastest) bound attribuate to a concept
   * @param lowestMatrixValue Matrix lowest value
   * @param biggestMatrixValue Matrix highest value
   * @param sumStrength strength sum of all concepts
   * @return a list of tuple containing whatever is "maxOccupe" and its associated concept
   */
  def buildRepartitionOfOneConcept(conceptId:Int,listConceptId:List[Int],lastBound: Int, lowestMatrixValue: Int, biggestMatrixValue: Int, sumStrength: Int): List[(Int, Concept)] ={
    Concept.getById(conceptId) match {
      case Some(concept) =>
        val maxOccupe = getMaxOccupe(lastBound, lowestMatrixValue, biggestMatrixValue, sumStrength, concept, listConceptId.length)
        (maxOccupe, concept) :: repartition(maxOccupe, lowestMatrixValue, biggestMatrixValue, listConceptId, sumStrength)
      case _ => repartition(lastBound, lowestMatrixValue, biggestMatrixValue, listConceptId, sumStrength)
    }
  }

  /**
   * Get highter bound of a concept
   * @author Thomas Giovannini, Simon Roncière
   * @param lastBound lower bound free
   * @param lowestMatrixValue lowest value in the layer matrix
   * @param biggestMatrixValue bigest value in layer matrix
   * @param sumStr Sum of grounds strength
   * @param concept concept to put in the world
   * @param remainingElements number of remaining element
   * @return max value take up by concept
   */
  def getMaxOccupe(lastBound: Int, lowestMatrixValue: Int, biggestMatrixValue: Int, sumStr: Int, concept: Concept, remainingElements: Int): Int = {
    if (remainingElements > 0)
      lastBound + (getStrengthOf(concept) * (biggestMatrixValue - lowestMatrixValue) / sumStr)
    else biggestMatrixValue
  }

  /**
   *
   * @author Thomas Giovannini
   * @param tuple
   * @return
   */
  def notARepartitionError(tuple: (Int, Concept)): Boolean = {
    tuple._2 != Concept.error
  }

  /**
   * Generate the world map
   * @author Simon Roncière
   * @return List of Instances of the world
   */

  def worldMapGeneration(): Unit = {

    val map = Application.map
    val width = map.width
    val height = map.height
    //WorldInit.worldMapGeneration(width,height).foreach(map.addInstance)

    val frequency = 20
    val octave = 35
    val persistence = 0.5f
    val smoothed = 3
    val outputSize = width

    val layer = Layer.generateLayer(frequency, octave, persistence, smoothed, outputSize)

    val idGround = getGround(Concept.findAll)
    val listGrounds = getGrounds(idGround :: Nil)


    val sumStr = sumStrength(listGrounds)

    val (min, max) = layer.getMinMax
    val repartitionList = repartition(min, min, max, listGrounds, sumStr).sortBy(_._1)

    val terrain = matrixToList(layer.matrix)
      .map {
      createInstance(_, repartitionList)
    }


    terrain.foreach(map.addInstance)
    fillWorldWithInstances(map,getListInstanceSorted().map(tuple=>tuple._1).tail.tail, 1)

  }

  /**
   * get sum of strength of concept
   * @author Simon Roncière
   * @param listConcept list of concept's id whose we want the sum
   * @return sum of strength
   */
  def sumStrength(listConcept: List[Int]): Int = {
    listConcept match {
      case Nil => 0
      case head :: tail => additionHeadStrength(head,tail)


    }
  }

  /**
   * Add the stength of a concept
   * @author Simon Roncière
   * @param conceptId concept which strenght must be added
   * @param listConceptRemaining list of concept remaining to add
   * @return value of sum
   */
  def additionHeadStrength(conceptId : Int, listConceptRemaining : List[Int]): Int ={
    val strength = Concept.getById(conceptId) match {
      case Some(c) => getStrengthOf(c)
      case _ => 0
    }
    strength + sumStrength(listConceptRemaining)
  }

  /**
   * @author Simon Roncière
   * @param matrix Matrix to transform
   * @return List of triplet (value, coordonate_x, coordonate_y)
   */
  def matrixToList(matrix: Array[Array[Int]]): List[(Int, Int, Int)] = {
    matrix.map(_.toList.zipWithIndex).toList.zipWithIndex
      .map {
      indexedLine => indexedLine._1.map(element => (element._1, element._2, indexedLine._2))
    }.flatten
  }

  /**
   * Create a random instance in the map
   * @author Simon Ronciere
   * @param triplet tile to fill
   * @param repartitionList rules of filling
   * @return the instance
   */
  def createInstance(triplet: (Int, Int, Int), repartitionList: List[(Int, Concept)]): Instance = {
    val instance = Instance.createRandomInstanceOf(instanciationTile(triplet._1, repartitionList)).at(Coordinates(triplet._2, triplet._3))
    instance
  }

  /**
   * Get the concept to associate with value
   * @author Simon Roncière
   * @param value value of the tile
   * @param list list of repatition rules for concepts
   * @return concept associate at value
   */
  def instanciationTile(value: Int, list: List[(Int, Concept)]): Concept = {
    list match {
      case Nil => Concept.error
      case _ =>
        if (value <= list.head._1)
          list.head._2
        else
          instanciationTile(value, list.tail)
    }


  }

  /**
   * Get concept where label = "Ground"
   * @param concepts List of concept to browse
   * @return -1 if concept "Ground" not found, else id of the concept
   */
  def getGround(concepts: List[Concept]): Int = {
    concepts.find(_.label == "Ground") match {
      case Some(c) => c.id
      case _ => -1
    }
  }

  /**
   * get all grounds of the map
   * @author Simon Roncière
   * @param listID concept's id list where we search grounds
   * @return list of every grounds
   */
  def getGrounds(listID: List[Int]): List[Int] = {
    listID match {
      case Nil => Nil
      case head :: tail => val listChild = Concept.getChildren(head)
        if (listChild.isEmpty) head :: getGrounds(tail)
        else getGrounds(tail ::: listChild.map(_.id))
    }


  }

  ///////////////////////////:: OBJECT - RANDOM :://///////////////////////////////


  /**
   * Get id list of concept instanciable
   * @author Simon Roncière
   * @param listConcepts list concept where we get instanciables
   * @return
   */
  def getInstanciable(listConcepts: List[Concept]): List[Concept] = {
    val propertyInstanciable = Property("Instanciable", "Boolean", false)
    listConcepts.filter(concept => concept.rules.contains(ValuedProperty(propertyInstanciable, true)))
  }

  /**
   * Get id list of concept to instanciate
   * @author Simon Roncière
   * @param grounds list of grounds
   * @param concepts list of instanciable concept
   * @return list concept to instanciate
   */
  def getInstance(grounds: List[Int], concepts: List[Concept]): List[Int] = {
    val allInst: List[Concept] = getInstanciable(concepts)
    allInst match {
      case Nil => List()
      case head :: tail =>getConceptIfNotGround(grounds,head,tail)
    }
  }

  /**
   * Get the concept if he is not a Ground
   * @param grounds list of grounds
   * @param concept concept to verify
   * @param tail rest of concept to study
   * @return List of concept which is not a ground
   */
  def getConceptIfNotGround(grounds: List[Int], concept:Concept,tail : List[Concept]): List[Int] ={
    if (grounds.contains(concept.id)) getInstance(grounds, tail)
    else concept.id :: getInstance(grounds, tail)
  }

  /**
   * get number of tile to put an instance of concept
   * @author Simon Ronciere
   * @param listInstanciable list of id to instanciate, 
   * @param sumStrength sum of all strength
   * @param nbTile number of Tile in the map
   * @param vide percentage of empty tile
   * @return list of tuple associate concept to number of instance to put
   */
  def getNbTileByInstance(listInstanciable: List[Int], sumStrength: Int, nbTile: Int, vide: Int): List[(Int, Concept)] = {
    if (sumStrength == 0 || vide == 0) {
      println("erreur, mauvais paramètre : vide doit valoir au moins 1. Et la somme des force doit être non nul")
      Nil
    } else {
      listInstanciable match {
        case Nil => List()
        case head :: tail => BuildListNumberInstanceByConcept(head,tail,sumStrength,nbTile,vide)
      }
    }
  }

  /**
   * Build the list of Number concept to create for each Concept depending on strength of the concept
   * @author Simon Ronciere
   * @param head
   * @param tail list of id to remaining instance,
   * @param sumStrength sum of all strength
   * @param nbTile number of Tile in the map
   * @param vide percentage of empty tile
   * @return list of tuple associate concept to number of instance to put
   */
  def BuildListNumberInstanceByConcept(head:Int, tail : List[Int], sumStrength: Int, nbTile:Int, vide :Int): List[(Int, Concept)] ={
    Concept.getById(head) match {
      case Some(concept) =>
        val str = getStrengthOf(concept)
        (str * nbTile / (sumStrength * vide), concept) :: getNbTileByInstance(tail, sumStrength, nbTile, vide)
      case _ => getNbTileByInstance(tail, sumStrength, nbTile, vide)
    }
  }

  /**
   *  Instanciation of all concept with random position and values
   * @author Simon Ronciere
   * @param grounds list of grounds
   * @param heigth heigth of map
   * @param width width of map
   * @return List of Instances of the world
   */
  def randomObjectInstanciation(grounds: List[Int], heigth: Int, width: Int): List[Instance] = {
    val nbTile = heigth * width
    val listInstance = getInstance(grounds, Concept.findAll)
    val sumStr = sumStrength(listInstance)
    val listNumberEachInstance = getNbTileByInstance(listInstance, sumStr, nbTile, 2)
    listNumberEachInstance.map(tuple => randomPutInstanciesOfOneConcept(tuple._1, tuple._2, heigth, width)).flatten
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
  def randomPutInstanciesOfOneConcept(nbTile: Int, concept: Concept, heigth: Int, width: Int): List[Instance] = {
    val r = scala.util.Random
    Seq.fill(nbTile)(Instance.createRandomInstanceOf(concept).at(Coordinates(r.nextInt(heigth), r.nextInt(width)))).toList
  }

  ///////////////////////////:: OBJECT - Good world :://///////////////////////////////
  
  /**
   * Fill the world with concepts
   * @param map map with instances fill yet
   * @param listConcept sorted list of concept to instanciate
   * @param emptyParam empty percent
   */
  def fillWorldWithInstances(map:WorldMap,listConcept:List[Int], emptyParam :Int): Unit ={
    if(listConcept.nonEmpty){
      //instanciate first concept
      fillInstancesLivingeOnOne(map,listConcept.head, emptyParam)
      .foreach(map.addInstance)
      //instanciate rest
      fillWorldWithInstances(map,listConcept.tail,emptyParam)
    }
  }
  /**
   * Instanciate a specific concept
   * @param map map with instances fill yet
   * @param conceptToPutId concept to instanciate
   * @param emptyParam empty percent
   * @return List of instances of the concept
   */
  def fillInstancesLivingeOnOne(map:WorldMap,conceptToPutId:Int, emptyParam :Int): List[Instance] ={
    Concept.getById(conceptToPutId) match {
      case Some(concept) =>fillInstancesDependingGrounds(map, conceptToPutId,emptyParam)
      case _ => Nil
    }
  }
  /**
   * Instanciate concept on each life grounds 
   * @param map map with instances fill yet
   * @param conceptToPutId concept to instanciate
   * @param emptyParam empty percent
   * @return list of instances of concept
   */
  def fillInstancesDependingGrounds(map:WorldMap,conceptToPutId:Int,emptyParam:Int): List[Instance] ={
    val listOfLifeGround = liveOnConcept(conceptToPutId)
    listOfLifeGround.map(idground => putInstanceForeachGround(map.getInstancesOf(idground),idground,conceptToPutId,emptyParam)).flatten
  }
  /**
   * Instanciate concept on a life ground
   * @param listInstanceGround list of ground instances
   * @param idGround ground on which we put the concept
   * @param conceptToPutId concept to instanciate   
   * @param emptyParam empty percent
   * @return list of instances of concept
   */
  def putInstanceForeachGround(listInstanceGround: List[Instance],idGround:Int,conceptToPutId:Int, emptyParam:Int): List[Instance] ={
    val listConceptLivingWithMe = conceptLivingOn(idGround)
    buildListInstances(listInstanceGround,listConceptLivingWithMe,conceptToPutId,emptyParam)
  }

  /**
   * Build list of Instances of the concept
   * @param listInstanceGround list of ground instances
   * @param listConceptLivingWithMe concept living in the same environment
   * @param conceptToPutId concept to instanciate   
   * @param emptyParam empty percent
   * @return list of instances of concept
   */
  def buildListInstances(listInstanceGround:List[Instance],listConceptLivingWithMe: List[Int],conceptToPutId:Int, emptyParam :Int): List[Instance] ={
    if (!listConceptLivingWithMe.isEmpty){
      val sumStr = sumStrength(listConceptLivingWithMe)
      Concept.getById(conceptToPutId) match {
        case Some(concept) =>
          val str = getStrengthOf(concept)
          val nbTile = str * listInstanceGround.size / (sumStr * emptyParam)
          putInstanciesOfOneConcept(nbTile, concept, listInstanceGround.map(_.coordinates), listInstanceGround.size)
        case _ => List()
      }
    }else{List()}
  }

  /**
   * Get list of life place of a concept
   * @param conceptId concept whose we search the life evironment
   * @return list of Concept Id's life place of a concept
   */
  def liveOnConcept(conceptId:Int): List[Int] ={
    val toto = Concept.getRelationsFrom(conceptId)
      .filter(tuple => tuple._1.label=="LIVE_ON")
      .map(tuple => tuple._2.id)
    toto
  }
  /**
   * Get list of concepts living on a concept
   * @param conceptId environment whose we search population
   * @return list of Concept Id's living on
   */
  def conceptLivingOn(conceptId:Int): List[Int] ={
    val toto = Concept.getRelationsTo(conceptId)
      .filter(tuple => tuple._1.label=="LIVE_ON")
      .map(tuple => tuple._2.id)
    toto
  }

  /**
   * Build list of random instance for on concept
   * @param nbTile Number of tile to fill with the concept
   * @param concept concept to instanciate
   * @param listTile list of tile where we put the concept
   * @param size number of tile possible for put
   * @return
   */
  def putInstanciesOfOneConcept(nbTile: Int, concept: Concept, listTile:List[Coordinates], size:Int): List[Instance] = {
    val r = scala.util.Random
    Seq.fill(nbTile)(Instance.createRandomInstanceOf(concept).at(listTile(r.nextInt(size)))).toList
  }

  ///////////////////////////:: Util - Tools :://///////////////////////////////
  /**
   * Function to get list of Object instanciables in the world, sort by order of appearance
   * @author Simon Roncière
   * @return list of Object instanciables in the world, sort by order of appearance
   */
  def getListInstanceSorted(): List[(Int,Int)] ={
    val listOfInstanciable = getInstanciable(Concept.findAll)
    val listLiveOnRelationTriplets = getLiveOnRelationTriplets(listOfInstanciable)
    val mapConceptOrder=buildMap(listLiveOnRelationTriplets, listOfInstanciable)
    mapConceptOrder.toList.sortBy(_._2)
  }

  /**
   * Function which return for each instanciable concept the list of relation "LIVE_ON" and the two concepts associates
   * @author Simon Roncière
   * @param listOfInstanciable List of instanciable of in the world
   * @return list of tuplet (Concept, (relation,Concept))
   */
  def getLiveOnRelationTriplets(listOfInstanciable:List[Concept]): List[(Concept,List[(Relation,Concept)])] ={
    listOfInstanciable.map(c=>(c,Concept.getRelationsFrom(c.id)))
      .map(tuple=>(tuple._1,tuple._2.filter(tuple => tuple._1.label=="LIVE_ON")))
  }

  /**
   * Build the map ordering appearance of concepts in the world
   * @param listLiveOnRelationTriplets list of tuplet (Concept, (relation,Concept)) where relation is LIVE_ON
   * @param listOfInstanciable List of instanciable of in the world
   * @return map of order concept
   */
  def buildMap(listLiveOnRelationTriplets:List[(Concept,List[(Relation,Concept)])],listOfInstanciable:List[Concept]): collection.mutable.Map[Int, Int] ={
    val mapConceptAndApparitionOrder: collection.mutable.Map[Int, Int] = collection.mutable.Map.empty[Int, Int]
    listOfInstanciable.map(c => c.id).foreach(s => mapConceptAndApparitionOrder(s)=0)
    listLiveOnRelationTriplets.foreach(relation => calculateOrderOfOneConcept(relation._1,listLiveOnRelationTriplets,mapConceptAndApparitionOrder))
    mapConceptAndApparitionOrder
  }

  /**
   * Calculate Order Value of on Concept
   * @param concept Concept to evaluate
   * @param listLiveOnRelationTriplets list of concept to find preceding concept to instanciate
   * @param mapOrder map with every order
   * @return value of the concept
   */
  def calculateOrderOfOneConcept(concept : Concept,listLiveOnRelationTriplets:List[(Concept,List[(Relation,Concept)])],mapOrder:collection.mutable.Map[Int, Int]): Int ={
    val relation = listLiveOnRelationTriplets.find(tuple=>tuple._1==concept)
    relation match {
      case Some(relation) =>getOrderOfConcept(relation,listLiveOnRelationTriplets,mapOrder)
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
  //Attention en cas de refractoring, imbriquer les if n'est pas rentable
  def getOrderOfConcept(relation:(Concept,List[(Relation,Concept)]),listLiveOnRelationTriplets:List[(Concept,List[(Relation,Concept)])],mapOrder:collection.mutable.Map[Int, Int]): Int ={
    if (relation._2.isEmpty){
      if (mapOrder(relation._1.id)<1){
        mapOrder(relation._1.id)=1
      }
    }
    else{ mapOrder(relation._1.id)=maxOrdre(relation._2.map(tuple =>tuple._2),listLiveOnRelationTriplets,mapOrder)+1 }
    mapOrder(relation._1.id)
  }

  /**
   * get highest value order
   * @param listConceptPrecedent list of every concept which previous the concept that whose we want know the order
   * @param listLiveOnRelationTriplets list of concept to find preceding concept to instanciate
   * @param mapOrder map with every order
   * @return value of the concept
   */
  def maxOrdre(listConceptPrecedent:List[Concept],listLiveOnRelationTriplets:List[(Concept,List[(Relation,Concept)])],mapOrder:collection.mutable.Map[Int, Int]): Int ={
    listConceptPrecedent.map(c => (c,calculateOrderOfOneConcept(c,listLiveOnRelationTriplets,mapOrder))).sortBy(- _._2).head._2
  }

}