package models.ontology

import models.custom_types.Label
import org.scalatest.FunSuite

/**
 * Test class for concept class
 */
class ConceptTest extends FunSuite {

    test("the addPropertyToConcept method should add a property to the given concept"){
        val conceptWithoutProperty = Concept(Label("C1"), List(), List())
        assert(conceptWithoutProperty.properties.isEmpty)
        val property = IntProperty(Label("Age"), 21)
        val conceptWithProperty = Concept.addPropertyToConcept(conceptWithoutProperty, property)
        assert(conceptWithProperty.properties.nonEmpty)
        assert(conceptWithProperty.properties.head == property)
    }

    test("the addRelationToConcept method should add a relation to a given concept"){
        val conceptWithoutRelation1 = Concept(Label("C1"), List(), List())
        val conceptWithoutRelation2 = Concept(Label("C2"), List(), List())
        assert(conceptWithoutRelation1.relatedConcepts.isEmpty)
        val relation = Relation(Label("R1"))
        val conceptWithRelation = Concept
          .addRelationToConcept(conceptWithoutRelation1, relation, conceptWithoutRelation2)
        assert(conceptWithRelation.relatedConcepts.nonEmpty)
        assert(conceptWithRelation.relatedConcepts.head == (relation, conceptWithoutRelation2))
    }

    test("the getConceptsLinkedByRelation method should return a list of concept linked to an " +
      "other by the given relation"){
        val conceptWithoutRelation1 = Concept(Label("C1"), List(), List())
        val conceptWithoutRelation2 = Concept(Label("C2"), List(), List())
        val relation = Relation(Label("R1"))
        val conceptWithRelation = Concept
          .addRelationToConcept(conceptWithoutRelation1, relation, conceptWithoutRelation2)
        assert(conceptWithRelation.getConceptsLinkedByRelation(relation) ==
              List(conceptWithoutRelation2))
    }

}
