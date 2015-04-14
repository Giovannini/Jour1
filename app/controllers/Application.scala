package controllers

import models.map.WorldMap
import org.anormcypher.Neo4jREST
import play.Play
import play.api.Play.current
import play.api.db.DB
import play.api.mvc._


object Application extends Controller {

  val map = WorldMap("MapOfTheWorld", "description", 15, 15)
  lazy val connection = DB.getConnection()
  implicit val neoConnection = Neo4jREST(Play.application.configuration.getString("serverIP"), 7474, "/db/data/")

  /**
   * Show the different urls of the project
   * @author Julien Pradet
   */
  def index: Action[AnyContent] = Action {
    Ok(views.html.index())
  }
}