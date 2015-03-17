package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Coordinates, DisplayProperty, Statement}
import models.graph.ontology.property.{Property, PropertyDAO}
import models.graph.ontology.relation.Relation
import org.anormcypher.CypherResultRow
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author Thomas GIOVANNINI
 * @param label for the concept
 * @param _properties of this concept
 * @param rules of this concept
 * @param displayProperty for tis concept to be displayed
 */
case class Concept(label: String,
                   private val _properties: List[Property],
                   rules: List[ValuedProperty],
                   displayProperty: DisplayProperty) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$"))

  /**
   * Retrieve the properties of a Concept but also the one from its parents
   * @author Thomas GIOVANNINI
   */
  // TODO while creating instances, all the properties are not taken...
  lazy val properties: List[Property] = (_properties ::: getParents.flatMap(_.properties)).distinct

  /**
   * Constructor giving automatically the color #AAAAAA to the concept.
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties of the concept
   */
  def this(label: String, properties: List[Property], rules: List[ValuedProperty]) = {
    this(label, properties, rules, DisplayProperty())
  }

  val id: Long = hashCode

  override def hashCode = label.hashCode + "CONCEPT".hashCode()

  override def equals(obj: Any) = {
    obj.isInstanceOf[Concept] && obj.asInstanceOf[Concept].id == this.id
  }

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
      " { label: \"" + label + "\"," +
      " properties: [" + properties.map(_.id).mkString(",") + "]," +
      " rules: [" + rules.map(p => "\"" + p + "\"").mkString(",") + "]," +
      " display: \"" + displayProperty + "\"," +
      " type: \"CONCEPT\"," +
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

  /**
   * Retrieve the rules of a Concept but also the one from its parents
   * @author Thomas GIOVANNINI
   * @return a list of rules
   */
  def getAllRules: List[ValuedProperty] = {
    ValuedProperty.keepHighestLevelRules(rules ::: getParents.flatMap(_.getAllRules), List())
  }

  /**
   * Get value for a given rule
   * @author Thomas GIOVANNINI
   * @param property to evaluate
   * @return the value associated to the given property in rules
   */
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

  /**
   * Get descendance of the concept
   * @author Thomas GIOVANNINI
   * @return all children of gthe concept and their children
   */
  def getDescendance: List[Concept] = {
    if (this == Concept.error){
      println("ERROR: Trying to get descendence of an error")
      List()
    }else {
      Concept.getChildren(id).flatMap(concept => concept :: concept.getDescendance)
    }
  }

}

object Concept {

  implicit val connection = NeoDAO.connection

  val error = Concept("XXX", List(), List(), DisplayProperty())

  /**
   * Apply method for the second constructor
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties of the concept
   * @return a new Concept
   */
  def apply(label: String, properties: List[Property], rules: List[ValuedProperty]) = new Concept(label, properties, rules)

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param label concept label
   * @param properties concept properties
   * @param rules concept rules
   * @param displayProperty concept display properties
   * @return a concept using these parameters
   */
  def applyForm(label: String, properties: List[Property], rules: List[ValuedProperty], displayProperty: DisplayProperty) = new Concept(label, properties, rules, displayProperty)

  /**
   * Unapply method used in the Concept controller
   * Allows to match a json to a form
   * @param concept concept
   * @return the different parts of a concept
   */
  def unapplyForm(concept: Concept): Option[(String, List[Property], List[ValuedProperty], DisplayProperty)] = {
    Some(concept.label, concept.properties, concept.rules, concept.displayProperty)
  }
  /**
   * Create a concept given a list of ids of properties instead of a list of properties directly.
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties id for the concept
   * @param rules of the concept
   * @return a concept
   */
  def create(label: String, properties: List[Long], rules: List[ValuedProperty]): Concept = {
    Concept(label, properties.map(PropertyDAO.getById), rules)
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
  def addPropertyToConcept(concept: Concept, property: Property): Concept = {
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
  def removePropertyFromConcept(concept: Concept, property: Property): Concept = {
    if (concept.properties.contains(property)) {
      NeoDAO.removePropertyFromConcept(concept, property)
      this(concept.label, concept.properties diff List(property), concept.rules)
    } else concept
  }

  /**
   * Method to add a rule to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param rule to be added
   * @return the concept with the given property added
   */
  def addRuleToConcept(concept: Concept, rule: ValuedProperty): Concept = {
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
  def removeRuleFromConcept(concept: Concept, rule: ValuedProperty): Concept = {
    if (concept.rules.contains(rule)) {
      NeoDAO.removeRuleFromConcept(concept, rule)
      this(concept.label, concept.properties, concept.rules diff List(rule))
    } else concept
  }

  /**
   * Method to get a concept from the graph by its ID
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the desired concept
   * @return the desired concept if exists
   */
  def getById(conceptId: Long): Concept = {
    val statement = Statement.getConceptById(conceptId)
    val cypherResultRowStream = statement.apply
    if (cypherResultRowStream.nonEmpty) {
      val row: CypherResultRow = statement.apply.head
      parseRow(row)
    } else error
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
  def getParents(conceptId: Long): List[Concept] = {
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
  def getChildren(conceptId: Long): List[Concept] = {
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
  def getDescendance(conceptId: Long): List[Concept] = {
    getChildren(conceptId).flatMap(concept => concept :: getDescendance(concept.id))
  }

  /**
   * Get all the relations from a given source concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsFrom(conceptId: Long): List[(Relation, Concept)] = {
    Statement.getRelationsFrom(conceptId).apply.view
      .filter(noInstance)
      .map(row => (Relation.DBGraph.parseRow(row), Concept.parseRow(row)))
      .toList
  }

  /**
   * Get all the relations to a given destination concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelationsTo(conceptId: Long): List[(Relation, Concept)] = {
    Statement.getRelationsTo(conceptId).apply
      .toList
      .filter(noInstance)
      .map { row => (Relation.DBGraph.parseRow(row), Concept.parseRow(row))}
  }

  /**
   * Get all the relations given a concept
   * @param conceptId id of the concept
   * @return (relations from, relations to)
   */
  def getRelationsFromAndTo(conceptId: Long): (List[(Relation, Concept)], List[(Relation, Concept)]) = {
    (getRelationsFrom(conceptId), getRelationsTo(conceptId))
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
  def getReachableRelations(conceptId: Long): List[(Relation, Concept)] = {
    val conceptRelations = getRelationsFrom(conceptId) /*.filter(notASubtype)*/
    val parentsRelations = getParentsRelations(conceptId)
    conceptRelations ::: parentsRelations
  }

  /**
   * Get the relations of parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the concept from which the parent relations are desired
   * @return a list of relations and concepts
   */
  def getParentsRelations(conceptId: Long): List[(Relation, Concept)] = {
    getParents(conceptId).flatMap {
      parent => getReachableRelations(parent.id)
    }
  }

  /**
   * Predicates indicating if a given relation is a SUBTYPE relation or not
   * @author Thomas GIOVANNINI
   * @param tuple containing the relation
   * @return true if the relation is not a subtype
   *         false else
   */
  private def notASubtype(tuple: (Relation, Concept)): Boolean = {
    tuple._1 != Relation("SUBTYPE_OF")
  }
}