package controllers.graph

import models.graph.NeoDAO
import models.graph.custom_types.{DisplayProperty, Statement}
import models.graph.ontology.{ValuedProperty, Concept}
import models.graph.ontology.property.Property
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

/**
 * Controller that operate CRUD operations on Concepts
 */
object ConceptController extends Controller {

  /**
   * Concept form
   */
  val conceptForm = Form(
    mapping(
      "label" -> nonEmptyText, //can't be modified
      "properties" -> list(
        mapping(
          "id" -> longNumber,
          "label" -> nonEmptyText,
          "valueType" -> nonEmptyText,
          "defaultValue" -> text
        )(Property.apply)(Property.unapplyForm)
      ),
      "rules" -> list(
        mapping(
          "property" -> mapping(
            "id" -> longNumber,
            "label" -> nonEmptyText,
            "valueType" -> nonEmptyText,
            "defaultValue" -> text
          )(Property.apply)(Property.unapplyForm),
          "value" -> text
        )(ValuedProperty.applyForm)(ValuedProperty.unapplyForm)
      ),
      "displayProperty" -> mapping(
        "color" -> text,
        "zindex" -> number
      )(DisplayProperty.apply)(DisplayProperty.unapply)
    )(Concept.applyForm)(Concept.unapplyForm)
  )

  /**
   * Creates a concept in DB from a JSON request
   * @return Satus of the request
   */
  def createConcept = Action(parse.json) { request =>
    println(request.body)
    val newConceptForm = conceptForm.bind(request.body)
    newConceptForm.fold(
      hasErrors = {
        form => {
          BadRequest(form.errorsAsJson)
        }
      },
      success = {
        newConcept => println(newConcept)
        if(NeoDAO.addConceptToDB(newConcept)) {
          Ok(newConcept.toJson)
        } else {
          InternalServerError("Couldn't add concept to DB")
        }
      }
    )
  }

  /**
   * Reads a concept in DB (and its relations)
   * @param search the label of the concept looked for
   * @param deepness the deepness of the children we're looking to display
   * @return a json that contains each nodes in an array, and the list of edges to display on the graph
   *         This format is compatible with AlchemyJS as a data source
   */
  def searchConcept(search: String, deepness: Int) = Action { request =>
    GraphVisualisation.jsonOrRedirectToIndex(request) {

      /**
       * Gets the concepts related to a list of concept
       * These concepts can be children or parents
       * @param nodes Source nodes
       * @return The list of relations linked to the children/parents
       *         The list of children/parents
       */
      def getRelationsAndLinkedConceptsFromNodes(nodes: List[Concept]): (List[(Long, String, Long)], List[Concept]) = {
        nodes.flatMap(node => {
          val relations = Concept.getRelationsFromAndTo(node.id)
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
          case 0 => (Nil, Nil)
          case n =>
            val children = getRelationsAndLinkedConceptsFromNodes(nodes)
            val deeper = getChildrenDeep(children._2, n - 1)

            val concepts = nodes:::children._2:::deeper._2
            val relations = children._1 ::: deeper._1

            (relations.distinct, concepts.distinct)
        }
      }

      /* Get the initial concept */
      val statement = Statement.getConceptByLabel(search)
      val cypherResultRowStream = statement.apply()(NeoDAO.connection)
      if(cypherResultRowStream.nonEmpty) {
        // A concept has been found
        val nodes = cypherResultRowStream.map(Concept.parseRow)
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
        NotFound("\""+search+"\" not found")
      }
    }
  }
}