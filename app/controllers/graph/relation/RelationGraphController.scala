package controllers.graph.relation

import models.graph.concept.ConceptDAO
import models.graph.relation.{Relation, RelationGraphDAO, RelationSqlDAO}
import play.api.libs.json.Json
import play.api.mvc._

/**
 * CRUD of the relations in the graph
 * @author Julien PRADET
 */
object RelationGraphController extends Controller {
  /**
   * Action that adds a new relation to the graph
   * @author Julien PRADET
   * @param label of the relation
   * @param source id of the source concept
   * @param target id of the target concept
   * @return Status of the request with explained errors in JSON or Ok message
   */
  def addRelationToGraph(label: String, source: Long, target: Long) = Action { request =>
    val relation = RelationSqlDAO.getByName(label)
    if(relation == Relation.error) {
      BadRequest(Json.obj("result" -> "Unknown relation"))
    } else {
      val result = RelationGraphDAO.addRelationToDB(source, relation.id, target)
      if (result) {
        ConceptDAO.removeCacheRelationFromConcept(source)
        ConceptDAO.removeCacheRelationToConcept(target)
        Ok(Json.obj("result" -> "Relation added to the graph"))
      } else {
        InternalServerError(Json.obj("result" -> "Couldnt add the relation to the graph"))
      }
    }
  }

  /**
   * Action that removes a new relation to the graph
   * @author Julien PRADET
   * @param label of the relation
   * @param source id of the source concept
   * @param target id of the target concept
   * @return Status of the request with explained errors in JSON or Ok message
   */
  def removeRelationToGraph(label: String, source: Long, target: Long) = Action { request =>
    val relation = RelationSqlDAO.getByName(label)
    if(relation == Relation.error) {
      BadRequest(Json.obj("result" -> "Unknown relation"))
    } else {
      val result = RelationGraphDAO.removeRelationFromDB(source, relation.id, target)
      if (result) {
        ConceptDAO.removeCacheRelationFromConcept(source)
        ConceptDAO.removeCacheRelationToConcept(target)
        Ok(Json.obj("result" -> "Relation removed from the graph"))
      } else {
        InternalServerError(Json.obj("result" -> "Couldnt remove the relation from the graph"))
      }
    }
  }

  /**
   * Get a relation and makes it fit with the graph viewer
   * @author Julien PRADET
   * @param label of the relation
   * @return a list of nodes affected by this relation and the edges that relate those nodes
   */
  def getRelation(label: String) = Action {
    val relation = RelationSqlDAO.getByName(label)
    if(relation == Relation.error) {
      NotFound("Invalid")
    } else {
      val result = RelationGraphDAO.getRelationsById(relation.id)
      val unzipped = result.unzip
      val nodes = (unzipped._1 ::: unzipped._2).distinct
      val edges = result.map(
        tuple => (tuple._1.id, label, tuple._2.id)
      )
      Ok(Json.obj(
        "nodes" -> Json.toJson(nodes.map(_.toJson)),
        "edges" -> Json.toJson(edges.map(relation => Json.obj(
          "source" -> relation._1,
          "label" -> relation._2,
          "target" -> relation._3
        )))
      ))
    }
  }
}