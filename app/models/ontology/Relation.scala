package models.ontology

import models.custom_types.Label

/**TODO test
 * Model for a relation in an ontology
 * @param label for the relation
 */
case class Relation(label: Label)

object Relation {

    /**TODO
     * Method to get the Scala function associated to the given relation
     * @param relation key of the Scala function's name in the DB
     * @return the Scala function associated to the given relation
     */
    def getAssociatedRule(relation: Relation): (Concept, Concept) => Unit = ???

}
