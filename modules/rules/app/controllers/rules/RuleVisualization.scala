package controllers

import controllers.graph.GraphVisualisation._
import play.api.mvc.{Controller, Action}

/**
 * Created by giovannini on 2/11/15.
 */
object RuleVisualization extends Controller{
  def index = Action {
    Ok(views.html.graph.index())
  }
}
