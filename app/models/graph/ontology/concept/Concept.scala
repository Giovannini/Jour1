package models.graph.ontology.concept

import models.graph.NeoDAO
import models.graph.custom_types.DisplayProperty
import models.graph.ontology.ValuedProperty
import models.graph.ontology.concept.need.{NeedDAO, Need}
import models.graph.ontology.property.{Property, PropertyDAO}
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author Thomas GIOVANNINI
 * @param label for the concept
 * @param _properties of this concept
 * @param _rules of this concept
 * @param displayProperty for tis concept to be displayed
 */
case class Concept(label: String,
                   private val _properties: List[Property],
                   private val _rules: List[ValuedProperty],
                   needs: List[Need],
                   displayProperty: DisplayProperty) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$"))

  /**
   * Retrieve the properties of a Concept but also the one from its parents
   * @author Thomas GIOVANNINI
   */
  lazy val properties: List[Property] = (_properties ::: getParents.flatMap(_.properties)).distinct
  lazy val rules: List[ValuedProperty] = ValuedProperty.distinctProperties(_rules ::: getParents.flatMap(_.rules))

  val id: Long = hashCode

  /**
   * Overriding method hashCode
   * @author Tomas GIOVANNINI
   * @return an hashcode for the concept
   */
  override def hashCode = label.hashCode + "CONCEPT".hashCode()

  /**
   * Parse a Concept to Json
   * @author Thomas GIOVANNINI
   * @return the Json form of the concept
   */
  def toJson: JsValue = {
    Json.obj("label" -> JsString(label),
      "properties" -> properties.map(_.toJson),
      "rules" -> rules.map(_.toJson),
      "needs" -> needs.map(need => JsNumber(need.id)),
      "type" -> JsString("CONCEPT"),
      "id" -> JsNumber(hashCode()),
      "display" -> displayProperty.toJson)
  }

  /**
   * Parse a Concept for it to be used in a Neo4J Cypher statement
   * @author Thomas GIOVANNINI
   * @return a cypher statement compatible string representing the concept
   */
  def toNodeString = {
    "(" + label.toLowerCase + toNodePropertiesString +")"
  }

  def toNodePropertiesString = {
    "{ label: \"" + label + "\"," +
      " properties: [" + properties.map("\""+_.toString+"\"").mkString(",") + "]," +
      " rules: [" + rules.map("\""+_.toString+"\"").mkString(",") + "]," +
      " needs: [" + needs.map("\"" + _.id + "\"").mkString(",") + "]," +
      " display: \"" + displayProperty + "\"," +
      " type: \"CONCEPT\"," +
      " id:" + id + "}"
  }

  /*#################################
   * Methods related to rules
   #################################*/
    /**
   * Get value for a given rule
   * @author Thomas GIOVANNINI
   * @param property to evaluate
   * @return the value associated to the given property in rules
   */
  def getRuleValueByProperty(property: Property): Double = {
    rules.find(_.property == property)
      .getOrElse(property.defaultValuedProperty)
      .value
  }

  /*#################################
   * Methods related to graph relations
   #################################*/
  /**
   * Retrieve the parents of the concept
   * @author Thomas GIOVANNINI
   * @return the list of parent concepts
   */
  def getParents: List[Concept] = {
    ConceptDAO.getParents(id)
  }

  /**
   * Get descendance of the concept
   * @author Thomas GIOVANNINI
   * @return all children of the concept and their children
   */
  def getDescendance: List[Concept] = {
    ConceptDAO.getChildren(id).flatMap(concept => concept :: concept.getDescendance)
  }

}

object Concept {

  implicit val connection = NeoDAO.connection

  val error = Concept("XXX", List(), List(), List(), DisplayProperty())

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param label concept label
   * @param propertiesToParse concept properties
   * @param rules concept rules
   * @param displayProperty concept display properties
   * @return a concept using these parameters
   */
  def applyForm(
      label: String,
      propertiesToParse: List[String],
      rules: List[ValuedProperty],
      needs: List[Need],
      displayProperty: DisplayProperty)
    : Concept = {
    val properties = propertiesToParse.map(Property.parseString)
    Concept(label, properties, rules, needs, displayProperty)
  }

  /**
   * Unapply method used in the Concept controller
   * Allows to match a json to a form
   * @param concept concept
   * @return the different parts of a concept
   */
  def unapplyForm(concept: Concept)
    : Option[(String, List[String], List[ValuedProperty], List[Need], DisplayProperty)] = {
    Some(concept.label, concept.properties.map(_.toString), concept.rules, concept.needs, concept.displayProperty)
  }

  /**
   * Create a concept given a list of ids of properties instead of a list of properties directly.
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties id for the concept
   * @param rules of the concept
   * @param displayProperty of the concept
   * @return a concept
   */
  def identify(
      label: String,
      properties: List[Long],
      rules: List[ValuedProperty],
      needs: List[Long],
      displayProperty: DisplayProperty): Concept = {
    Concept(label, properties.map(PropertyDAO.getById), rules, needs.map(NeedDAO.getById), displayProperty)
  }

  /**
   * Parse a Json value to a concept
   * @author Thomas GIOVANNINI
   * @param jsonConcept the json to parse
   * @return the proper concept
   */
  def parseJson(jsonConcept: JsValue): Concept = {
    val label       = (jsonConcept \ "label").as[String]
    val properties  = (jsonConcept \ "properties").as[List[Long]].map(PropertyDAO.getById)
    val rules       = (jsonConcept \ "rules").as[List[JsValue]].map(ValuedProperty.parseJson)
    val needs       = (jsonConcept \ "needs").as[List[Long]].map(NeedDAO.getById)
    val displayProperty = DisplayProperty.parseJson(jsonConcept \ "displayProperty")
    Concept(label, properties, rules, needs, displayProperty)
  }
  
}