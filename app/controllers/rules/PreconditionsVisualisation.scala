package controllers.rules

import play.api.mvc._

object PreconditionsVisualisation extends Controller {
  def preconditions = Action {
    Ok(views.html.rules.preconditions())
  }
}


