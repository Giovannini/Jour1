package models.graph.ontology

import models.graph.custom_types.Statement
import org.anormcypher.{Neo4jREST, CypherResultRow}
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Model for a concept of an ontology
 * @author
 * @param label for the concept
 * @param properties of this concept
 */
case class Concept(label: String,
                   properties: List[Property]) {
  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$"))

  val id = hashCode()

  override def hashCode() = label.hashCode + properties.hashCode()

  def toJson: JsValue = Json.obj( "label" -> JsString(label), "properties" -> properties.map(_.toJson),
    "type" -> JsString("CONCEPT"), "id" -> JsNumber(hashCode()))

  def toNodeString = "(" + label.toLowerCase +
    " { label: \"" + label + "\","+
    " properties: [" + properties.mkString(",") + "],"+
    " type: \"CONCEPT\","+
    " id:" + hashCode() + "})"

}

object Concept{

  // Setup the Rest Client
  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

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
    Concept(label, properties)
  }

  /**
   * Method to add a property to a given concept
   * @author Thomas GIOVANNINI
   * @param concept to which the property has to be added
   * @param property to be added
   * @return the concept with the given property added
   */
  def addPropertyToConcept(concept: Concept, property: Property): Concept =
    Concept(concept.label, property :: concept.properties)

  /**
   * Method to get a concept from its ID
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the desired concept
   * @return the desired concept
   */
  def getById(conceptId: Int): Concept = {
    val statement = Statement.getConceptById(conceptId)
    val row: CypherResultRow = statement.apply().head
    parseRow(row)
  }

  /**
   * Method to get all the concepts in the graph
   * @author Thomas GIOVANNINI
   * @return a list of concepts
   */
  def findAll: List[Concept] =
    Statement.getAllConcepts.apply()
      .toList
      .map(row => parseRow(row))

  /**
   * Method to retrieve all the parents of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getParents(conceptId: Int): List[(Relation, Concept)] = {
    val statement = Statement.getParentConcepts(conceptId)
    val subtypeRelation = Relation("SUBTYPE_OF")
    statement.apply().toList.map{ row => (subtypeRelation, Concept.parseRow(row))}
  }

  /**
   * Method to retrieve all the children of a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getChildren(conceptId: Int): List[(Relation, Concept)] = {
    val statement = Statement.getChildrenConcepts(conceptId)
    val parentRelation = Relation("PARENT_OF")
    statement.apply().toList.map{ row => (parentRelation, Concept.parseRow(row))}
  }

  /**
   * Get all the relations from or to a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId id of the source concept
   * @return a list of tuple containing the relation and destination concept.
   */
  def getRelations(conceptId: Int): List[(Relation, Concept)] = {
    Statement.getRelationsOf(conceptId)
      .apply()
      .toList
      .filter(row => row[String]("node_type") != "INSTANCE")
      .map{ row => (Relation.parseRow(row), Concept.parseRow(row))}
  }

  /**
   * Method to retrieve all the possible actions for a given concept
   * @author Thomas GIOVANNINI
   * @param conceptId the ID of the concept
   * @return a list of relations and concepts
   */
  def getPossibleActions(conceptId: Int): List[(Relation, Concept)] = {
    val relations = getRelations(conceptId)
      .filter(t => t._1 != Relation("SUBTYPE_OF"))
    val parentsRelation = getParents(conceptId)
      .map(tuple => getPossibleActions(tuple._2.id))
      .flatten
    relations ::: parentsRelation
  }

  /**
   * Method to retrieve all the instances of a given concept and its children concepts
   * @author Thomas GIOVANNINI
   * @param conceptId of the desired instances
   * @return a list of the desired instances
   */
  def getInstancesOf(conceptId : Int) : List[Instance] = {
    val statement = Statement.getInstances(conceptId)
    val instances = statement.apply()
      .toList
      .map{ row => Instance.parseRowGivenConcept(row, conceptId)}
    val instancesOfChildren = Concept.getChildren(conceptId)
      .map(_._2.id)
      .map(getInstancesOf)
      .flatten
    instances ::: instancesOfChildren
  }
}