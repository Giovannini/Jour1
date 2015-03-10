package controllers.graph

import play.api.mvc._

import scala.language.reflectiveCalls

object GraphVisualisation extends Controller {
   def index = Action {
     Ok(views.html.graph.index())
   }
 }
