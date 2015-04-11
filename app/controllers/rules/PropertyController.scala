package controllers.rules

import forms.graph.ontology.property.PropertyForm
import models.graph.ontology.property.PropertyDAO
import play.api.libs.json.Json
import play.api.mvc._

object PropertyController extends Controller {
  /**
   * Get all properties
   * @author Julien PRADET
   * @return array of propeties in JSON
   */
  def getProperties() = Action { request =>
    val properties = PropertyDAO.getAll
    Ok(Json.toJson(properties.map(_.toJson)))
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
            if (result) {
              Ok(property.toJson)
            }
            else {
              InternalServerError(Json.obj("result" -> "Impossible to create property"))
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
              InternalServerError(Json.obj("result" -> "Impossible to update property"))
            }
          }
        }
      )
    }
  }

  /**
   * Delete a given property from its id
   * @param id id of the property
   * @return
   */
  def deleteProperty(id: Long) = Action {
    val result = PropertyDAO.delete(id)
    if (result >= 1) {
      Ok("deleted")
    }
    else {
      InternalServerError(Json.obj("result" -> "Impossible to delete property"))
    }
  }

}