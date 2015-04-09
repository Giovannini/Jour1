package controllers.rules

import models.graph.property.PropertyDAO
import play.api.libs.json.Json
import play.api.mvc._

object PropertyController extends Controller {
  def getProperties = Action { request =>
    val properties = PropertyDAO.getAll
    Ok(Json.toJson(properties.map(_.toJson)))
  }

  def createProperty(label: String) = play.mvc.Results.TODO

  def getProperty(label: String) = play.mvc.Results.TODO

  def updateProperty(label: String) = play.mvc.Results.TODO

  def deleteProperty(label: String) = play.mvc.Results.TODO
}