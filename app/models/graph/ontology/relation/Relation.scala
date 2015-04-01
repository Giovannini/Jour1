package models.graph.ontology.relation

import models.graph.NeoDAO
import models.graph.custom_types.Statement
import org.anormcypher.CypherResultRow

import scala.language.postfixOps
import scala.util.{Success, Failure, Try}

/** TODO test
  * Model for a relation in an ontology
  * @param id id of the relation
  * @param label for the relation
  */
case class Relation(id: Long, label: String) {
  require(label.matches("^[A-Z][A-Z0-9_]*$"))

  /**
   * To string method for relations
   * @author Thomas GIOVANNINI
   * @return the label of the relation
   */
  override def toString = label

  override def equals(obj: Any) = {
    (obj.isInstanceOf[Relation]
      && obj.asInstanceOf[Relation].id == this.id)
  }

  def isAnAction = label.startsWith("ACTION_")
  def isAMood = label.startsWith("MOOD_")
}

object Relation {
  def apply(label: String) = {
    new Relation(0, label)
  }

  val error = Relation(-1, "ERROR")

  /**
   * Object for connection with the graph database
   */
  object DBGraph {
    implicit val connection = NeoDAO.connection

    /**
     * Method to get a Relation from a Neo4J row
     * @author Thomas GIOVANNINI
     * @param row the Neo4J row
     * @return the appropriate relation
     */
    def parseRow(row: CypherResultRow): Relation = {
      Try {
        val relationID = row[String]("rel_type").substring(2).toLong //row return something like R_123
        RelationDAO.getById(relationID)
      } match {
        case Success(relation) => relation
        case Failure(e) =>
          println("Error while parsing a Relation in Relation.scala:")
          println(e)
          Relation.error
      }
    }

    /**
     * Method to retrieve all existing relations from the database
     * @return the list of all existing relations
     */
    def getAll: List[Relation] = {
      val statement = Statement.getAllRelations
      statement.apply
        .map(parseRow)
        .toList
    }
  }

}
