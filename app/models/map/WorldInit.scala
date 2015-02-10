package models.map

import models.graph.custom_types.Coordinates
import models.graph.ontology.{ValuedProperty, Instance, Property, Concept}

/**
 * Class with method to initialize the world
 */
case class WorldInit (layer: Layer) {

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
   * @param lowestStrength the lowest ground strength
   * @param biggestStrength the biggest ground strength
   * @param listGrounds the ground list
   * @return a list of tuple containing whatever is "maxOccupe" and its associated concept
   */
  def repartition(lastBorn: Int, lowestStrength : Int, biggestStrength :Int, listGrounds:List[Int] ): List[(Int,Concept)] ={
    listGrounds match {
      case head::tail => Concept.getById(head) match {
          case Some(concept) =>
            val maxOccupe = getMaxOccupe(lastBorn, listGrounds, concept)
            (maxOccupe, concept) :: repartition(maxOccupe, lowestStrength, biggestStrength, tail)
          case _ => repartition(lastBorn, lowestStrength, biggestStrength, tail)
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

  /**
   * Get what Simon calls the maxOccupe
   * @author Thomas GIOVANNINI
   * @param lastBorn
   * @param listGrounds the ground list
   * @param concept
   * @return whatever maxOccupe is, it returns it
   */
  def getMaxOccupe(lastBorn: Int, listGrounds:List[Int], concept: Concept): Int = {
    lastBorn + (getStrengthOf(concept) * (listGrounds.max - listGrounds.min) / listGrounds.sum)
  }

  def notARepartitionError(tuple: (Int, Concept)): Boolean = {
    tuple._2 != Concept.error
  }

  def WorldMapGeneration(): Unit ={
    //val map = WorldMap
    val frequency = 20
    val octave = 35
    val persistence= 0.5f
    val lissage = 3
    val outputSize = 150

    val layer = Layer.generateLayer(frequency,octave,persistence,lissage,outputSize)
    val instanciableConceptsList = getInstanciable(Concept.findAll)

    val idGround=getGround(Concept.findAll)
    val listGrounds=getGrounds(idGround::Nil)
    val (min,moy,max)=layer.statMatrix
    val repartitionList = repartition(min, min,max,listGrounds)

    for(i<-0 until outputSize;j<-0 until outputSize){
      //TODO remplire la liste d'instance en fonction de la liste des concepts "repartitionList" et des valeurs de la matrice
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
    val listChild=Concept.getChildren(listID.head)
    if (listChild.isEmpty)listID.head::getGrounds(listID.tail)
    else getGrounds(listID.tail:::listChild.map(_.id))
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




