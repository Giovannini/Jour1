package models.graph.ontology

import models.graph.custom_types.Label
import org.scalatest.FunSuite

/**
 * Test class for concept class
 */
class ConceptTest extends FunSuite {

    test("the addPropertyToConcept method should add a property to the given concept"){
        val conceptWithoutProperty = Concept(Label("C1"), List())
        assert(conceptWithoutProperty.properties.isEmpty)
        val property = Property("Age")
        val conceptWithProperty = Concept.addPropertyToConcept(conceptWithoutProperty, property)
        assert(conceptWithProperty.properties.nonEmpty)
        assert(conceptWithProperty.properties.head == property)
    }

}
