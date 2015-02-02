package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Label}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Test class for concept class
 */
class ConceptTest extends FunSuite {

    implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

    test("the addPropertyToConcept method should add a property to the given concept"){
        val conceptWithoutProperty = Concept("C1", List())
        assert(conceptWithoutProperty.properties.isEmpty)
        val property = Property("Age")
        val conceptWithProperty = Concept.addPropertyToConcept(conceptWithoutProperty, property)
        assert(conceptWithProperty.properties.nonEmpty)
        assert(conceptWithProperty.properties.head == property)
    }

    test("it is possible to get a concept from the graph via it's id"){
        val propName = Property("Name")
        val propFirstName = Property("FirstName")
        val conceptMan = Concept("Man", List(propName, propFirstName))
        NeoDAO.addConceptToDB(conceptMan)
        assert(Concept.getById(conceptMan.hashCode()) == conceptMan)
    }

    //Statement.clearDB.execute()

}
