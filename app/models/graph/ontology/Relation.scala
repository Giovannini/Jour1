package models.graph.ontology

import models.graph.custom_types.Statement
import org.anormcypher.{Neo4jREST, CypherResultRow}
import play.Play

/**TODO test
 * Model for a relation in an ontology
 * @param label for the relation
 */
case class Relation(label: String, src: Long, dest: Long){
    require(label.matches("^[A-Z][A-Z0-9_]*$"))

    override def toString = label

    def isAnAction = label.startsWith("ACTION_")
}

object Relation {

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
