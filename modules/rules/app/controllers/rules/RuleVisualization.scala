package controllers.rules

import play.api.mvc.{Controller, Action}

/**
 * Created by giovannini on 2/11/15.
 */
object RuleVisualization extends Controller{
  def index = Action {
    Ok(views.html.rules.index())
  }
}
