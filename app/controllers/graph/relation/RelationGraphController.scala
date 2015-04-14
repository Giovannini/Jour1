package controllers.graph.relation

import forms.graph.relation.RelationForm
import models.graph.relation.{Relation, RelationGraphDAO, RelationSqlDAO}
import play.api.libs.json.Json
import play.api.mvc._

/**
 * CRUD of the relations in the graph
 */
object RelationGraphController extends Controller {
  /**
   * Create a new kind of relations in the graph
   * @param label label of the relation
   * @return Status of the request with explained errors in JSON or Ok message
   */
  def createRelation(label: String) = Action(parse.json) { request =>
    val relationForm = RelationForm.graphForm.bind(request.body)
    relationForm.fold(
      hasErrors = {
        form => BadRequest(form.errorsAsJson)
      },
      success = {
        tuple => {
          val source = tuple._1
          val destination = tuple._2
          val relation = tuple._3
          val result = RelationGraphDAO.addRelationToDB(source.id, relation.id, destination.id)
          if(result) {
            Ok("Relation has been added to the graph")
          } else {
            InternalServerError(Json.obj(
              "global" -> "Impossible to add to the graph"
            ))
          }
        }
      }
    )
  }

  /**
   * Action that adds a new relation to the graph
   * @param label of the relation
   * @param source id of the source concept
   * @param target id of the target concept
   * @return Status of the request with explained errors in JSON or Ok message
   */
  def addRelationToGraph(label: String, source: Long, target: Long) = Action { request =>
    val relation = RelationSqlDAO.getByName(label)
    if(relation == Relation.error) {
      BadRequest("Unknown relation")
    } else {
      val result = RelationGraphDAO.addRelationToDB(source, relation.id, target)
      if (result) {
        Ok("Relation added to the graph")
      } else {
        InternalServerError("Couldnt add the relation to the graph")
      }
    }
  }

  /**
   * Action that removes a new relation to the graph
   * @param label of the relation
   * @param source id of the source concept
   * @param target id of the target concept
   * @return Status of the request with explained errors in JSON or Ok message
   */
  def removeRelationToGraph(label: String, source: Long, target: Long) = Action { request =>
    val relation = RelationSqlDAO.getByName(label)
    if(relation == Relation.error) {
      BadRequest("Unknown relation")
    } else {
      val result = RelationGraphDAO.removeRelationFromDB(source, relation.id, target)
      if (result) {
        Ok("Relation removed from the graph")
      } else {
        InternalServerError("Couldnt remove the relation from the graph")
      }
    }
  }

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

  def updateRelation(label: String) = Action(parse.json) { request =>
    val oldRelation = RelationSqlDAO.getByName(label)
    if(oldRelation == Relation.error) {
      BadRequest("Relation does not exist")
    } else {
      val relationForm = RelationForm.graphForm.bind(request.body)
      relationForm.fold(
        hasErrors = {
          form => BadRequest(form.errorsAsJson)
        },
        success = {
          tuple => {
            val source = tuple._1
            val destination = tuple._2
            val relation = tuple._3

            val removeResult = RelationGraphDAO.removeRelationFromDB(source.id, oldRelation.id, destination.id)
            if(!removeResult) {
              InternalServerError(Json.obj(
                "global" -> "Impossible to update the graph"
              ))
            } else {
              val result = RelationGraphDAO.addRelationToDB(source.id, relation.id, destination.id)
              if (result) {
                Ok("Relation has been added to the graph")
              } else {
                RelationGraphDAO.addRelationToDB(source.id, oldRelation.id, destination.id)
                InternalServerError(Json.obj(
                  "global" -> "Impossible to update the graph"
                ))
              }
            }
          }
        }
      )
    }
  }

  def deleteRelation(label: String) = Action(parse.json) { request =>
    val relationForm = RelationForm.graphForm.bind(request.body)
    relationForm.fold(
      hasErrors = {
        form => BadRequest(form.errorsAsJson)
      },
      success = {
        tuple => {
          val source = tuple._1
          val destination = tuple._2
          val relation = tuple._3

          val result = RelationGraphDAO.removeRelationFromDB(source.id, relation.id, destination.id)
          if(result) {
            Ok("Relation deleted")
          } else {
            InternalServerError(Json.obj(
              "global" -> "Impossible to remove from the graph"
            ))
          }
        }
      }
    )
  }
}