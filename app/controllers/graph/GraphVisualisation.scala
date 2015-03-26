package controllers.graph

import models.graph.ontology.property.{Property, PropertyDAO}
import models.instance_action.action.InstanceAction
import models.instance_action.precondition.PreconditionDAO
import play.api.libs.json.Json
import play.api.mvc._

/**
 * Controller that displays the concept graph to the user
 */
object GraphVisualisation extends Controller {
  /**
   * The graph displayed to the user allows him to entirely manage the graph with an user friendly interface
   * @return
   */
  def index: Action[AnyContent] = Action {
    Ok(views.html.graph.index())
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
        Redirect("/graph")
    }
  }

  /**
   * Gets a property
   * @param propertyId property ID
   * @return Property in json if it's found. 404 otherwise
   */
  def getProperty(propertyId: Int): Action[AnyContent] = Action { request =>
    jsonOrRedirectToIndex(request) {
      val property = PropertyDAO.getById(propertyId)
      if (property == Property.error) {
        NotFound("Undefined property")
      } else {
        Ok(property.toJson)
      }
    }
  }

  /**
   * Gets an action
   * @param actionLabel action label
   * @return Action in json if it's found. 404 otherwise
   */
  def getAction(actionLabel: String): Action[AnyContent] = Action { request =>
    jsonOrRedirectToIndex(request) {
      val action = InstanceAction.getByName(actionLabel)
      if(action == InstanceAction.error) {
        NotFound("Undefined action")
      } else {
        Ok(action.toJson)
      }
    }
  }

  /**
   * Gets all the properties
   * @return
   */
  def getProperties: Action[AnyContent] = Action { request =>
    jsonOrRedirectToIndex(request) {
      val properties = PropertyDAO.getAll
      Ok(Json.toJson(properties.map(_.toJson)))
    }
  }

  def getPreconditions: Action[AnyContent] = Action { request =>
    jsonOrRedirectToIndex(request) {
      val preconditions = PreconditionDAO.getAll
      Ok(Json.toJson(preconditions.map(_.toJson)))
    }
  }

  def getActions: Action[AnyContent] = Action { request =>
    jsonOrRedirectToIndex(request) {
      val preconditions = InstanceAction.getAll
      Ok(Json.toJson(preconditions.map(_.toJson)))
    }
  }
}
