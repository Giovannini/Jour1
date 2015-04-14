package controllers.map

import actors.socket._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import controllers.Application
import models.graph.Instance
import models.graph.concept.{Concept, ConceptDAO}
import models.graph.relation.{RelationSqlDAO, Relation}
import models.interaction.action.{InstanceAction, InstanceActionParser}
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
import scala.util.{Try, Random, Failure, Success}

/**
 * Controller that manages the display of the map and the websockets to update the map for the view
 * @author Thomas GIOVANNINI
 */
object MapController extends Controller with Secured {

  /**
   * Displays the map
   * @author Julien PRADET
   * @return the html frame for the map view
   */
  def show = withAuth {
    implicit request => userId =>
      Ok(views.html.map.index())
  }

  /**
   * Get all the relation existing for a given concept except for its instances
   * @author Thomas GIOVANNINI
   * @param conceptId of the concept the relations are desired
   */
  def getAllActionsOf(conceptId: Long) = Action {
    val t1 = System.currentTimeMillis()
    val relations = ConceptDAO.getReachableRelations(conceptId)
    val actions = relations.filter(_._1.isAnAction)
      .map(relationnedConceptToJson)
    val t2 = System.currentTimeMillis()
    println("Getting all actions took " + (t2 - t1) + "ms.")
    Ok(Json.toJson(actions))
  }

  /**
   * Parse a relationned concept to Json
   * @author Thomas GIOVANNINI
   * @param tuple containing the relationned concept
   * @return a JsValue representing the relationned concept
   */
  def relationnedConceptToJson(tuple: (Relation, Concept)): JsValue = {
    Json.obj("relationID" -> JsNumber(tuple._1.id),
      "relationLabel" -> JsString(tuple._1.label),
      "conceptId" -> JsNumber(tuple._2.hashCode()))
  }

  /**
   * Receive a json action and execute it server side, then actualize the client-side.
   * json model: {action: "action", instances: [instance, instance, ...]}
   * @author Thomas GIOVANNINI
   */
  def executeAction = Action(parse.json) { request =>

    /*
     * Parse json request and execute it.
     * @author Thomas GIOVANNINI
     */
    def execution(request: Request[JsValue]): Boolean = {
      val jsonRequest = Json.toJson(request.body)
      val actionReference = (jsonRequest \ "action").as[Long]
      val actionId = RelationSqlDAO.getActionIdFromRelationId(actionReference)
      val actionArguments = (jsonRequest \ "instances").as[List[Long]]
      InstanceActionParser.parseAction(actionId, actionArguments)
    }

    val t1 = System.currentTimeMillis()
    val result = execution(request)
    val t2 = System.currentTimeMillis()
    println("Executing action took " + (t2 - t1) + "ms.")
    if (result) {
      Ok("Ok")
    }
    else {
      Ok("Error while executing this action")
    }
  }

  /**
   * Get all the instances that can be destination of a given action with its preconditions.
   * json model: {action: "action", instance: instanceID, concept: conceptID}
   * @author Thomas GIOVANNINI
   * @return a list of instances under JSON format
   */
  def getPossibleDestinationOfAction(initInstanceId: Long, relationId: Long, conceptId: Long)
  : Action[AnyContent] = Action {
    val sourceInstance = Application.map.getInstanceById(initInstanceId)
    val actionID = RelationSqlDAO.getActionIdFromRelationId(relationId)
    val action = InstanceActionParser.getAction(actionID)
    if (sourceInstance == Instance.error || action == InstanceAction.error) {
      Ok(Json.arr())
    } else {
      val destinationInstancesList = Application.map.getInstancesOf(conceptId)
      val reducedList = action.getDestinationList(sourceInstance, destinationInstancesList)
        .map(_.toJson)
      Ok(Json.toJson(reducedList))
    }
  }

  val mapSocketActor = Akka.system.actorOf(Props[WebSocketActor])

  /**
   * This function create a WebSocket using the enumerator linked to the current user.
   * @author Thomas GIOVANNINI
   */
  def indexWS = withAuthWS {
    userId =>
      implicit val timeout = Timeout(3 seconds)

      /* Using the ask pattern of Akka to get the enumerator for that user */
      (mapSocketActor ? StartSocket(userId)) map { enumerator =>
        /*
         * Create an Iteratee which ignore the input and send a SocketClosed message to the actor when
         * connection is closed from the client.
         */
        (Iteratee.foreach[JsValue] {
          case message: JsValue =>
            Try {
              val event = (message \ "event").as[String]
              val data = message \ "data"
//              println("Received event: " + event)
              (event, data)
            } match {
              case Success(("updateClient", data)) =>
                mapSocketActor ! UpdateClient(userId)

              case Success(("start", _)) =>
                mapSocketActor ! Start(userId)

              case Success(("stop", _)) =>
                mapSocketActor ! Stop(userId)

              case Success(("close", _)) =>
                mapSocketActor ! SocketClosed(userId)

              case Success((_, _)) =>
                println("Event doesnt exist")

              case Failure(ex) =>
                println("Data must match { event: ..., data: ... }")
                println(ex.getMessage)
                mapSocketActor ! SocketClosed(userId)
            }
          case _ =>
            println("Error with socket baby...")
            mapSocketActor ! SocketClosed(userId)
        }, enumerator.asInstanceOf[Enumerator[JsValue]])
      }
  }
}

/**
 * Trait using to characterize authentified socket connections
 * @author Thomas GIOVANNINI
 */
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
  def withAuth(f: => Long => Request[_ >: AnyContent] => Result): EssentialAction = {
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
  def withAuthWS(f: => Long => Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])]): WebSocket[JsValue, JsValue] = {
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