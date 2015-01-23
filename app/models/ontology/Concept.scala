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

object Concept {

    /**
     * Method to add a relation to a given concept
     * @author Thomas GIOVANNINI
     * @param conceptSrc the source of the relation
     * @param relation the relation
     * @param conceptDest the dest of the relation
     * @return the source concept with the given relation added to it
     */
    def addRelationToConcept(conceptSrc: Concept,
                             relation: Relation,
                             conceptDest: Concept): Concept = {
        Concept(conceptSrc.label,
                (relation, conceptDest) :: conceptSrc.relatedConcepts,
                conceptSrc.properties)
    }

    /**
     * Method to add a property to a given concept
     * @author Thomas GIOVANNINI
     * @param concept to which the property has to be added
     * @param property to be added
     * @return the concept with the given property added
     */
    def addPropertyToConcept(concept: Concept, property: Property): Concept =
        Concept(concept.label, concept.relatedConcepts, property :: concept.properties)

}