package controllers.interaction.precondition

import forms.interaction.precondition.PreconditionForm
import models.interaction.precondition.{Precondition, PreconditionDAO}
import play.api.libs.json.Json
import play.api.mvc._

object PreconditionController extends Controller {
  /**
   * Displays the SinglePageApp for CRUDing Precondition
   * @return
   */
  def index = Action {
    Ok(views.html.precondition.index())
  }

  /**
   * Make sure that the request is in json
   * Otherwise, it redirects to the graph index
   * @author Julien PRADET
   * @param request request sent by the user
   * @param action action to display if the request is correct
   * @return redirects to the index or displays the information matching the request
   */
  def jsonOrRedirectToIndex(request: Request[AnyContent])(action: Result) = {
    request.headers.get("Accept") match {
      case Some(accept) if accept.contains("application/json") =>
        action
      case _ =>
        Ok(views.html.precondition.index())
    }
  }

 /**
  * Get all preconditions
  * @author Julien PRADET
  * @return array of properties in JSON or view with options of CRUD
  */
  def getPreconditions = Action { request =>
    jsonOrRedirectToIndex(request) {
      val preconditions = PreconditionDAO.getAll
      Ok(Json.toJson(preconditions.map(_.toJson)))
    }
  }

  /**
   * Get a precondition
   * @author Aurélie LORGEOUX
   * @param id id of the precondition
   * @return precondition in JSON
   */
  def getPrecondition(id: Long) = Action { request =>
    val precondition = PreconditionDAO.getById(id)
    Ok(precondition.toJson)
  }


  /**
   * Create a given precondition from a received from
   * @author Aurélie LORGEOUX
   * @return the new precondition in JSON
   */
  def createPrecondition = Action(parse.json) {
    request => {
      val newPreconditionForm = PreconditionForm.form.bind(request.body)
      newPreconditionForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          precondition => {
            val result = PreconditionDAO.save(precondition)
            if (result != Precondition.error) {
              Ok(precondition.toJson)
            }
            else {
              InternalServerError("Impossible to create precondition")
            }
          }
        }
      )
    }
  }

  /**
   * Update a given precondition from a received form
   * @author Aurélie LORGEOUX
   * @return the new precondition in JSON
   */
  def updatePrecondition = Action(parse.json) {
    request => {
      val newPreconditionForm = PreconditionForm.form.bind(request.body)
      newPreconditionForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          precondition => {
            val result = PreconditionDAO.update(precondition.id, precondition)
            if (result >= 1) {
              Ok(precondition.toJson)
            }
            else {
              InternalServerError("Impossible to update precondition")
            }
          }
        }
      )
    }
  }

  /**
   * Delete a given precondition from its id
   * @author Aurélie LORGEOUX
   * @param id id of the precondition
   * @return deleted if precondition was deleted,
   *         error else
   */
  def deletePrecondition(id: Long) = Action {
    val result = PreconditionDAO.delete(id)
    if (result >= 1) {
      Ok("deleted")
    }
    else {
      InternalServerError("Impossible to delete precondition")
    }
  }
}