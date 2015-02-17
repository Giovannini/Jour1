package controllers

import models.graph.custom_types.Coordinates
import models.graph.ontology.Instance
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
      "coordinateY" -> number
    )
  )
  def update = Action { implicit request =>
    val newTodoForm = instanceForm.bindFromRequest()
    println("Save the form.")
    newTodoForm.fold(
      hasErrors = { form =>
        println("Has errors: ")
        form.errors.foreach(println)
      },
      success = { newInstanceForm =>
        val verification = newInstanceForm._3 < Application.map.width && newInstanceForm._4 < Application.map.height
        if(verification) {
          val oldInstance = Application.map.getInstanceById(newInstanceForm._1)
          val newInstance = Instance(
            oldInstance.id,
            newInstanceForm._2,
            Coordinates(newInstanceForm._3, newInstanceForm._4),
            oldInstance.properties,
            oldInstance.concept
          )
          Application.map.updateInstance(oldInstance, newInstance)
        }
      }
    )
    Redirect(routes.Application.index())
  }

}
