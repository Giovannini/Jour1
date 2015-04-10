package controllers.map

import models.intelligence.Intelligence
import play.api.mvc._

object Evolution extends Controller {
  def next() = Action {
    Intelligence.calculate(nrOfWorkers = 4)
    Ok("OK")
  }

  def pause() = Action {
    // TODO
    Ok("OK")
  }

  def resume() = Action {
    // TODO
    Ok("OK")
  }

}
