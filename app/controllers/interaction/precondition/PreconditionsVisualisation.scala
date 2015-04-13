package controllers.interaction.precondition

import play.api.mvc._

object PreconditionsVisualisation extends Controller {
  def preconditions: Action[AnyContent] = Action {
    Ok(views.html.rules.preconditions())
  }
}


