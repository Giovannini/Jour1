package controllers.graph.concept

import controllers.Application
import controllers.graph.GraphVisualisation
import controllers.graph.concept.need.NeedController
import forms.graph.concept.ConceptForm.form
import models.graph.DisplayProperty
import models.graph.concept.{Concept, ConceptDAO, ConceptStatement}
import models.intelligence.need.NeedDAO
import models.interaction.action.InstanceAction
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

/**
 * Controller that operate CRUD operations on Concepts
 */
object ConceptController extends Controller {

  /**
   * Creates a concept in DB from a JSON request
   * @author Julien PRADET
   * @return Status of the request with explained error or created concept in json
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

  /**
   * Update a concept from a json
   * @author Julien PRADET
   * @param label old label of the concept
   * @return Status of the request with explained errors or
   */
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

            val updatedNeeds = NeedController.updateNeedsForConcept(conceptToUpdate, newConcept)
            val conceptToAddToDB = newConcept.withNeeds(updatedNeeds)

            val updatedConcept = ConceptDAO.updateConcept(conceptToUpdate, conceptToAddToDB)
            if (updatedConcept == Concept.error) {
              InternalServerError(Json.obj("global" -> "Couldn't update concept in DB"))
            } else {
              NeedDAO.updateNeedsUsingConcept(conceptToUpdate.id, updatedConcept.id)

              Ok(newConcept.toJson)
            }

        }
      )
    }
  }

  /**
   * Reads a concept in DB (and its relations)
   * @author Julien PRADET
   * @param search the label of the concept looked for
   * @param deepness the deepness of the children we're looking to display
   * @return a json that contains each nodes in an array, and the list of edges to display on the graph
   *         This format is compatible with AlchemyJS as a data source
   */
  def readConcept(search: String, deepness: Int): Action[AnyContent] = {
    Action { request =>
      GraphVisualisation.jsonOrRedirectToIndex(request) {
        require(deepness >= 0)

        /*
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

        /*
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
   * @author Julien PRADET
   * @param label label of concept to remove
   * @return an action redirecting to the main page of application
   */
  def deleteConcept(label: String): Action[AnyContent] = {
    Action {
      val concept = ConceptDAO.getByLabel(label)
      val relations = ConceptDAO.getRelationsFromAndTo(concept.id)
      val needs = NeedDAO.getNeedsWhereConceptUsed(concept.id)
      if(relations._1.length > 0 || relations._2.length > 0 || needs.length > 0) {
        BadRequest(
          Json.obj(
            "relationsFrom" -> relations._1.map(item => Json.obj(
              "relation" -> item._1.label,
              "concept" -> item._2.label
            )),
            "relationsTo" -> relations._2.map(item => Json.obj(
              "relation" -> item._1.label,
              "concept" -> item._2.label
            )),
            "needs" -> needs.map(_.toJson)
          )
        )
      } else {
        val result = ConceptDAO.removeConceptFromDB(concept)
        if (result) {
          Ok("Concept successfully deleted")
        } else {
          InternalServerError("Impossible to delete Concept")
        }
      }
    }
  }

  /**
   * Gets all the existing actions that a concept can do
   * @param label of the concept
   * @return a list of actions that the concept can do
   */
  def getActions(label: String) = Action {
    val concept = ConceptDAO.getByLabel(label)
    val actions = concept.getPossibleActionsAndDestinations.filter(_._1 != InstanceAction.error)
    Ok(Json.toJson(actions.map(_._1.toJson)))
  }
}