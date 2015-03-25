package controllers

import play.api.mvc._

object MapController extends Controller {
  def show: Action[AnyContent] = Action {
    Ok(views.html.map.index())
  }
}