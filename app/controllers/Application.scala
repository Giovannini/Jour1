package controllers

import models.graph.custom_types.Label
import models.map.WorldMap
import models.utils.{ActionParser, ActionManager}
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}


object Application extends Controller {

  val map = WorldMap(Label("MapOfTheWorld"), "description", 150, 150)
  val actionManager = ActionManager(List(), map)
  val actionParser = ActionParser(actionManager)

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