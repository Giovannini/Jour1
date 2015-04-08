package controllers.ontology

import controllers.Application
import forms.graph.ontology.InstanceForm
import models.graph.ontology.{Instance, ValuedProperty}
import play.api.data.Form
import play.api.mvc.{AnyContent, Action, Controller}

/**
 * Object containing tools for instance editing
 */
object InstanceManager extends Controller {
  /**
   * Print errors contained in a form
   * @author Thomas GIOVANNINI
   */
  def printErrors(form: Form[Instance]) = {
    form.errors.foreach(error => println("###Error:\n" + error.messages.mkString("\n")))
  }

  /**
   * Create a given instance from a received form
   * @author Simon RONCIERE, Aurélie LORGEOUX
   * @return an action redirecting to the index page of the application
   */
  def create = Action(parse.json) {
    request => {
      val newInstanceForm = InstanceForm.form.bind(request.body)
      newInstanceForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          instance => {
            val oldInstance = Application.map.getInstanceById(instance.id)
            val newInstance = getNewInstance(newInstanceForm, oldInstance)
            Application.map.createInstance(newInstance)
            Ok(newInstance.toJson)
          }
        }
      )
    }
  }

  /**
   * Update a given instance from a received form
   * @author Thomas GIOVANNINI, Aurélie LORGEOUX
   * @return an action redirecting to the index page of the application
   */
  def update = Action(parse.json) {
    request => {
      println(request.body)
      val newInstanceForm = InstanceForm.form.bind(request.body)
      newInstanceForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          instance => {
            val oldInstance = Application.map.getInstanceById(instance.id)
            val newInstance = getNewInstance(newInstanceForm, oldInstance)
            Application.map.updateInstance(oldInstance, newInstance)
            Ok(newInstance.toJson)
          }
        }
      )
    }
  }

  /**
   * Delete an instance
   * @param instanceId instance id
   * @return an action redirecting to the index page of the application
   */
  def delete(instanceId: Int): Action[AnyContent] = Action {
    Application.map.removeInstance(instanceId)
    Redirect(controllers.routes.MapController.show())
  }

  /**
   * Read a form containing pieces of information to create a new instance from an old one
   * @author Thomas GIOVANNINI, Aurélie LORGEOUX
   * @param newInstanceForm containing pieces of information on new instance
   * @param oldInstance from which he new one will be created
   * @return a new instance containing attributes from the form
   */
  def getNewInstance(newInstanceForm: Form[Instance], oldInstance: Instance): Instance = {
    println("oldInstance " + oldInstance)
    println("newInstanceForm " + newInstanceForm)
    println("get " + newInstanceForm.get)
    oldInstance
      .withLabel(newInstanceForm.get.label)
      .at(newInstanceForm.get.coordinates)
      .ofConcept(newInstanceForm.get.concept)
      .withProperties(newInstanceForm.get.properties)
  }
}
