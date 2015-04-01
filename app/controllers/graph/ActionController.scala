package controllers.graph

import forms.instance_action.action.InstanceActionForm
import models.interaction.action.{InstanceAction, InstanceActionDAO}
import play.api.libs.json.Json
import play.api.mvc._

/**
 * CRUD of InstanceAction
 */
object ActionController extends Controller {
  /**
   * Displays the SinglePageApp for CRUDing InstanceAction
   * @return
   */
  def index = Action {
    Ok(views.html.action.index())
  }

  /**
   * Make sure that the request is in json
   * Otherwise, it redirects to the graph index
   * @param request request sent by the user
   * @param action action to display if the request is correct
   * @return redirects to the index or displays the information matching the request
   */
  def jsonOrRedirectToIndex(request: Request[AnyContent])(action: Result) = {
    request.headers.get("Accept") match {
      case Some(accept) if accept.contains("application/json") =>
        action
      case _ =>
        Redirect("/action")
    }
  }

  /**
   * Create a new Instance action
   * @param label of the action
   * @return the json of the created action
   */
  def createAction(label: String) = Action(parse.json) {
    request => {
      val newActionForm = InstanceActionForm.form.bind(request.body)
      newActionForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          newAction => {
            if(InstanceAction.error != InstanceActionDAO.getByName(newAction.label)) {
              BadRequest(Json.obj("global" -> "Label already exists"))
            } else {
              val savedAction = InstanceActionDAO.save(newAction)
              if(savedAction == InstanceAction.error) {
                InternalServerError(Json.obj("global" -> "Couldn't add to DB."))
              } else {
                Ok(savedAction.toJson)
              }
            }
          }
        }
      )
    }
  }

  /**
   * Get an InstanceAction and returns it in JSON
   * @param label of the action
   * @return
   */
  def readAction(label: String) = Action {
    request => {
      jsonOrRedirectToIndex(request) {
        val action = InstanceActionDAO.getByName(label)
        if(action == InstanceAction.error) {
          NotFound("Label not found")
        } else {
          Ok(action.toJson)
        }
      }
    }
  }

  /**
   * Updates an existing instance
   * @param label of the action
   * @return
   */
  def updateAction(label: String) = Action(parse.json) {
    request => {
      val action = InstanceActionDAO.getByName(label)
      if(action == InstanceAction.error) {
        NotFound("Label not found")
      } else {
        val newActionForm = InstanceActionForm.form.bind(request.body)
        newActionForm.fold(
          hasErrors = {
            form => {
              BadRequest(form.errorsAsJson)
            }
          },
          success = {
            newAction => {
              val result = InstanceActionDAO.update(action.id, newAction)
              if(result > 1) {
                Ok(Json.obj("result" -> "OK"))
              } else {
                InternalServerError(Json.obj("result" -> "Impossible to delete action"))
              }
            }
          }
        )
      }
    }
  }

  /**
   * Deletes an existing instance
   * @param label of the action
   * @return
   */
  def deleteAction(label: String) = Action {
    val action = InstanceActionDAO.getByName(label)
    if(action == InstanceAction.error) {
      NotFound("Label not found")
    } else {
      val result = InstanceActionDAO.delete(action.id)
      if(result > 1) {
        Ok("deleted")
      } else {
        InternalServerError("Impossible to delete action")
      }
    }
  }
}
