package models.graph.ontology

import models.graph.custom_types.{Coordinates, Label}
import play.api.libs.json._


/**
 * Model for an instance of the ontology
 * @author Thomas GIOVANNINI
 * @param label of the instance
 */
case class Instance(label:          Label,
                    coordinates:    Coordinates,
                    properties:     List[(Property, Any)],
                    concept:       Concept) {
  def toJson: JsValue = Json.obj(
    "label" -> label.content,
    "coordinates" -> Json.obj("x" -> coordinates.x, "y" -> coordinates.y),
    "properties" -> properties.map(p => p._1.toJson),
    "concept" -> concept.toJson
  )
}

