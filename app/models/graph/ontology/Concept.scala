package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{DisplayProperty, Coordinates, Statement}
import models.graph.ontology.property.{PropertyDAO, Property}
import org.anormcypher.{Neo4jREST, CypherResultRow}
import play.Play
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author Thomas GIOVANNINI
 * @param label for the concept
 * @param properties of this concept
 * @param rules of this concept
 * @param displayProperty for tis concept to be displayed
 */
case class Concept(label: String,
               properties: List[Property],
               rules: List[ValuedProperty],
               displayProperty: DisplayProperty) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$"))

  /**
   * Constructor giving automatically the color #AAAAAA to the concept.
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties of the concept
   */
  def this(label: String, properties: List[Property], rules: List[ValuedProperty]) = {
    this(label, properties, rules, DisplayProperty())
  }

  val id = hashCode

  override def hashCode = label.hashCode + "CONCEPT".hashCode()

  /**
   * Parse a Concept to Json
   * @author Thomas GIOVANNINI
   * @return the Json form of the concept
   */
  def toJson: JsValue = {
    Json.obj("label" -> JsString(label),
      "properties" -> properties.map(p => JsNumber(p.id)),
      "rules" -> rules.map(_.toJson),
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
    "(" + label.toLowerCase +
      " { label: \"" + label + "\","+
      " properties: [" + properties.map(_.id).mkString(",") + "],"+
      " rules: [" + rules.map(p => "\""+p+"\"").mkString(",") + "],"+
      " display: \""+displayProperty+"\","+
      " type: \"CONCEPT\","+
      " id:" + id + "})"
  }

  /**
   * Create an instance of the concept at given coordinates
   * @author Thomas GIOVANNINI
   * @param coordinates to give to the instance
   * @return an instance of the concept at the given concept with evolutions.default properties values.
   */
  def createInstanceAt(coordinates: Coordinates): Instance = {
    Instance(0, label, coordinates, properties.map(_.defaultValuedProperty), this)
  }

  /*
   * Retrieve the properties of a Concept but also the one from its parents
   * @author Thomas GIOVANNINI
   * @return a list of properties
   */
  /*def getAllProperties: Set[Property] = {
    properties.toSet ++ getParents.flatMap(_.getAllProperties).toSet
  }*/

  /**
   * Retrieve the rules of a Concept but also the one from its parents
   * @author Thomas GIOVANNINI
   * @return a list of rules
   */
  def getAllRules: List[ValuedProperty] = {
    ValuedProperty.keepHighestLevelRules(rules ::: getParents.flatMap(_.getAllRules), List())
  }

  def getRuleValue(property: Property): Any = {
    rules.find(_.property == property)
      .getOrElse(property.defaultValuedProperty)
      .value
  }

  /**
   * Retrieve the parents of the concept
   * @author Thomas GIOVANNINI
   * @return the list of parent concepts
   */
  def getParents: List[Concept] = {
    Concept.getParents(id)
  }

  def getDescendance: List[Concept] = {
    Concept.getChildren(id).flatMap(concept => concept :: concept.getDescendance)
  }

}

object Concept {

  implicit val connection = Neo4jREST(Play.application.configuration.getString("serverIP"), 7474, "/db/data/")

  val error = Concept("XXX", List(), List(), DisplayProperty())

  /**
    * Apply method for the second constructor
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties of the concept
   * @return a new Concept
   */
  def apply(label: String, properties: List[Property], rules: List[ValuedProperty]) = new Concept(label, properties, rules)

  def create(label: String, properties: List[Long], rules: List[ValuedProperty]): Concept = {
    Concept(label, properties.map(PropertyDAO.getById), rules)
  }

  def create(label: String, properties: List[Long], rules: List[ValuedProperty], displayProperty: DisplayProperty): Concept = {
    Concept(label, properties.map(PropertyDAO.getById), rules, displayProperty)
  }


  /**
   * Parse a Json value to a concept
   * @author Thomas GIOVANNINI
   * @param jsonConcept the json to parse
   * @return the proper concept
   */
  def parseJson(jsonConcept: JsValue): Concept = {
    val label = (jsonConcept \ "label").as[String]
    val properties = (jsonConcept \ "properties").as[List[Long]].map(PropertyDAO.getById)
    val rulesProperties = (jsonConcept \ "rules").as[List[JsValue]].map(ValuedProperty.parseJson)
    val displayProperty = DisplayProperty.parseJson(jsonConcept \ "displayProperty")
    Concept(label, properties, rulesProperties, displayProperty)
  }

