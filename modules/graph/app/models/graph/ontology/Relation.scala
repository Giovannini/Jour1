package models.graph.ontology

import models.graph.custom_types.Label
import org.anormcypher.CypherResultRow

/**TODO test
 * Model for a relation in an ontology
 * @param label for the relation
 */
case class Relation(label: String){
    require(label.matches("^[A-Z][A-Z0-9_]*$"))

    override def toString = label

}

object Relation {

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

}
