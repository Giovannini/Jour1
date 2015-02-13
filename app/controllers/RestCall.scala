package controllers

import models.graph.ontology.{Relation, Concept}
import models.utils.ActionParser
import play.api.libs.json._
import play.api.mvc.{Controller, Action}

/**
 * Controller to send Json data to client
 */
object RestCall extends Controller{

  /**
   * Get all the concepts existing in the graph
   * @author Thomas GIOVANNINI
   */
  def getAllConcepts = Action {
    val concepts = Concept.findAll
      .map(_.toJson)
    Ok(Json.toJson(concepts))
  }

  /**
   * Get all the relation existing for a given concept except for its instances
   * @author Thomas GIOVANNINI
   * @param conceptId of the concept the relations are desired
   */
  def getAllRelationsOf(conceptId: Int) = Action {
    val relations = Concept.getReachableRelations(conceptId)
      .map(relationnedConceptToJson)
    
    Ok(Json.toJson(relations))
  }

  /**
   * Parse a relationned concept to Json
   * @author Thomas GIOVANNINI
   * @param tuple containing the relationned concept
   * @return a JsValue representing the relationned concept
   */
  def relationnedConceptToJson(tuple: (Relation, Concept)): JsValue = {
    Json.obj( "relation" -> JsString(tuple._1.toString),
              "conceptId" -> JsNumber(tuple._2.hashCode()))
  }

  /**
   * Receive a json action and execute it server side, then actualize the client-side.
   * json model: {action: "action", instances: [instance, instance, ...]}
   */
  def executeAction = Action(parse.json) { request =>
    println(request.body)
    val jsonRequest = Json.toJson(request.body)
    val actionReference = (jsonRequest \ "action").as[String]
    val actionArguments = (jsonRequest \ "instances").as[List[Int]]
    val result = Application.actionParser.parseAction(actionReference, actionArguments)
    if(result) Ok("Ok")
    else Ok("Error while executing this action")
  }
}
