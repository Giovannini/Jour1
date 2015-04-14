package controllers.interaction

import forms.interaction.InteractionForm
import models.graph.relation.RelationSqlDAO
import models.interaction.InteractionType
import models.interaction.action.{InstanceAction, InstanceActionDAO}
import play.api.libs.json.Json
import play.api.mvc._

/**
 * CRUD of InstanceAction
 * @author Julien PRADET
 */
object ActionController extends Controller {
  /**
   * Displays the SinglePageApp for CRUDing InstanceAction
   * @author Julien PRADET
   * @return the main frame for the CRUD - see public/templates/action for more templates
   */
  def index = Action {
    Ok(views.html.interaction.action.index())
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
        Ok(views.html.interaction.action.index())
    }
  }

  /**
   * Gets the list of actions (every entry in the rules table) in JSON
   * @author Julien PRADET
   * @return Status that returns JSON
   */
  def getActions: Action[AnyContent] = Action { request =>
    val actions = InstanceActionDAO.getAll
    Ok(Json.toJson(actions.map(_.toJson)))
  }

  /**
   * Gets the list of effects in JSON (rules that starts with "EFFECT_")
   * @author Julien PRADET
   * @return Status that returns JSON
   */
  def getEffects: Action[AnyContent] = Action { request =>
    val effects = InstanceActionDAO.getAllEffects
    Ok(Json.toJson(effects.map(_.toJson)))
  }

  /**
   * Create a new Instance action
   * @author Julien PRADET
   * @param label of the action
   * @return the json of the created action
   */
  def createAction(label: String) = Action(parse.json) {
    request => {
      val newActionForm = InteractionForm.form.bind(request.body)
      newActionForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          interaction => {
            val newAction = interaction._2
            if(InstanceAction.error != InstanceActionDAO.getByName(newAction.label)) {
              BadRequest(Json.obj("global" -> "Label already exists"))
            } else {
              val savedAction = InstanceActionDAO.save(newAction)
              if(savedAction == InstanceAction.error) {
                InternalServerError(Json.obj("global" -> "Couldn't add to DB."))
              } else {
                if(interaction._1 != InteractionType.Simple) {
                  RelationSqlDAO.save(savedAction.label)
                }
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
   * @author Julien PRADET
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
   * Updates an existing action
   * @author Julien PRADET
   * @param label of the action
   * @return
   */
  def updateAction(label: String) = Action(parse.json) {
    request => {
      val action = InstanceActionDAO.getByName(label)
      if(action == InstanceAction.error) {
        NotFound("Label not found")
      } else {
        val newActionForm = InteractionForm.form.bind(request.body)
        newActionForm.fold(
          hasErrors = {
            form => {
              BadRequest(form.errorsAsJson)
            }
          },
          success = {
            interaction => {
              val newAction = interaction._2
              val result = InstanceActionDAO.update(action.id, newAction)
              if(result >= 1) {
                if(interaction._1 != InteractionType.Simple) {
                  val oldRelation = RelationSqlDAO.getByName(label)
                  RelationSqlDAO.update(oldRelation.id, newAction.label)
                }

                Ok(Json.obj("result" -> "OK"))
              } else {
                InternalServerError(Json.obj("result" -> "Impossible to update action"))
              }
            }
          }
        )
      }
    }
  }

  /**
   * Deletes an existing action
   * @author Julien PRADET
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
