package controllers.rules

import play.api.mvc._

object RulesVisualisation extends Controller {
  def rules = Action {
    Ok(views.html.rules.index())
  }
}


