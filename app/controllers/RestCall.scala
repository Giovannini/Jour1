package controllers

import models.graph.ontology.{Relation, Concept}
import play.api.libs.json.{JsValue, JsNumber, JsString, Json}
import play.api.mvc.{Controller, Action}

/**
 * Controller to send Json data to client
 */
object RestCall extends Controller{

  def getAllConcepts = Action {
    val concepts = Concept.findAll
      .map(_.toJson)
    Ok(Json.toJson(concepts))
  }

  def getAllRelationsOf(conceptId: Int) = Action {
    val actions = Concept.getPossibleActions(conceptId)
    val parents = Concept.getParents(conceptId)
    val children = Concept.getChildren(conceptId)
    val relations = (actions ::: parents ::: children)
      .map(relationnedConceptToJson)
    Ok(Json.toJson(relations))
  }

  def relationnedConceptToJson(tuple: (Relation, Concept)): JsValue = {
    Json.obj( "relation" -> JsString(tuple._1.toString),
              "conceptId" -> JsNumber(tuple._2.hashCode()))
  }
}
