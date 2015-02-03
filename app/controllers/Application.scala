package controllers

import models.graph.custom_types.{Coordinates, Label}
import models.graph.ontology.{ValuedProperty, Instance, Property, Concept}
import models.map.WorldMap
import play.api.mvc._

object Application extends Controller {



  def index = Action {
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



}