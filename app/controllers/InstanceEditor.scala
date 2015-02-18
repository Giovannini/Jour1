package controllers

import models.graph.custom_types.Coordinates
import models.graph.ontology.{ValuedProperty, Instance}
import play.api.mvc.{Action, Controller}

import play.api.data.Form
import play.api.data.Forms._

/**
 * Object containing tools for instance editing
 */
object InstanceEditor extends Controller{

  val instanceForm = Form(
    tuple(
      "id" -> number,
      "label" -> nonEmptyText,
      "coordinateX" -> number,
      "coordinateY" -> number,
      "property" -> list(nonEmptyText)
    )
  )
  def update = Action { implicit request =>
    val newTodoForm = instanceForm.bindFromRequest()
    newTodoForm.fold(
      hasErrors = { form =>
        form.errors.foreach(error => println("###Error:\n" + error.messages.mkString("\n")))
      },
      success = { newInstanceForm =>
        val verification = newInstanceForm._3 < Application.map.width && newInstanceForm._4 < Application.map.height
        if(verification) {
          val oldInstance = Application.map.getInstanceById(newInstanceForm._1)
          val newInstance = getNewInstance(newInstanceForm, oldInstance)
          Application.map.updateInstance(oldInstance, newInstance)
        }
      }
    )
    Redirect(routes.Application.index())
  }

  def updateProperties(valuesToString: List[String], oldInstance: Instance): List[ValuedProperty] = {
    oldInstance.properties
      .zip(valuesToString)
      .map(tuple => ValuedProperty.parseValue(tuple._1.property, tuple._2))
  }

  def getNewInstance(newInstanceForm: (Int, String, Int, Int, List[String]), oldInstance: Instance): Instance = {
    val newProperties = updateProperties(newInstanceForm._5, oldInstance)
    oldInstance
      .withLabel(newInstanceForm._2)
      .at(Coordinates(newInstanceForm._3, newInstanceForm._4))
      .updateProperties(newProperties)
  }
}
