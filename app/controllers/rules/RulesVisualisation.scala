package controllers.rules

import models.rules.action.{Action => InstanceAction}
import play.api.mvc._

object RulesVisualisation extends Controller {
  def index = Action {
    Ok(views.html.rules.rules())
  }

  def load(id: Long) = Action {
    InstanceAction.getById(id) match {
      case InstanceAction.error => Ok(views.html.rules.rules())
      case action: InstanceAction => Ok(views.html.rules.show(action))
      case _ => Ok(views.html.rules.rules())
    }
  }
}


