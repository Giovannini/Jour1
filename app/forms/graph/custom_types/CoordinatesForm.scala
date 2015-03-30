package forms.graph.custom_types

import controllers.Application
import models.graph.custom_types.Coordinates
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

object CoordinatesForm {
  val form = Form(
    mapping(
      "x" -> number.verifying(min(0), max(Application.map.width)),
      "y" -> number.verifying(min(0), max(Application.map.height))
    )(Coordinates.apply)(Coordinates.unapply)
  )
}
