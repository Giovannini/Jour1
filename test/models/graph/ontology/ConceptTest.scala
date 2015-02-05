package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Coordinates, Statement}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.{JsNumber, JsString, Json}

/**
 * Test class for concept class
 */
class ConceptTest extends FunSuite {

    implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

    val prop1 = Property("P1")
    val prop2 = Property("P2")
    val prop3 = Property("P3")
    val concept1 = Concept("C1", List(prop1, prop2))
    val concept2 = Concept("C2", List(prop1, prop3))
    val concept3 = Concept("C3", List(prop1))
    val relation1 = Relation("R1")
    val relation2 = Relation("R2")
    val relSubtype = Relation("SUBTYPE_OF")
    val thomas = Instance("Thomas",
        Coordinates(0, 0),
        List(ValuedProperty(prop1, "GIOVA"),
            ValuedProperty(prop2, "Thomas")),
        concept1)
    val aurelie = Instance("Aurelie",
        Coordinates(0, 0),
        List(ValuedProperty(prop1, "LORGEOUX"),
            ValuedProperty(prop3, "22")),
        concept2)


    test("toJson should give an appropriate Json value"){
        val supposedJsonValue = Json.obj(
            "label" -> JsString("C1"),
            "properties" -> Json.arr(
                JsString("P1"),
                JsString("P2")
            ),
            "type" -> JsString("CONCEPT"),
            "id" -> JsNumber(concept1.id)
        )
        assert(concept1.toJson == supposedJsonValue)
    }

    test("toNodeString should return the desired string"){
        val desiredString = "(c1 { label: \"C1\", properties: [\"P1\",\"P2\"], type: \"CONCEPT\", id:"+concept1.id+"})"
        assert(concept1.toNodeString == desiredString)
    }

    test("a cypher row containing a concept should be correctly parsed"){
        NeoDAO.addConceptToDB(concept1)
        val statement = Statement.getConceptById(concept1.id)
        val row = statement.apply.head
        assert(Concept.parseRow(row) == concept1)
        NeoDAO.removeConceptFromDB(concept1)
    }

    /*add to DB => DONE*/
    test("the addPropertyToConcept method should add a property to the given concept"){
        NeoDAO.addConceptToDB(concept1)
        val conceptWithProperty = Concept.addPropertyToConcept(concept1, prop3)
        assert(conceptWithProperty.properties.length == 3)
        assert(conceptWithProperty.properties.contains(prop3))
        NeoDAO.removeConceptFromDB(concept1)
    }

    /* Getters */

    test("it is possible to get a concept from the DB by its ID"){
        NeoDAO.addConceptToDB(concept1)
        assert(Concept.getById(concept1.id).get == concept1)
        NeoDAO.removeConceptFromDB(concept1)
    }

    test("method getRelations should return all the relations linked to the given concept") {
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
        val relationList = Concept.getRelations(concept1.id)
        assert(relationList.contains((relation1, concept2)))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
    }

    test("method getParentsConceptOf should return parent concepts of the given one"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
        assert(Concept.getParents(concept1.hashCode()).contains((relSubtype,concept2)))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
    }

    test("method getPossibleActions should give all the possible actions for a given concept"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addConceptToDB(concept3)
        NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
        NeoDAO.addRelationToDB(concept2.id, relation1, concept3.id)
        val relationList = Concept.getPossibleActions(concept1.id)
        assert(relationList.contains((relation1, concept3)))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
        NeoDAO.removeConceptFromDB(concept3)
    }

    test("method getRelation should give all the non instance relations of a concept"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
        NeoDAO.addInstance(thomas)
        val relations = Concept.getRelations(concept1.id)
        assert(relations.contains((relation1, concept2)))
        assert(! relations.contains((Relation("INSTANCE_OF"), thomas)))
        NeoDAO.removeInstance(thomas)
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
    }

    test("method getInstancesOfSelf"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
        NeoDAO.addInstance(thomas)
        NeoDAO.addInstance(aurelie)
        val instancesOf1 = Concept.getInstanceOfSelf(concept1.id)
        val instancesOf2 = Concept.getInstanceOfSelf(concept2.id)
        assert(instancesOf1.contains(thomas))
        assert(! instancesOf2.contains(thomas))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
    }

    test("method getInstancesOf should return the instances of a given concept and also its children"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addConceptToDB(concept3)
        NeoDAO.addRelationToDB(concept1.id, relSubtype, concept3.id)
        NeoDAO.addRelationToDB(concept2.id, relSubtype, concept3.id)
        NeoDAO.addInstance(thomas)
        NeoDAO.addInstance(aurelie)
        val instancesOfFather = Concept.getInstancesOf(concept3.id)
        assert(instancesOfFather.contains(thomas))
        assert(instancesOfFather.contains(aurelie))
        val instancesOfSon = Concept.getInstancesOf(concept1.id)
        assert(instancesOfSon.contains(thomas))
        assert(! instancesOfSon.contains(aurelie))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
        NeoDAO.removeConceptFromDB(concept3)
    }

}
