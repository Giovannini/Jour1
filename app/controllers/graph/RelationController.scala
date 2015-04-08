package controllers.graph

import forms.graph.ontology.relation.RelationForm
import models.graph.NeoDAO
import models.graph.ontology.relation.{RelationDAO, Relation}
import play.api.libs.json.Json
import play.api.mvc._

object RelationController extends Controller {
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
          val result = NeoDAO.addRelationToDB(source.id, relation.id, destination.id)
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

  def addRelationToGraph(label: String, source: Long, target: Long) = Action { request =>
    val relation = RelationDAO.getByName(label)
    if(relation == Relation.error) {
      BadRequest("Unknown relation")
    } else {
      val result = NeoDAO.addRelationToDB(source, relation.id, target)
      if (result) {
        Ok("Relation added to the graph")
      } else {
        InternalServerError("Couldnt add the relation to the graph")
      }
    }
  }

  def removeRelationToGraph(label: String, source: Long, target: Long) = Action { request =>
    val relation = RelationDAO.getByName(label)
    if(relation == Relation.error) {
      BadRequest("Unknown relation")
    } else {
      val result = NeoDAO.removeRelationFromDB(source, relation.id, target)
      if (result) {
        Ok("Relation removed from the graph")
      } else {
        InternalServerError("Couldnt remove the relation from the graph")
      }
    }
  }

  def getRelation(label: String) = Action {
    val relation = RelationDAO.getByName(label)
    if(relation == Relation.error) {
      NotFound("Invalid")
    } else {
      val result = NeoDAO.getRelationsById(relation.id)
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
    val oldRelation = RelationDAO.getByName(label)
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

            val removeResult = NeoDAO.removeRelationFromDB(source.id, oldRelation.id, destination.id)
            if(!removeResult) {
              InternalServerError(Json.obj(
                "global" -> "Impossible to update the graph"
              ))
            } else {
              val result = NeoDAO.addRelationToDB(source.id, relation.id, destination.id)
              if (result) {
                Ok("Relation has been added to the graph")
              } else {
                NeoDAO.addRelationToDB(source.id, oldRelation.id, destination.id)
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

          val result = NeoDAO.removeRelationFromDB(source.id, relation.id, destination.id)
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