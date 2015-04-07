package controllers.rules

import play.api.mvc._

object RelationController extends Controller {
  /**
   * Displays the SinglePageApp for CRUDing InstanceAction
   * @return
   */
  def index = Action {
    Ok(views.html.action.index())
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