package controllers.graph.property

import forms.graph.property.PropertyForm
import models.graph.concept.{Concept, ConceptDAO}
import models.graph.property.PropertyDAO
import play.api.libs.json.Json
import play.api.mvc._

object PropertyController extends Controller {
  /**
   * Displays the SinglePageApp for CRUDing Property
   * @return
   */
  def index = Action {
    Ok(views.html.property.index())
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
        Ok(views.html.property.index())
    }
  }

  /**
   * Get all properties
   * @author Julien PRADET
   * @return array of properties in JSON or view with options of CRUD
   */
  def getProperties = Action { request =>
    jsonOrRedirectToIndex(request) {
      val properties = PropertyDAO.getAll
      Ok(Json.toJson(properties.map(_.toJson)))
    }
  }

  /**
   * Get a property with its name
   * @author Aurélie LORGEOUX
   * @param id id of the property
   * @return property in JSON
   */
  def getProperty(id: Long) = Action { request =>
    val property = PropertyDAO.getById(id)
    Ok(property.toJson)
  }

  /**
   * Create a given property from a received form
   * @author Aurélie LORGEOUX
   * @return the new property in JSON
   */
  def createProperty = Action(parse.json) {
    request => {
      val newPropertyForm = PropertyForm.form.bind(request.body)
      newPropertyForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          property => {
            val result = PropertyDAO.save(property)
            if (result >= 1) {
              Ok(property.toJson)
            }
            else {
              InternalServerError("Impossible to create property")
            }
          }
        }
      )
    }
  }

  /**
   * Update a given property from a received form
   * @author Aurélie LORGEOUX
   * @return the updated property in JSON
   */
  def updateProperty = Action(parse.json) {
    request => {
      val newPropertyForm = PropertyForm.form.bind(request.body)
      newPropertyForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          property => {
            val result = PropertyDAO.update(property.id, property)
            if (result >= 1) {
              Ok(property.toJson)
            }
            else {
              InternalServerError("Impossible to update property")
            }
          }
        }
      )
    }
  }

  /**
   * Delete a given property from its id
   * @param id id of the property
   * @return deleted if property was deleted,
   *         error else
   */
  def deleteProperty(id: Long) = Action {
    // Get all concepts using the property
    val concepts = ConceptDAO.getAll
      .filter(
        concept => {
          concept.getOwnProperties
            .exists(property => property.property.id == id) ||
          concept.getOwnRules
            .exists(rule => rule.property.id == id)
        }
      )

    // If one concept at least exists the property can't be deleted
    if (concepts != List()) {
      val listOfConcepts = concepts.map(_.label) mkString (" - ")
      InternalServerError("This property is used in following concepts : " + listOfConcepts)
    }
    else {
      val result = PropertyDAO.delete(id)
      if (result >= 1) {
        Ok("deleted")
      }
      else {
        InternalServerError("Impossible to delete property")
      }
    }
  }
}