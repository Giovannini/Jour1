package controllers

import play.api._
import play.api.mvc._

object MapController extends Controller {
  def show = Action {
    Ok(views.html.map.index())
  }
}