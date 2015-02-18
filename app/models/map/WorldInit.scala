package models.map

import models.graph.custom_types.Coordinates
import models.graph.ontology.{ValuedProperty, Instance, Property, Concept}

/**
 * Class with method to initialize the world
 */

object WorldInit {

  /**
   * Get the sum of the surface strength of all the ground concepts.
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
   * Get the surface strength of a given concept
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
   * @param matrixExtremum a tuple containing the lowest and biggest ground surface strength
   * @param groundsConceptsIDs the ground list
   * @return a list of tuple containing whatever is "maxOccupe" and its associated concept
   */
  def repartition(lastRepartitionBound: Int,
                  matrixExtremum: (Int, Int),
                  groundsConceptsIDs: List[Int],
                  sumStrength: Int): List[(Int, Concept)] = {
    groundsConceptsIDs match {
      case conceptId::tail =>
        val concept   = Concept.getById(conceptId).getOrElse(Concept.error)
        val repartitionBound = getRepartitionBound(lastRepartitionBound, matrixExtremum, sumStrength, concept, tail.length)
        (repartitionBound, concept) :: repartition(repartitionBound, matrixExtremum, tail, sumStrength)
      case _ => List()
    }
  }

  /**
   * Get upper bound of a concept
   * @author Thomas Giovannini, Simon Roncière
   * @param lastBound lower bound free
   * @param matrixMinMax lowest and highest values in the layer matrix
   * @param sumStr Sum of grounds strength
   * @param concept concept to put in the world
   * @param remainingElements number of remaining element
   * @return max value take up by concept
   */
  def getRepartitionBound(lastBound: Int, matrixMinMax: (Int, Int), sumStr: Int, concept: Concept, remainingElements: Int): Int = {
    val (matrixMinValue, matrixMaxValue) = matrixMinMax
    if (remainingElements > 0)
      lastBound + (getStrengthOf(concept) * (matrixMaxValue - matrixMinValue) / sumStr)
    else matrixMaxValue
  }

  /**
   * Generate the world map
   * @author Simon Roncière
   * @param width width of the map
   * @param height height of the map
   * @return List of Instances of the world
   */
  def worldMapGeneration(width: Int, height: Int): List[Instance] = {
    val frequency = 20
    val octave = 35
    val persistence = 0.5f
    val smoothed = 3
    val outputSize = width

    val layer = Layer.generateLayer(frequency, octave, persistence, smoothed, outputSize)
    val listGrounds = getGrounds

    val sumStr = sumStrength(listGrounds)
    val matrixMinMax = layer.getMinMax
    val repartitionList = repartition(matrixMinMax._1, matrixMinMax, listGrounds, sumStr).sortBy(_._1)

    val terrain = matrixToList(layer.matrix)
      .map(createInstance(_, repartitionList))

    terrain ::: recursiveConcept(terrain,listGrounds, 1)
    //randomObjectInstanciation(listGrounds, outputSize, outputSize)
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
   * Retrieve the Ground root cocnept from the graph
   * @return id of the concept "Ground"
   *         ErrorConcept id else
   */
  def getConceptGroundID: Int = {
    val allConcepts = Concept.findAll
    allConcepts.find(_.label == "Ground")
      .getOrElse(Concept.error)
      .id
  }

  /**
   * get all grounds of the map
   * @author Simon Roncière
   * @return list of every grounds
   */
  def getGrounds: List[Int] = {
    val conceptGround = getConceptGroundID
    //println("Ground ID: " + conceptGround)
    Concept.getDescendance(conceptGround).map(_.id)
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
   *
   * @param listTile liste de toute les instance de terrain de la map
   * @param listGround liste des types de terrain
   * @param vide pourcentage de vide
   * @return liste des instances
   */
  def recursiveConcept(listTile:List[Instance],listGround:List[Int], vide :Int): List[Instance] ={
    listGround.map(fillInstancesLivingeOnOne(listTile,_, vide)).flatten
  }

  /**
   * Pour un terrain donné on cherche
   * @param listTile
   * @param groundID
   * @param vide
   * @return
   */
  def fillInstancesLivingeOnOne(listTile:List[Instance],groundID:Int, vide :Int): List[Instance] ={
    Concept.getById(groundID) match {
      case Some(concept) => fillmap(listTile.filter(_.label == concept.label),conceptLivingOn(groundID),vide)
      case _ => Nil
    }
  }

  /**
   *
   * @param listTile
   * @param listConcept
   * @param vide
   * @return
   */
  def fillmap(listTile:List[Instance],listConcept: List[Int], vide :Int): List[Instance] ={
    if (!listConcept.isEmpty){
      val nbTile= listTile.size
      val sumStr = sumStrength(listConcept)
      val listtruc = getNbTileByInstance(listConcept, sumStr, nbTile, vide)
      complete(listTile.map(instance => instance.coordinates),listtruc)
    }else{List()}
  }

  def conceptLivingOn(conceptId:Int): List[Int] ={
    Concept.getRelationsTo(conceptId).filter(tuple => tuple._1.label=="LIVE_ON").map(tuple => tuple._2.id)
  }
  
  def complete(listTile:List[Coordinates],listtruc : List[(Int,Concept)]): List[Instance] ={
    listtruc.map(tuple => putInstanciesOfOneConcept(tuple._1,tuple._2,listTile,listTile.size)).flatten
  }

  def putInstanciesOfOneConcept(nbTile: Int, concept: Concept, listTile:List[Coordinates], size:Int): List[Instance] = {
    val r = scala.util.Random
    Seq.fill(nbTile)(Instance.createRandomInstanceOf(concept).at(listTile(r.nextInt(size)))).toList
  }

}