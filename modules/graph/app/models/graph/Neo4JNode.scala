package models.graph

import models.graph.custom_types.Label
import play.api.libs.json.JsValue


/**
 * Model for a node in Neo4J
 * @param name of the node
 * @param jSon of the node
 */
case class Neo4JNode(name: Label, jSon: JsValue) {

  override def toString = "(" + name.content.toLowerCase + " " + jSon.toString + ")"

}
