package controllers.graph

import forms.instance_action.action.InstanceActionForm
import models.instance_action.action.InstanceAction
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
   * @param label
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
            if(InstanceAction.error != InstanceAction.getByName(newAction.label)) {
              BadRequest(Json.obj("global" -> "Label already exists"))
            } else {
              val savedAction = InstanceAction.save(newAction)
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
   * @param label
   * @return
   */
  def readAction(label: String) = Action {
    request => {
      jsonOrRedirectToIndex(request) {
        val action = InstanceAction.getByName(label)
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
   * @param label
   * @return
   */
  def updateAction(label: String) = Action(parse.json) {
    request => {
      val action = InstanceAction.getByName(label)
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
              val result = InstanceAction.update(action.id, newAction)
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
   * @param label
   * @return
   */
  def deleteAction(label: String) = Action {
    val action = InstanceAction.getByName(label)
    if(action == InstanceAction.error) {
      NotFound("Label not found")
    } else {
      val result = InstanceAction.delete(action.id)
      if(result > 1) {
        Ok("deleted")
      } else {
        InternalServerError("Impossible to delete action")
      }
    }
  }
}