  /**
   * Read a Neo4J cypher result row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRow(row: CypherResultRow): Concept = {
    val label = row[String]("concept_label")
    val properties = row[Seq[Long]]("concept_prop").map(PropertyDAO.getById).toList
    val rulesProperty = ValuedProperty.rowToPropertiesList(row, "concept_rules")
    val display = DisplayProperty.parseString(row[String]("concept_display"))
    val result = Concept(label, properties, rulesProperty, display)
    result
  }

  /**
   * Method to add a property to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param property to be added
   * @return the concept with the given property added
   */
  def addPropertyToConcept(concept: Concept, property: Property): Concept ={
    NeoDAO.addPropertyToConcept(concept, property)
    this(concept.label, property :: concept.properties, concept.rules)
  }

  /**
   * Method to remove a property from a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be removed
   * @param property to be removed
   * @return the concept without the given property
   */
  def removePropertyFromConcept(concept: Concept, property: Property): Concept ={
    if (concept.properties.contains(property)){
      NeoDAO.removePropertyFromConcept(concept, property)
      this(concept.label, concept.properties diff List(property), concept.rules)
    }else concept
  }

  /**
   * Method to add a rule to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param rule to be added
   * @return the concept with the given property added
   */
  def addRuleToConcept(concept: Concept, rule: ValuedProperty): Concept ={
    NeoDAO.addRuleToConcept(concept, rule)
    this(concept.label, concept.properties, rule :: concept.rules)
  }

  /**
   * Method to remove a rule from a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be removed
   * @param rule to be removed
   * @return the concept without the given rule
   */
  def removeRuleFromConcept(concept: Concept, rule: ValuedProperty): Concept ={
    if(concept.rules.contains(rule)){
      NeoDAO.removeRuleFromConcept(concept, rule)
      this(concept.label, concept.properties, concept.rules diff List(rule))
    }else concept
  }
  
  /**
   * Method to get a concept from the graph by its ID
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the desired concept
   * @return the desired concept if exists
   */
  def getById(conceptId: Int): Concept = {
    val statement = Statement.getConceptById(conceptId)
    val cypherResultRowStream = statement.apply
    if(cypherResultRowStream.nonEmpty) {
      val row: CypherResultRow = statement.apply.head
      parseRow(row)
    }else error
  }

  /**
   * Method to get all the concepts in the graph database
   * @author Thomas GIOVANNINI
   * @return a list of concepts
   */
  def findAll: List[Concept] = {
    Statement.getAllConcepts.apply()
      .toList
      .map(parseRow)
  }

  /**
   * Method to retrieve all the parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getParents(conceptId: Int): List[Concept] = {
    val statement = Statement.getParentConcepts(conceptId)
    statement.apply
      .toList
      .map(Concept.parseRow)
  }

  /**
   * Method to retrieve all the children of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getChildren(conceptId: Int): List[Concept] = {
    val statement = Statement.getChildrenConcepts(conceptId)
    statement.apply
      .map(Concept.parseRow)
      .toList
  }

  /**
   * Get all the child node of a concept recursively
   * @author Thomas GIOVANNINI
   * @param conceptId of the desired concept
   * @return a list of concepts which are the given concept's descendance
   */
  def getDescendance(conceptId: Int): List[Concept] = {
    //println(conceptId)
    getChildren(conceptId).flatMap(concept => concept :: getDescendance(concept.id))
  }

  /**
   * Get all the relations from a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsFrom(conceptId: Int): List[(Relation, Concept)] = {
    Statement.getRelationsFrom(conceptId).apply
      .toList
      .filter(noInstance)
      .map{ row => (Relation.DBGraph.parseRow(row), Concept.parseRow(row))}
  }

  /**
   * Get all the relations to a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsTo(conceptId: Int): List[(Relation, Concept)] = {
    Statement.getRelationsTo(conceptId).apply
      .toList
      .filter(noInstance)
      .map{ row => (Relation.DBGraph.parseRow(row), Concept.parseRow(row))}
  }

  /**
   * Method to know if a row represents an instance or not
   * @author Thomas GIOVANNINI
   * @param row to test
   * @return true if the row doesn't represent an instance
   *         false else
   */
  private def noInstance(row: CypherResultRow): Boolean = {
    row[String]("node_type") != "INSTANCE"
  }

  /**
   * Method to retrieve all the possible actions for a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getReachableRelations(conceptId: Int): List[(Relation, Concept)] = {
    val conceptRelations = getRelationsFrom(conceptId)/*.filter(notASubtype)*/
    val parentsRelations = getParentsRelations(conceptId)
    conceptRelations ::: parentsRelations
  }

  /**
   * Get the relations of parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the concept from which the parent relations are desired
   * @return a list of relations and concepts
   */
  def getParentsRelations(conceptId: Int): List[(Relation, Concept)] = {
    getParents(conceptId).map {
      parent => getReachableRelations(parent.id)
    }.flatten
  }
  
  private def notASubtype(tuple: (Relation, Concept)): Boolean = tuple._1 != Relation("SUBTYPE_OF")
}