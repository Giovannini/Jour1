package controllers.interaction.precondition

import models.interaction.precondition.PreconditionDAO
import play.api.libs.json.Json
import play.api.mvc._

object PreconditionController extends Controller {
  def getPreconditions = Action {
    val preconditions = PreconditionDAO.getAll
    Ok(Json.toJson(preconditions.map(_.toJson)))
  }

  def createPrecondition(label: String) = play.mvc.Results.TODO

  def getPrecondition(label: String) = play.mvc.Results.TODO

  def updatePrecondition(label: String) = play.mvc.Results.TODO

  def deletePrecondition(label: String) = play.mvc.Results.TODO
}