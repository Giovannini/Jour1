package controllers

import models.map.WorldMap
import org.anormcypher.Neo4jREST
import play.Play
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json
import play.api.mvc._


object Application extends Controller {

  val map = WorldMap("MapOfTheWorld", "description", 50, 50)
  lazy val connection = DB.getConnection()
  implicit val neoConnection = Neo4jREST(Play.application.configuration.getString("serverIP"), 7474, "/db/data/")

  /**
   * Show the different urls of the project
   * @author Julien Pradet
   */
  def index: Action[AnyContent] = Action {
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