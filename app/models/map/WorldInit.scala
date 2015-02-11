package models.map

import models.graph.custom_types.Coordinates
import models.graph.ontology.{ValuedProperty, Instance, Property, Concept}

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
  def getStrengthSum(groundIdsList:List[Int]): Int = {
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
   * Get what Simon calls the repartition list.
   * @author Thomas GIOVANNINI
   * @param lowestMatrixValue the lowest ground strength
   * @param biggestMatrixValue the biggest ground strength
   * @param listGrounds the ground list
   * @return a list of tuple containing whatever is "maxOccupe" and its associated concept
   */

  def repartition(lastBounds: Int, lowestMatrixValue : Int, biggestMatrixValue :Int, listGrounds:List[Int] , sumStr:Int): List[(Int,Concept)] ={
    listGrounds match {
      case head::Nil => Concept.getById(head) match {
        case Some(concept) =>
          (biggestMatrixValue, concept)::Nil
        case _ => Nil
      }

      case head::tail => Concept.getById(head) match {
        case Some(concept) =>
          val maxOccupe = getMaxOccupe(lastBounds,lowestMatrixValue, biggestMatrixValue, sumStr, concept)
          (maxOccupe, concept) :: repartition(maxOccupe, lowestMatrixValue, biggestMatrixValue, tail, sumStr)
        case _ => repartition(lastBounds, lowestMatrixValue, biggestMatrixValue, tail, sumStr)
      }
      case _ => List()
    }

  }

  def shortIncreasing (list:List[(Int,Concept)]):List[(Int,Concept)]={
    list.sortBy(_._1)
  }
  /**
   * Get what Simon calls the maxOccupe
   * @author Thomas GIOVANNINI
   * @param lastBounds
   * @param concept
   * @return whatever maxOccupe is, it returns it
   */
  def getMaxOccupe(lastBounds: Int, lowestMatrixValue:Int, biggestMatrixValue:Int, sumStr:Int, concept: Concept): Int = {
    lastBounds + (getStrengthOf(concept) * (biggestMatrixValue - lowestMatrixValue) / sumStr)
  }

  /**
   *
   * @author Simon Roncière
   * @param tuple
   * @return
   */
  def notARepartitionError(tuple: (Int, Concept)): Boolean = {
    tuple._2 != Concept.error
  }

  /**
   *
   * @author Simon Roncière
   * @param width
   * @param heigth
   * @return
   */
  def worldMapGeneration(width:Int,heigth:Int): List[Instance] ={
    val frequency = 20
    val octave = 35
    val persistence= 0.5f
    val smoothed = 3
    val outputSize = width

    val layer = Layer.generateLayer(frequency,octave,persistence,smoothed,outputSize)

    val idGround=getGround(Concept.findAll)
    val listGrounds=getGrounds(idGround::Nil)
    val sumStr= sumStrength(listGrounds)
    println("SumStr grounds = "+sumStr)
    val (min,moy,max)=layer.statMatrix
    val repartitionList = repartition(min, min,max,listGrounds,sumStr).sortBy(_._1)
    //

    val terrain = matrixToList(layer.matrix)
      .map{createInstance(_,repartitionList)
    }

    terrain:::randomObjectInstanciation(listGrounds,outputSize,outputSize)
  }

  /**
   * get sum of strength of concept
   * @author Simon Roncière
   * @param listConcept list of concept's id whose we want the sum
   * @return
   */

  def sumStrength(listConcept : List[Int]):Int={
    listConcept match{
      case Nil => 0
      case head::tail => Concept.getById(head) match {
        case Some(c) => getStrengthOf(c)+sumStrength(tail)
        case _ => sumStrength(tail)

      }
    }
  }

  /**
   * @author Simon Roncière
   * @param matrix
   * @return
   */
  def matrixToList(matrix: Array[Array[Int]]):List[(Int,Int,Int)]={
    matrix.map(_.toList.zipWithIndex).toList.zipWithIndex
      .map{
      indexedLine => indexedLine._1.map(element => (element._1, element._2, indexedLine._2))
    }.flatten
  }

  /**
   * @author Simon Ronciere
   * @param triplet
   * @param repartitionList
   * @return
   */
  def createInstance(triplet:(Int,Int,Int),repartitionList: List[(Int,Concept)]):Instance={
    Instance.createRandomInstanceOf(instanciationTile(triplet._1,repartitionList)).at(Coordinates(triplet._2,triplet._3))
  }

  /**
   * @author Simon Roncière
   * @param valeur
   * @param list
   * @return
   */
  def instanciationTile(valeur:Int,list:List[(Int,Concept)]):Concept={
    list match{
      case Nil => Concept.error
      case _ => if(valeur<list.head._1)
        list.head._2
      else
        instanciationTile(valeur,list.tail)
    }


  }

  /**
   *
   * @param concepts
   * @return -1 if concept "Ground" not found, else id of the concept
   */
  def getGround(concepts:List[Concept]): Int ={
    concepts.find(_.label=="Ground") match {
      case Some(c)=> c.id
      case _ => -1
    }
  }

  /**
   * @author Simon Roncière
   * @param listID
   * @return
   */
  def getGrounds(listID:List[Int]):List[Int]={
    listID match {
      case Nil => Nil
      case head::tail => val listChild = Concept.getChildren(head)
        if (listChild.isEmpty)head::getGrounds(tail)
        else getGrounds(tail:::listChild.map(_.id))
    }


  }

  /**
   * @author Thomas Giovannini
   * @param listConcepts
   */
  def compTile(listConcepts: List[Concept]):Unit={

  }
  def instConcept(coordinates:Coordinates,concept:Concept):Instance={
    val prop=List(
      ValuedProperty(Property("Nom", "String", "GIOVA"), concept.label),
      ValuedProperty(Property("Id", "Int", 0), (math.random * 10).toInt.toString))
    Instance(0, concept.label,coordinates,prop,concept)
  }




  ///////////////////////////::OBJECT:://///////////////////////////////




  /**
   * Get id list of concept instanciable
   * @author Simon Roncière
   * @param listConcepts list concept where we get instanciables
   * @return
   */
  def getInstanciable(listConcepts:List[Concept]): List[Concept] ={
    val propertyInstanciable      = Property("Instanciable", "Boolean", false)
    listConcepts.filter(concept=>concept.rules.contains(ValuedProperty(propertyInstanciable,true)))
  }

  /**
   * Get id list of concept to instanciate
   * @author Simon Roncière
   * @param grounds
   * @param concepts
   * @return
   */
  def getInstance(grounds:List[Int], concepts:List[Concept]):List[Int]={
     val allInst:List[Concept] = getInstanciable(concepts)
      allInst match {
      case Nil => Nil
      case head::Nil => 
        if(!grounds.contains(head.id))head.id::Nil
        else Nil
      case head::tail => 
        if(grounds.contains(head.id))getInstance(grounds,tail) 
        else head.id::getInstance(grounds,tail)
    }
  }

  def getNbTileByInstance(listInstanciable:List[Int],sumStrO:Int,nbTile:Int,vide:Int): List[(Int,Concept)] ={
    if(sumStrO==0 || vide==0){
      println("erreur")
      Nil
    }else {
      listInstanciable match {
        case Nil => Nil
        case head :: tail =>
          Concept.getById(head) match {
            case Some(concept) =>
              val str = getStrengthOf(concept)
              (str * nbTile / (sumStrO * vide), concept) :: getNbTileByInstance(tail, sumStrO, nbTile, vide)
            case _ => getNbTileByInstance(tail, sumStrO, nbTile, vide)
          }
      }
    }
  }

  def randomObjectInstanciation(grounds:List[Int],heigth:Int,width:Int): List[Instance] ={
    val nbTile = heigth*width
    val listInstance= getInstance(grounds,Concept.findAll)
    val sumStr = sumStrength(listInstance)
    println("NbTile = "+nbTile)
    println("SumStr objets = "+sumStr)
    val listNumberEachInstance = getNbTileByInstance(listInstance,sumStr,nbTile,2)
    listNumberEachInstance.map(tuple=>println("concept "+tuple._2.label+" = "+tuple._1))
    listNumberEachInstance.map(tuple=>putInstanciesOfOneConcept(tuple._1,tuple._2,heigth,width)).flatten
  }




  def putInstanciesOfOneConcept(nbTile:Int,concept:Concept,heigth:Int,width:Int): List[Instance] ={
    val r = scala.util.Random
    Seq.fill(nbTile)(Instance.createRandomInstanceOf(concept).at(Coordinates(r.nextInt(heigth),r.nextInt(width)))).toList
  }


}




