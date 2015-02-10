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

    /*Concept.getById(listGrounds.head) match {
      case Some(c) => val l=c.rules.filter(p=>p.property.label=="Force")
        val maxOccupe = (min+ (l.head.value * (max-minReel) / sumStr)).toInt
        (maxOccupe,c)::repartition(maxOccupe,max,minReel,listGrounds.tail)
      case _ => repartition(min,max,minReel,listGrounds.tail)
    }*/
    //minOccupe est la valeur telle que [min;maxOccupe]=typeGround head
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

  def notARepartitionError(tuple: (Int, Concept)): Boolean = {
    tuple._2 != Concept.error
  }

  def worldMapGeneration(width:Int,heigth:Int): List[Instance] ={
    //val map = WorldMap
    val frequency = 20
    val octave = 35
    val persistence= 0.5f
    val smoothed = 3
    val outputSize = width

    val layer = Layer.generateLayer(frequency,octave,persistence,smoothed,outputSize)
    val instanciableConceptsList = getInstanciable(Concept.findAll)

    val idGround=getGround(Concept.findAll)
    val listGrounds=getGrounds(idGround::Nil)

    val sumStr= sumStrength(listGrounds)


    val (min,moy,max)=layer.statMatrix

    val repartitionList = repartition(min, min,max,listGrounds,sumStr).sortBy(_._1)

    matrixToList(layer.matrix)
      .map{createInstance(_,repartitionList)
    }

  }

  def sumStrength(list : List[Int]):Int={
    list match{
      case Nil => 0
      case head::tail => Concept.getById(head) match {
        case Some(c) => getStrengthOf(c)+sumStrength(tail)
        case _ => sumStrength(tail)

      }
    }
  }

  def matrixToList(matrix: Array[Array[Int]]):List[(Int,Int,Int)]={
    matrix.map(_.toList.zipWithIndex).toList.zipWithIndex
      .map{
      indexedLine => indexedLine._1.map(element => (element._1, element._2, indexedLine._2))
    }.flatten
  }

  def createInstance(triplet:(Int,Int,Int),repartitionList: List[(Int,Concept)]):Instance={
    Instance.createRandomInstanceOf(instanciationTile(triplet._1,repartitionList)).at(Coordinates(triplet._2,triplet._3))
  }

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


  def getGrounds(listID:List[Int]):List[Int]={
    listID match {
      case Nil => Nil
      case head::tail => val listChild = Concept.getChildren(head)
        if (listChild.isEmpty)head::getGrounds(tail)
        else getGrounds(tail:::listChild.map(_.id))
    }


  }


  def compTile(listConcepts: List[Concept]):Unit={

  }
  def instConcept(coordinates:Coordinates,concept:Concept):Instance={
    val prop=List(
      ValuedProperty(Property("Nom", "String", "GIOVA"), concept.label),
      ValuedProperty(Property("Id", "Int", 0), (math.random * 10).toInt.toString))
    Instance(0, concept.label,coordinates,prop,concept)
  }
  def getInstanciable(listConcepts:List[Concept]): List[Concept] ={
    listConcepts.filter(_.properties.map(_.label).contains("Instanciable"))
  }
}




