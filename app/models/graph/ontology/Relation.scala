package models.graph.ontology

import anorm.{~, RowParser}
import anorm.SqlParser._
import models.graph.custom_types.{RelationStatement, Statement}
import org.anormcypher.{Neo4jREST, CypherResultRow}
import play.Play
import play.api.db.DB
import play.api.Play.current

/**TODO test
 * Model for a relation in an ontology
 * @param id id of the relation
 * @param label for the relation
 * @param src concept source
 * @param dest concept destination
 */
case class Relation(id: Long, label: String, src: Long, dest: Long){
    require(label.matches("^[A-Z][A-Z0-9_]*$"))

    override def toString = label

    def isAnAction = label.startsWith("ACTION_")
}

object Relation {
  def apply(id: Long) = Relation.DBList.getById(id)

  /* apply temporaire juste pour pas tout faire planter de suite */
  def apply(label: String) = {
    new Relation(0, label, 0, 0)
  }

  val error = Relation(-1, "ERROR", -1, -1)

  /**
   * Object for connection with the graph database
   */
  object DBGraph {
    implicit val connection = Neo4jREST(Play.application.configuration.getString("serverIP"), 7474, "/db/data/")

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
      val label = row[String]("rel_type")
      Relation(label)
    }

    /**
     * Method to retrieve all existing relations from the database
     * @return the list of all existing relations
     */
    def getAll: List[Relation] = {
      val statement = Statement.getAllRelations
      statement.apply
        .map(row => row[String]("relation_label"))
        .map(Relation(_))
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
        get[String]("label") ~
        get[Long]("src") ~
        get[Long]("dest") map {
        case id ~ label ~ src ~ dest => Relation(id, label, src, dest)
      }
    }

    /**
     * Clear the database
     * @author Aurélie LORGEOUX
     * @return number of relations deleted
     */
    def clear: Int = {
      DB.withConnection { implicit connection =>
        val statement = RelationStatement.clearDB
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
        val statement = RelationStatement.getAll
        statement.as(relationParser *)
      }
    }

    /**
     * Save relation in database
     * @author Aurélie LORGEOUX
     * @param relation relation to put in the database
     * @return true if the relation saved
     *         false else
     */
    def save(relation: Relation): Long = {
      DB.withConnection { implicit connection =>
        val statement = RelationStatement.add(relation)
        val optionId: Option[Long] = statement.executeInsert()
        /*val id = */optionId.getOrElse(-1L)
        //if (id == -1L) Action.error else action.withId(id)
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
        val statement = RelationStatement.get(id)
        statement.as(relationParser.singleOpt).getOrElse(Relation.error)
      }
    }

    /**
     * Update a relation in database
     * @author Aurélie LORGEOUX
     * @param id id of the relation
     * @param relation relation identified by id
     */
    def update(id: Long, relation: Relation): Int = {
      DB.withConnection { implicit connection =>
        val statement = RelationStatement.set(id, relation)
        statement.executeUpdate
      }
    }

    /**
     * Delete a relation in database
     * @author Aurélie LORGEOUX
     * @param id id of the relation
     */
    def delete(id: Long): Int = {
      DB.withConnection { implicit connection =>
        val statement = RelationStatement.remove(id)
        statement.executeUpdate
      }
    }
  }
}
