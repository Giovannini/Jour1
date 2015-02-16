package controllers.rules

import play.api._
import play.api.mvc._
import models.rules.Rule

object RulesVisualisation extends Controller {
  def index = Action {
    Ok(views.html.rules.index(Rule.list))
  }
}


