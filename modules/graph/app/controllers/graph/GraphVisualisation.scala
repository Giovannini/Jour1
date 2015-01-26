package controllers.graph;

import play.api._
import play.api.mvc._

object GraphVisualisation extends Controller {
   def index = Action {
     Ok(views.html.graph.index())
   }
 }
