package controllers.rules

import controllers.Application
import models.rules.action.{Action => InstanceAction}
import play.api.mvc._

object RulesVisualisation extends Controller {

  def init = Action {
    InstanceAction.clear
    InstanceAction.save(Application.actionManager._actionAddInstanceAt)
    InstanceAction.save(Application.actionManager._actionRemoveInstanceAt)
    InstanceAction.save(Application.actionManager._actionMoveInstanceAt)
    Ok(views.html.rules.rules())
  }

  def index = Action {
    Ok(views.html.rules.rules())
  }
}


