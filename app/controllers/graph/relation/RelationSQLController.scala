package controllers.graph.relation

import play.api.mvc._

/**
 * CRUD relations in the SQL DB
 * @author Julien PRADET
 */
object RelationSQLController extends Controller {
  /**
   * Displays the SinglePageApp for CRUDing Relations in SQL DB
   * @return
   */
  def index = Action {
    Ok(views.html.graph.relation.relations())
  }

  /**
   * Make sure that the request is in json
   * Otherwise, it redirects to the graph index
   * @param request request sent by the user
   * @param action action to display if the request is correct
   * @return redirects to the index or displays the information matching the request
   */
  def jsonOrRedirectToIndex(request: Request[AnyContent])(action: Result) = {
    request.headers.get("Accept") match {
      case Some(accept) if accept.contains("application/json") =>
        action
      case _ =>
        Redirect("/relation")
    }
  }

  def readAction(label: String) = play.mvc.Results.TODO

  def createRelation(label: String) = play.mvc.Results.TODO

  def updateRelation(label: String) = play.mvc.Results.TODO

  def deleteRelation(label: String) = play.mvc.Results.TODO
}