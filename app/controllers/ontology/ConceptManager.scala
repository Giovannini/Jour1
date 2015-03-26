package controllers.ontology

import controllers.Application
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.Controller


object ConceptManager extends Controller{

  val instanceForm = Form(
    tuple(
      "id" -> number, //can't be modified
      "label" -> nonEmptyText,
      "properties" -> list(nonEmptyText),
      "rules" -> list(nonEmptyText), // TODO secure properties
      "color" -> nonEmptyText
    )
  )

}
