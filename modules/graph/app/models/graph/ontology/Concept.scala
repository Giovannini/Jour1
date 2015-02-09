package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.Statement
import org.anormcypher.{Neo4jREST, CypherResultRow}
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author Thomas GIOVANNINI
 * @param label for the concept
 * @param properties of this concept
 */
case class Concept(label: String,
               properties: List[Property],
               color: String) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$") && color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$") )

  /**
   * Constructor giving automatically the color #AAAAAA to the context.
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties of the concept
   */
  def this(label: String, properties: List[Property]) = {
    this(label, properties, "#AAAAAA")
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
      "properties" -> properties.map(_.toJson),
      "type" -> JsString("CONCEPT"),
      "id" -> JsNumber(hashCode()),
      "color" -> JsString(color))
  }

  /**
   * Parse a Concept for it to be used in a Cypher statement
   * @author Thomas GIOVANNINI
   * @return a cypher statement compatible string representing the concept
   */
  def toNodeString = {
    "(" + label.toLowerCase +
      " { label: \"" + label + "\","+
      " properties: [" + properties.map(p => "\""+p+"\"").mkString(",") + "],"+
      " color: \""+color+"\","+
      " type: \"CONCEPT\","+
      " id:" + id + "})"
  }

}

object Concept {

  /**
   * Apply method for the second constructor
   * @author Thomas GIOVANNINI
   * @param label of the concept
   * @param properties of the concept
   * @return a new Concept
   */
  def apply(label: String, properties: List[Property]) = new Concept(label, properties)

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  /**
   * Parse a Json value to a concept
   * @author Thomas GIOVANNINI
   * @param jsonConcept the json to parse
   * @return the proper concept
   */
  def parseJson(jsonConcept: JsValue): Concept = {
    val label = (jsonConcept \ "label").as[String]
    val properties = (jsonConcept \ "properties").as[List[JsValue]].map(Property.parseJson)
    val color = (jsonConcept \ "color").as[String]
    Concept(label, properties, color)
  }

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def parseRow(row: CypherResultRow): Concept = {
    val label = row[String]("concept_label")
    val properties = Property.rowToPropertiesList(row)
    val color = row[String]("concept_color")
    Concept(label, properties, color)
  }

  /**
   * Method to add a property to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param property to be added
   * @return the concept with the given property added
   */
  def addPropertyToConcept(concept: Concept, property: Property, defaultValue: Any): Concept ={
    NeoDAO.addPropertyToConcept(concept, property, defaultValue)
    this(concept.label, property :: concept.properties)
  }

  /**
   * Method to get a concept from its ID
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the desired concept
   * @return the desired concept
   */
  def getById(conceptId: Int): Option[Concept] = {
    val statement = Statement.getConceptById(conceptId)
    val cypherResultRowStream = statement.apply
    if(cypherResultRowStream.nonEmpty) {
      val row: CypherResultRow = statement.apply.head
      Some(parseRow(row))
    }else None
  }

  /**
   * Method to get all the concepts in the graph
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
   * Get all the relations from or to a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelations(conceptId: Int): List[(Relation, Concept)] = {
    Statement.getRelationsOf(conceptId)
      .apply
      .toList
      .filter(noInstance)
      .map{ row => (Relation.parseRow(row), Concept.parseRow(row))}
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
    val conceptRelations = getRelations(conceptId).filter(notASubtype)
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

  /**
   * Method to retrieve all the instances of a given concept and its children concepts' instances
   * @author Thomas GIOVANNINI
   * @param conceptId of the desired instances
   * @return a list of the desired instances
   */
  def getInstancesOf(conceptId : Int) : List[Instance] = {
    val instances = getInstanceOfSelf(conceptId)
    //println(instances)
    val instancesOfChildren = getChildren(conceptId)
      .map(children => getInstancesOf(children.id))
      .flatten
    instances ::: instancesOfChildren
  }

  /**
   * Method to retrieve all the instances of a given concept but not its children concepts' instances
   * @author Thomas GIOVANNINI
   * @param conceptId of the desired instances
   * @return a list of the desired instances
   */
  def getInstanceOfSelf(conceptId: Int): List[Instance] = {
    val statement = Statement.getInstances(conceptId)
    statement.apply
      .toList
      .map{ row => Instance.parseRowGivenConcept(row, conceptId)}
  }
}