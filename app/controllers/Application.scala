package controllers

import models.WorldMap
import models.graph.custom_types.Label
import play.api.libs.json.Json
import play.api.mvc._


object Application extends Controller {

  val map = WorldMap(Label("MapOfTheWorld"), "description", 100, 100)

  /**
   * Show the different urls of the project
   * @author Julien Pradet
   */
  def index = Action {
    Ok(views.html.index())
  }

  /**
   * Get all the instances of a concept
   * @author Julien Pradet
   */
  def getAllInstancesOf(conceptId: Int) = Action {
    Ok(Json.toJson(map.getInstancesOf(conceptId).map(_.toJson)))
  }
}