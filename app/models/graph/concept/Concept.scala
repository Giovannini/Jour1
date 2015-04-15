package models.graph.concept

import controllers.Application
import models.graph.DisplayProperty
import models.graph.property.{Property, ValuedProperty}
import models.graph.relation.{Relation, RelationSqlDAO}
import models.intelligence.need.{Need, NeedDAO}
import models.interaction.action.InstanceAction
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author Thomas GIOVANNINI
 * @param label for the concept
 * @param _properties of this concept
 * @param _rules of this concept
 * @param displayProperty for tis concept to be displayed
 */
case class Concept(
  label: String,
  private val _properties: List[ValuedProperty],
  private val _rules: List[ValuedProperty],
  private val _needs: List[Need],
  displayProperty: DisplayProperty) {


  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$"))
  /**
   * Retrieve the properties of a Concept but also the one from its parents
   * @author Thomas GIOVANNINI
   */
  lazy val parents: List[Concept] = getParents

  lazy val descendance: List[Concept] = getDescendance
  lazy val properties: List[ValuedProperty] = (_properties ::: parents.flatMap(_.properties)).distinct

  lazy val rules: List[ValuedProperty] = ValuedProperty.distinctProperties(_rules ::: parents.flatMap(_.rules))
  lazy val needs: List[Need] = (_needs ::: parents.flatMap(_.needs)).distinct
  val id: Long = hashCode

  /**
   * Overriding method hashCode
   * @author Tomas GIOVANNINI
   * @return an hashcode for the concept
   */
  override def hashCode = {
    label.hashCode + "CONCEPT".hashCode()
  }

  def simplify: Concept = {
    Concept(this.label, List(), List(), List(), this.displayProperty)
  }

  /**
   * Parse a Concept to Json
   * @author Thomas GIOVANNINI
   * @return the Json form of the concept
   */
  def toJson: JsValue = {
    Json.obj("label" -> JsString(label),
      "properties" -> _properties.map(_.toJson),
      "rules" -> _rules.map(_.toJson),
      "needs" -> _needs.map(_.toJson),
      "type" -> JsString("CONCEPT"),
      "id" -> JsNumber(hashCode()),
      "display" -> displayProperty.toJson)
  }

  def toSimplifiedJson: JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label)
    )
  }

  /**
   * Parse a Concept for it to be used in a Neo4J Cypher statement
   * @author Thomas GIOVANNINI
   * @return a cypher statement compatible string representing the concept
   */
  def toNodeString = {
    "(" + label.toLowerCase + toNodePropertiesString + ")"
  }

  def toNodePropertiesString = {
    "{ label: \"" + label + "\"," +
    " properties: [" + _properties.map("\"" + _.toString + "\"").mkString(",") + "]," +
    " rules: [" + _rules.map("\"" + _.toString + "\"").mkString(",") + "]," +
    " needs: [" + _needs.map("\"" + _.id + "\"").mkString(",") + "]," +
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
    rules.find(_.property.label == property.label)
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
  private def getParents: List[Concept] = {
    ConceptDAO.getParents(id)
  }

  /**
   * Get descendance of the concept
   * @author Thomas GIOVANNINI
   * @return all children of the concept and their children
   */
  private def getDescendance: List[Concept] = {
    ConceptDAO.getChildren(id).flatMap(concept => concept :: concept.getDescendance)
  }

  def getMoodRelations: List[(Relation, Concept)] = {
    ConceptDAO.getReachableRelations(id).filter(_._1.isAMood)
  }

  def isSubConceptOf(concept: Concept): Boolean = {
    (this :: parents).contains(concept)
  }

  /**
   * Get all possible actions for this concept and the list of associated destination concepts
   * @author Thomas GIOVANNINI
   * @return a map with all possibles actions as keys and with the list of all possible destinations as values
   */
  lazy val getPossibleActionsAndDestinations: Map[InstanceAction, List[Concept]] = {
    ConceptDAO.getReachableRelations(id)
      .groupBy(_._1)
      .map(tuple => {
        (
          RelationSqlDAO.getActionFromRelationId(tuple._1.id),
          tuple._2.unzip._2.flatMap(concept => concept :: concept.getDescendance)
        )
      })
  }

  def withNeeds(needs: List[Need]): Concept = {
    Concept(
      this.label,
      this._properties,
      this._rules,
      needs,
      this.displayProperty
    )
  }

  /**
   * Get only rules of the concept without those of its parents
   * @author Aurélie LORGEOUX
   * @return list of rules
   */
  def getOwnRules: List[ValuedProperty] = _rules

  /**
   * Get only properties of the concept without those of its parents
   * @author Aurélie LORGEOUX
   * @return list of properties
   */
  def getOwnProperties: List[ValuedProperty] = _properties

  /**
   * Get only the needs of the concept without thos of its parents
   * @author Julien PRADET
   * @return list of needs
   */
  def getOwnNeeds: List[Need] = _needs
}

object Concept {

  implicit val connection = Application.neoConnection

  val error = Concept("XXX", List(), List(), List(), DisplayProperty())
  val any = Concept("Any", List(), List(), List(), DisplayProperty())
  val self = Concept("Self", List(), List(), List(), DisplayProperty())

  def apply(label: String):Concept = Concept(label, List(), List(), List(), DisplayProperty())

  /**
   * Parse a Json value to a concept
   * @author Thomas GIOVANNINI
   * @param jsonConcept the json to parse
   * @return the proper concept
   */
  def parseJson(jsonConcept: JsValue): Concept = {
    val label = (jsonConcept \ "label").as[String]
    val properties = (jsonConcept \ "properties").as[List[JsValue]].map(ValuedProperty.parseJson)
    val rules = (jsonConcept \ "rules").as[List[JsValue]].map(ValuedProperty.parseJson)
    val needs = (jsonConcept \ "needs").as[List[Long]].map(NeedDAO.getById)
    val displayProperty = DisplayProperty.parseJson(jsonConcept \ "displayProperty")
    Concept(label, properties, rules, needs, displayProperty)
  }

}