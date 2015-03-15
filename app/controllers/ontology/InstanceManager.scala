package controllers.ontology

import controllers.Application
import models.graph.custom_types.Coordinates
import models.graph.ontology.{Instance, ValuedProperty}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.{Action, Controller}

/**
 * Object containing tools for instance editing
 */
object InstanceManager extends Controller {

  val instanceForm = Form(
    tuple(
      "id" -> number, //can't be modified
      "label" -> nonEmptyText,
      "coordinateX" -> number.verifying(min(0), max(Application.map.width)),
      "coordinateY" -> number.verifying(min(0), max(Application.map.height)),
      "property" -> list(nonEmptyText)
    )
  )

  /**
   * Print errors contained in a form
   * @author Thomas GIOVANNINI
   */
  def printErrors(form: Form[(Int, String, Int, Int, List[String])]) = {
    form.errors.foreach(error => println("###Error:\n" + error.messages.mkString("\n")))
  }

  /**
   * Create a given instance from a received form.
   * @author Simon RONCIERE
   * @return an action redirecting to the index page of the application
   */
  def create = Action { implicit request =>

    /**
     * Create the relation following a form with no errors in it.
     * @author Thomas GIOVANNINI
     */
    def doCreate(form: (Int, String, Int, Int, List[String])) = {
      val oldInstance = Application.map.getInstanceById(form._1)
      val newInstance = getNewInstance(form, oldInstance)
      Application.map.updateInstance(oldInstance, newInstance)
    }

    val newTodoForm = instanceForm.bindFromRequest()
    newTodoForm.fold(
      hasErrors = { form => printErrors(form)},
      success = { newInstanceForm => doCreate(newInstanceForm)})
    Redirect(controllers.routes.MapController.show())
  }

  /**
   * Update a given instance from a received form.
   * @author Thomas GIOVANNINI
   * @return an action redirecting to the index page of the application
   */
  def update = Action { implicit request =>
    /**
     * Update the map following a form with no errors in it.
     * @author Thomas GIOVANNINI
     * @return the updated instance
     */
    def doUpdate(form: (Int, String, Int, Int, List[String])) = {
      val oldInstance = Application.map.getInstanceById(form._1)
      val newInstance = getNewInstance(form, oldInstance)
      Application.map.updateInstance(oldInstance, newInstance)
    }

    val newTodoForm = instanceForm.bindFromRequest()
    newTodoForm.fold(
      hasErrors = { form => printErrors(form)},
      success = { newInstanceForm => doUpdate(newInstanceForm)})
    Redirect(controllers.routes.MapController.show())
  }

  def delete(instanceId: Int) = Action {
    val instanceToRemove = Application.map.getInstanceById(instanceId)
    Application.map.removeInstance(instanceToRemove)
    Redirect(controllers.routes.MapController.show())
  }

  /**
   * Get a modified list of an instance properties given a list of new values
   * @param valuesToString new values to update the list
   * @param oldInstance instance from which the properties will be updated
   * @return an updated list of properties
   */
  def getUpdatedProperties(valuesToString: List[String], oldInstance: Instance): List[ValuedProperty] = {
    oldInstance.properties
      .zip(valuesToString)
      .map(tuple => ValuedProperty.parseValue(tuple._1.property, tuple._2))
  }

  /**
   * Read a form containing pieces of information to create a new instance from an old one
   * @author Thomas GIOVANNINI
   * @param newInstanceForm containing pieces of information on new instance
   * @param oldInstance from which he new one will be created
   * @return a new instance containing attributes from the form
   */
  def getNewInstance(newInstanceForm: (Int, String, Int, Int, List[String]), oldInstance: Instance): Instance = {
    //TODO secure properties here
    val newProperties = getUpdatedProperties(newInstanceForm._5, oldInstance)
    oldInstance
      .withLabel(newInstanceForm._2)
      .at(Coordinates(newInstanceForm._3, newInstanceForm._4))
      .updateProperties(newProperties)
  }
}
