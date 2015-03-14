package controllers.graph

import models.graph.NeoDAO
import models.graph.custom_types.Statement
import models.graph.ontology.Concept
import models.graph.ontology.property.{Property, PropertyDAO}
import models.graph.ontology.relation.Relation
import models.rules
import play.api.libs.json.Json
import play.api.mvc._

import scala.annotation.tailrec

object GraphVisualisation extends Controller {
  def index = Action {
    Ok(views.html.graph.index())
  }

  def searchNodes(search: String, deepness: Int) = Action {
    def getRelationsAndLinkedConceptsFromNodes(nodes: List[Concept]): (List[(Long, String, Long)], List[Concept]) = {
      nodes.flatMap(node => {
        val relations = Concept.getRelationsFromAndTo(node.id)
        (relations._1.map(link => ((node.id, link._1.label, link._2.id), link._2))
          ::: relations._2.map(link => ((link._2.id, link._1.label, node.id), link._2))).toSet
      }).unzip
    }

    def getChildrenDeep(nodes: List[Concept], deepness: Int): (List[(Long, String, Long)], List[Concept]) = {
      deepness match {
        case 0 => (Nil, Nil)
        case n =>
          val children = getRelationsAndLinkedConceptsFromNodes(nodes)
          val deeper = getChildrenDeep(children._2, n - 1)

          val concepts = nodes:::children._2:::deeper._2
          val relations = children._1 ::: deeper._1

          (relations.toSet.toList, concepts.toSet.toList)
      }
    }

    val statement = Statement.getConceptByLabel(search)
    val cypherResultRowStream = statement.apply()(NeoDAO.connection)
    if(cypherResultRowStream.nonEmpty) {
      val nodes = cypherResultRowStream.map(Concept.parseRow)
      val res = getChildrenDeep(nodes.toList, deepness)
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
      NotFound("\""+search+"\" not found")
    }
  }

  def getProperty(propertyId: Int) = Action {
    val property = PropertyDAO.getById(propertyId)
    if(property == Property.error) {
      NotFound("Undefined property")
    } else {
      Ok(property.toJson)
    }
  }

  def getAction(actionLabel: String) = Action {
    val action = rules.action.Action.getByName(actionLabel)
    if(action == rules.action.Action.error) {
      NotFound("Undefined action")
    } else {
      Ok(action.toJson)
    }
  }
 }
