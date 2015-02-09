package models.map

import models.graph.custom_types.{Coordinates, Label}
import models.graph.ontology.{ValuedProperty, Instance, Property, Concept}

/**
 * Created by eisti on 2/5/15.
 */
case class WorldInit (layer: Layer) {

  def sumForce(listGrounds:List[Int]): Int = {
    Concept.getById(listGrounds.head) match{
      //properties => rules
      case Some(c)=> val l=c.rules.filter(p=>p.property.label=="Force")
        // TODO value : Int
        l.head.value + sumForce(listGrounds.tail)
      case _ => 0
    }
  }
  
  def repartition(min : Int, max :Int, minReel : Int, listGrounds:List[Int] ): List[(Int,Concept)] ={
    var sumStr=sumForce(listGrounds)
    Concept.getById(listGrounds.head) match {
      case Some(c) => val l=c.rules.filter(p=>p.property.label=="Force")
        val maxOccupe = (min+ (l.head.value * (max-minReel) / sumStr)).toInt
        (maxOccupe,c)::repartition(maxOccupe,max,minReel,listGrounds.tail)
      case _ => repartition(min,max,minReel,listGrounds.tail)
    }
    //minOccupe est la valeur telle que [min;maxOccupe]=typeGround head
  }


  def WorldMapGeneration(): Unit ={
    //val map = WorldMap
    val frequency = 20
    val octave = 35
    val persistence= 0.5f
    val lissage = 3
    val outputSize = 150

    val layer = Layer.generateLayer(frequency,octave,persistence,lissage,outputSize)
    val listInstanciable = getInstanciable(Concept.findAll)

    val idGround=getGround(Concept.findAll)
    val listGrounds=getGrounds(idGround::Nil)
    val (min,moy,max)=layer.statMatrix()
    val listRepartition=repartition(min,max,min,listGrounds)

    for(i<-0 until outputSize;j<-0 until outputSize){
      //TODO remplire la liste d'instance en fonction de la liste des concepts "listRepartition" et des valeurs de la matrice
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
    val prop=List(ValuedProperty(Property("Nom"), concept.label),ValuedProperty(Property("Id"), (math.random * 10).toInt.toString))
    new Instance(concept.label,coordinates,prop,concept)
  }

  def getInstanciable(listConcepts:List[Concept]): List ={
    listConcepts.filter(_.properties.contains(Property("Instanciable")))
  }

}




