package controllers

import actors.socket._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object MapController extends Controller with Secured {

  def show = withAuth {
    implicit request => userId =>
      Ok(views.html.map.index())
  }

  val mapSocketActor = Akka.system.actorOf(Props[WebSocketActor])

  /**
   * This function create a WebSocket using the enumerator linked to the current user,
   * retrieved from the TaskActor.
   */
  def indexWS = withAuthWS {
    userId =>

      implicit val timeout = Timeout(3 seconds)

      /* Using the ask pattern of Akka to get the enumerator for that user */
      (mapSocketActor ? StartSocket(userId)) map { enumerator =>
        /* Create an Iteratee which ignore the input and
         * send a SocketClosed message to the actor when
         * connection is closed from the client. */
        (Iteratee.ignore[JsValue] map {
          _ =>
            mapSocketActor ! SocketClosed(userId)
        }, enumerator.asInstanceOf[Enumerator[JsValue]])
      }
  }

  def start = withAuth {
    userId => implicit request =>
      mapSocketActor ! Start(userId)
      Ok("")
  }


  def stop = withAuth {
    userId => implicit request =>
      mapSocketActor ! Stop(userId)
      Ok("")
  }

  /*def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.MapController.indexWS,
          routes.javascript.MapController.start,
          routes.javascript.MapController.stop
        )
      ).as("text/javascript")
  }*/

}

trait Secured {

  def username(request: RequestHeader) = {
    /* Verify or create session, this should be a real login. */
    request.session.get(Security.username)
  }

  /**
   * When user not have a session, this function create a
   * random userId and reload index page
   */
  def unauthF(request: RequestHeader) = {
    val newId: String = new Random().nextInt().toString
    Redirect(routes.MapController.show()).withSession(Security.username -> newId)
  }

  /**
   * Basic authentication system
   * try to retieve the username, call f() if it is present,
   * or unauthF() otherwise
   */
  def withAuth(f: => Int => Request[_ >: AnyContent] => Result): EssentialAction = {
    Security.Authenticated(username, unauthF) { username =>
      Action(request => f(username.toInt)(request))
    }
  }

  /**
   * This function provide a basic authentication for WebSocket, likely withAuth function try to retrieve the
   * the username form the session, and call f() funcion if find it,
   * or create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
   * if username is none
   */
  def withAuthWS(f: => Int => Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])]): WebSocket[JsValue, JsValue] = {
    println("caca")
    WebSocket.tryAccept[JsValue] { request =>
      username(request) match {
        case Some(id) =>
          f(id.toInt).map(tuple => Right(tuple))
        case _ =>
          Future(Left(Redirect(routes.MapController.show())))
      }
    }
  }
}