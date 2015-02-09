package controllers

import play.api.mvc._

object Application extends Controller {
  /**
   * Show the different urls of the project
   * @author Julien Pradet
   */
  def index = Action {
    Ok(views.html.index())
  }
}