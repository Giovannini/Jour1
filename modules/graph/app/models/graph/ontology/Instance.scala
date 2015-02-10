package models.graph.ontology

import models.graph.custom_types.Coordinates
import play.api.libs.json._


/**
 * Model for an instance of the ontology
 * @author Thomas GIOVANNINI
 * @param label of the instance
 */
case class Instance(id:             Int,
                    label:          String,
                    coordinates:    Coordinates,
                    properties:     List[ValuedProperty],
                    concept:        Concept) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$") && isValid)

  /**
   * Parse an Instance to Json
   * @author Thomas GIOVANNINI
   * @return the Json form of the instance
   */
  def toJson: JsValue = Json.obj(
    "id" -> id,
    "label" -> label,
    "coordinates" -> Json.obj("x" -> JsNumber(coordinates.x), "y" -> JsNumber(coordinates.y)),
    "properties" -> properties.map(_.toJson),
    "concept" -> JsNumber(concept.id)
  )

  override def hashCode = label.hashCode + concept.hashCode

  /**
   * Parse an Instance for it to be used in a Cypher statement
   * @author Thomas GIOVANNINI
   * @return a cypher statement compatible string representing the instance
   */
  def toNodeString = "(" + label.toLowerCase +
    " { id: \"" + id + "\","+
    " label: \"" + label + "\","+
    " coordinate_x: " + coordinates.x + ", coordinate_y: " + coordinates.y + ","+
    " concept: " + concept.id + ","+
    " type: \"INSTANCE\","+
    " id: " + hashCode + "," +
    " properties: ["+properties.map("\"" + _ + "\"").mkString(",") + "]})"

  /**
   * Method to see if the instance matches its concept properties
   * @author Thomas GIOVANNINI
   * @return true if it does
   *         false else
   */
  def isValid = {
    concept.properties.toSeq == properties.map(_.property).toSeq
  }

  /**
   * Method to add a property to the instance
   * @author Thomas GIOVANNINI
   * @param property to add
   * @return the same instance with the new property added
   */
  def withProperty(property: Property): Instance = {
    Instance(id, label, coordinates, property.defaultValuedProperty :: properties, concept)
  }

  /**
   * Method to give coordinates to the instance
   * @param newCoordinates to give to the instance
   * @return the same instance with updated coordinates
   */
  def at(newCoordinates: Coordinates): Instance = {
    Instance(id, label, newCoordinates, properties, concept)
  }

  /**
   * Method to give id to the instance
   * @param newId to give to the instance
   * @return the same instance with updated id
   */
  def withId(newId: Int): Instance = {
    Instance(newId, label, coordinates, properties, concept)
  }
}

object Instance {

  val error = Instance(0, "XXX", Coordinates(0,0), List(), Concept.error)

  /**
   * Transform a json representing an instance into the Instance it represents
   * @author Thomas GIOVANNINI
   * @param jsonInstance the instance to parse
   * @return the represented instance
   */
  def parseJson(jsonInstance: JsValue): Instance = {
    val id = (jsonInstance \ "id").as[Int]
    val label = (jsonInstance \ "label").as[String]
    val x_coordinate = (jsonInstance \ "coordinates" \ "x").as[Int]
    val y_coordinate = (jsonInstance \ "coordinates" \ "y").as[Int]
    val coordinates = Coordinates(x_coordinate, y_coordinate)
    val properties = (jsonInstance \ "properties").as[List[JsValue]].map(ValuedProperty.parseJson)
    val conceptId = (jsonInstance \ "concept").as[Int]
    Concept.getById(conceptId) match {
      case Some(concept) => Instance(id, label, coordinates, properties, concept)
      case _ => error
    }
  }

  /**
   * Create an instance of a certain concept with random attributes
   * @author Thomas GIOVANNINI
   * @param concept the concept of which the instance is desired
   * @return a new instance
   */
  def createRandomInstanceOf(concept: Concept): Instance = {
    Instance(0,
      concept.label,
      Coordinates(0,0),
      concept.properties.map(_.defaultValuedProperty),
      concept)
  }
}

