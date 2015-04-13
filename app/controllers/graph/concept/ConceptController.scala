package controllers.graph.concept

import controllers.Application
import controllers.graph.GraphVisualisation
import forms.graph.concept.ConceptForm.form
import models.graph.DisplayProperty
import models.graph.concept.{Concept, ConceptDAO, ConceptStatement}
import models.interaction.action.InstanceAction
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

/**
 * Controller that operate CRUD operations on Concepts
 */
object ConceptController extends Controller {

  /**
   * Creates a concept in DB from a JSON request
   * @return Satus of the request
   */
  def createConcept(label: String): Action[JsValue] = {
    Action(parse.json) { request =>
      val newConceptForm = form.bind(request.body)
      newConceptForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          newConcept => println(newConcept)
            if (ConceptDAO.addConceptToDB(newConcept)) {
              Ok(newConcept.toJson)
            } else {
              InternalServerError(Json.obj("global" -> "Couldn't add concept to DB"))
            }
        }
      )
    }
  }

  def updateConcept(label: String): Action[JsValue] = {
    Action(parse.json) { request =>
      val newConceptForm = form.bind(request.body)
      newConceptForm.fold(
        hasErrors = {
          form => {
            BadRequest(form.errorsAsJson)
          }
        },
        success = {
          newConcept =>
            val conceptToUpdate = Concept(label, Nil, Nil, Nil, DisplayProperty())
            val updatedConcept = ConceptDAO.updateConcept(conceptToUpdate, newConcept)
            if (updatedConcept == Concept.error) {
              InternalServerError(Json.obj("global" -> "Couldn't update concept in DB"))
            } else {
              Ok(newConcept.toJson)
            }
        }
      )
    }
  }

  /**
   * Reads a concept in DB (and its relations)
   * @param search the label of the concept looked for
   * @param deepness the deepness of the children we're looking to display
   * @return a json that contains each nodes in an array, and the list of edges to display on the graph
   *         This format is compatible with AlchemyJS as a data source
   */
  def readConcept(search: String, deepness: Int): Action[AnyContent] = {
    Action { request =>
      GraphVisualisation.jsonOrRedirectToIndex(request) {
        require(deepness >= 0)

        /**
         * Gets the concepts related to a list of concept
         * These concepts can be children or parents
         * @param nodes Source nodes
         * @return The list of relations linked to the children/parents
         *         The list of children/parents
         */
        def getRelationsAndLinkedConceptsFromNodes(nodes: List[Concept]): (List[(Long, String, Long)], List[Concept]) = {
          nodes.flatMap(node => {
            val relations = ConceptDAO.getRelationsFromAndTo(node.id)
            (relations._1.map(link => ((node.id, link._1.label, link._2.id), link._2))
             ::: relations._2.map(link => ((link._2.id, link._1.label, node.id), link._2))).toSet
          }).unzip
        }

        /**
         * Gets all the concepts related
         * @param nodes source concepts, we're willing to find their relations
         * @param deepness how far we want to reach the related concepts
         * @return The list of relations linked to the children/parents
         *         The list of children/parents
         */
        def getChildrenDeep(nodes: List[Concept], deepness: Int): (List[(Long, String, Long)], List[Concept]) = {
          deepness match {
            case 0 => (Nil, nodes)
            case n =>
              val children = getRelationsAndLinkedConceptsFromNodes(nodes)
              val deeper = getChildrenDeep(children._2, n - 1)

              val concepts = nodes ::: children._2 ::: deeper._2
              val relations = children._1 ::: deeper._1

              (relations.distinct, concepts.distinct)
          }
        }

        /* Get the initial concept */
        val statement = ConceptStatement.getConceptByLabel(search) //TODO is it normal ConceptStatement appears here ?
        val cypherResultRowStream = statement.apply()(Application.neoConnection)
        if (cypherResultRowStream.nonEmpty) {
          // A concept has been found
          val nodes = cypherResultRowStream.map(ConceptDAO.parseRow)
          // Look for its relations
          val res = getChildrenDeep(nodes.toList, deepness)
          // Return the concept and his children in a format that is compatible with AlchemyJS data source
          Ok(
            Json.obj(
              "nodes" -> res._2.map(_.toJson),
              "edges" -> Json.toJson(res._1.map(relation => Json.obj(
                "source" -> relation._1,
                "label" -> relation._2,
                "target" -> relation._3
              )))
            )
          )
        } else {
          // No concept is to be found
          NotFound("\"" + search + "\" not found")
        }
      }
    }
  }

  /**
   * Remove a concept from DB (and its relations)
   * @author Aurélie LORGEOUX
   * @param label label of concept to remove
   * @return an action redirecting to the main page of application
   */
  def deleteConcept(label: String): Action[AnyContent] = {
    Action {
      val concept = ConceptDAO.getByLabel(label)
      println(ConceptDAO.removeConceptFromDB(concept))
      Redirect(controllers.routes.Application.index())
    }
  }

  def getActions(label: String) = Action {
    val concept = ConceptDAO.getByLabel(label)
    val actions = concept.getPossibleActionsAndDestinations.filter(_._1 != InstanceAction.error)
    Ok(Json.toJson(actions.map(_._1.toJson)))
  }
}