package controllers

import models.graph.ontology.{Relation, Concept}
import play.api.libs.json.{JsValue, JsNumber, JsString, Json}
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
    val actions = Concept.getReachableRelations(conceptId)
    val parents = Concept.getParents(conceptId).map((Relation("SUBTYPE_OF"), _))
    val children = Concept.getChildren(conceptId).map((Relation("PARENT_OF"), _))
    val relations = (actions ::: parents ::: children)
      .map(relationnedConceptToJson)
    
    Ok(Json.toJson(actions.map(relationnedConceptToJson)))
//    Ok(Json.toJson(relations))
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
}
