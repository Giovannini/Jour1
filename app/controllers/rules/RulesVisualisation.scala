package controllers.rules

import models.rules.action.{Action => InstanceAction}
import play.api.mvc._

object RulesVisualisation extends Controller {

  def index = Action {
    Ok(views.html.rules.rules())
  }
}


