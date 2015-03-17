package models.graph.ontology.relation

import anorm.SqlParser._
import anorm.{RowParser, ~}
import models.graph.NeoDAO
import models.graph.custom_types.Statement
import models.graph.ontology.Concept
import models.instance_action.action.InstanceAction
import org.anormcypher.CypherResultRow
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps

/**TODO test
 * Model for a relation in an ontology
 * @param id id of the relation
 * @param label for the relation
 */
case class Relation(id: Long, label: String){
    require(label.matches("^[A-Z][A-Z0-9_]*$"))

    override def toString = label

    override def equals(obj:Any) = {
      (obj.isInstanceOf[Relation]
        && obj.asInstanceOf[Relation].id == this.id)
    }

    def isAnAction = label.startsWith("ACTION_")
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
    
    /**TODO
      * Method to get the Scala function associated to the given relation
      * @param relation key of the Scala function's name in the DB
      * @return the Scala function associated to the given relation
      */
    def getAssociatedRule(relation: Relation): (Concept, Concept) => Unit = ???

    /**
     * Method to get a Relation from a Neo4J row
     * @author Thomas GIOVANNINI
     * @param row the Neo4J row
     * @return the appropriate relation
     */
    def parseRow(row: CypherResultRow): Relation = {
      val relationID = row[String]("rel_type").substring(2).toLong //row return something like R_123
      Relation.DBList.getById(relationID)
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

  /**
   * Object for connection with the relational database
   */
  object DBList {
    implicit val connection = DB.getConnection()

    /**
     * Parse relation to interact with database
     * @author Aurélie LORGEOUX
     */
    private val relationParser: RowParser[Relation] = {
      get[Long]("id") ~
        get[String]("label") map {
        case id ~ label => Relation(id, label)
      }
    }

    /**
     * Clear the database
     * @author Aurélie LORGEOUX
     * @return number of relations deleted
     */
    def clear: Int = {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.clearDB
        statement.executeUpdate
      }
    }

    /**
     * Get all relations saved in database
     * @author Aurélie LORGEOUX
     * @return all relations
     */
    def getAll: List[Relation] = {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.getAll
        statement.as(relationParser *)
      }
    }

    /**
     * Save relation in database
     * @author Aurélie LORGEOUX
     * @param relationName relation to put in the database
     * @return true if the relation saved
     *         false else
     */
    def save(relationName: String): Long = {
      val id = InstanceAction.getByName(relationName).id
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.add(id, relationName)
        val optionId: Option[Long] = statement.executeInsert()
        optionId.getOrElse(-1L)
      }
    }

    /**
     * Get one relation saved in database with its id
     * @author Aurélie LORGEOUX
     * @param id id of the relation
     * @return relation identified by id
     */
    def getById(id: Long): Relation = {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.getById(id)
        statement.as(relationParser.singleOpt).getOrElse(Relation.error)
      }
    }

    /**
     * Get id of action associated to a relation
     * @author Thomas GIOVANNINI
     * @param id id of the relation
     * @return if of action
     */
    def getActionIdFromId(id: Long): Long = {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.getActionId(id)
        val optionResult = statement.apply.map(row => row[Long]("actionId")).headOption
        val result = optionResult.getOrElse(-1L)
        result
      }
    }

    /**
     * Get one relation saved in database with its id
     * @author Aurélie LORGEOUX
     * @param name of the relation
     * @return relation identified by id
     */
    def getByName(name: String): Relation = {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.getByName(name)
        statement.as(relationParser.singleOpt).getOrElse(Relation.error)
      }
    }

    /**
     * Update a relation in database
     * @author Aurélie LORGEOUX
     * @param id id of the relation
     * @param relationName to update
     */
    def update(id: Long, relationName: String): Int = {
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.rename(id, relationName)
        statement.executeUpdate
      }
    }

    /**
     * Delete a relation in database
     * @author Aurélie LORGEOUX
     * @param id id of the relation
     */
    def delete(id: Long): Int = {
      // delete relations in Neo4J graph
      NeoDAO.removeTypeRelationFromDB(id)
      DB.withConnection { implicit connection =>
        val statement = RelationSQLStatement.remove(id)
        statement.executeUpdate
      }
    }
  }
}
