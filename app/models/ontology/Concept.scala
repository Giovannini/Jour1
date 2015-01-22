package models.ontology

import models.custom_types.Label

/**
 * Model for a concept of an ontology
 * @param label for the concept
 * @param relatedConcepts to this concept
 */
case class Concept(label:           Label,
                   relatedConcepts: List[(Relation, Concept)],
                   properties:      List[Property]){

    /**
     * Get the concepts linked, in the ontology, to this one by the given relation
     * @param relation to follow to get concepts
     * @return a list of the concepts linked to this one following the given relation
     */
    def getConceptsLinkedByRelation(relation: Relation): List[Concept] ={
        relatedConcepts
          .filter(relation_concept => relation_concept._1 == relation)
          .map(relation_concept => relation_concept._2) //Only return the concepts, not the relation
    }

    /**
     * Get the concepts linked, in the ontology, to this one by the given relation
     * @param relationName to follow to get concepts
     * @return a list of the concepts linked to this one following the given relation
     */
    def getConceptsLinkedByRelation(relationName: String): List[Concept] =
        getConceptsLinkedByRelation(Relation(Label(relationName)))

}