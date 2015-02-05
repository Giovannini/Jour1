package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Coordinates}
import org.anormcypher.{CypherResultRow, Neo4jREST}
import org.scalatest.FunSuite
import play.api.libs.json.{JsNumber, JsString, Json}

/**
 * Test class for concept class
 */
class ConceptTest extends FunSuite {

        val propName = Property("Name")
        val propFirstName = Property("FirstName")
        val propAge = Property("Age")
        val conceptMan = Concept("Man", List(propName, propFirstName))
        val conceptWoman = Concept("Woman", List(propName, propAge))
        val conceptHuman = Concept("Human", List(propName))
        val conceptCat = Concept("Cat", List(propName))
        val relPet = Relation("PET")
        val relLike = Relation("LIKE")
        val relSubtype = Relation("SUBTYPE_OF")
        val thomas = Instance("Thomas",
            Coordinates(0, 0),
            List(ValuedProperty(propName, "GIOVA"),
                ValuedProperty(propFirstName, "Thomas")),
            conceptMan)
        val julien = Instance("Julien",
            Coordinates(0, 0),
            List(ValuedProperty(propName, "PRADET"),
                ValuedProperty(propFirstName, "Julien")),
            conceptMan)
        val simon = Instance("Simon",
            Coordinates(0, 0),
            List(ValuedProperty(propName, "RONCIERE"),
                ValuedProperty(propFirstName, "Simon")),
            conceptMan)
        val aurelie = Instance("Aurelie",
            Coordinates(0, 0),
            List(ValuedProperty(propName, "LORGEOUX"),
                ValuedProperty(propAge, "22")),
            conceptWoman)

    implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

    test("toJson should give an appropriate Json value"){
        val supposedJsonValue = Json.obj(
            "label" -> JsString("Man"),
            "properties" -> Json.arr(
                JsString("Name"),
                JsString("FirstName")
            ),
            "type" -> JsString("CONCEPT"),
            "id" -> JsNumber(conceptMan.hashCode())
        )
        assert(conceptMan.toJson == supposedJsonValue)
    }

    test("toNodeString should return the desired string"){
        val desiredString = "(man { label: \"Man\", properties: [\"Name\",\"FirstName\"], type: \"CONCEPT\", id:1669084034})"
        assert(conceptMan.toNodeString == desiredString)
    }

    test("a cypher row containing a concept should be correctly parsed"){
        val conceptId = conceptMan.hashCode()
        val statement = Statement.getConceptById(conceptId)
        val row = statement.apply().head
        assert(Concept.parseRow(row) == conceptMan)
    }

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

    /* Getters */

    test("it is possible to get a concept from the DB by its ID"){
        assert(Concept.getById(conceptMan.hashCode()) == conceptMan)
    }

    test("method getRelations should return all the relations linked to the given concept") {
        val relationList = Concept.getRelations(conceptMan.hashCode())
        assert(relationList.contains((relLike, conceptWoman)))
        assert(relationList.contains((relSubtype, conceptHuman)))
        assert(!relationList.contains((relLike, conceptHuman)))
    }

    test("method getParentsConceptOf should return parent concepts of the given one"){
        assert(Concept.getParents(conceptMan.hashCode()).contains((relSubtype,conceptHuman)))
    }

    test("method getPossibleActions should give all the possible actions for a given concept"){
        NeoDAO.addConceptToDB(conceptCat)
        NeoDAO.addRelationToDB(conceptHuman.hashCode(), relPet, conceptCat.hashCode())
        val relationList = Concept.getPossibleActions(conceptMan.hashCode())
        assert(relationList.contains((relPet, conceptCat)))
    }

    test("method getRelation should give all the non instance relations of a concept"){
        val relations = Concept.getRelations(conceptMan.hashCode())
        assert(relations.contains((relLike, conceptWoman)))
        assert(relations.contains((relSubtype, conceptHuman)))
        assert(! relations.contains((relPet, conceptWoman)))
        assert(! relations.contains((Relation("INSTANCE_OF"), thomas)))
    }

    test("method getInstancesOfSelf"){
        val instancesOfMan = Concept.getInstanceOfSelf(conceptMan.id)
        val instancesOfHuman = Concept.getInstanceOfSelf(conceptHuman.id)
        assert(instancesOfMan.contains(thomas))
        assert(! instancesOfHuman.contains(thomas))
    }

    test("method getInstancesOf should return the instances of a given concept and also its children"){
        println("gogogo")
        val instancesOfHuman = Concept.getInstancesOf(conceptHuman.id)
        assert(instancesOfHuman.contains(thomas))
        assert(instancesOfHuman.contains(aurelie))

        val instancesOfMan = Concept.getInstancesOf(conceptMan.id)
        assert(instancesOfMan.contains(simon))
        assert(! instancesOfMan.contains(aurelie))
    }

}
