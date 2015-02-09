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

    val prop1 = Property("P1", "Int", 0)
    val prop2 = Property("P2", "String", "Hello")
    val prop3 = Property("P3", "Boolean", false)
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

    Statement.clearDB.execute


    test("method toJson"){
        val supposedJsonValue = Json.obj(
            "label" -> JsString("C1"),
            "properties" -> Json.arr(
                prop1.toJson,
                prop2.toJson
            ),
            "type" -> JsString("CONCEPT"),
            "id" -> JsNumber(concept1.id),
            "color" -> JsString(concept1.color)
        )
        assert(concept1.toJson == supposedJsonValue)
    }

    test("method toNodeString"){
        val desiredString = "(c1 { label: \"C1\", properties: [\"P1%Int%0\",\"P2%String%Hello\"], color: \"#AAAAAA\", type: \"CONCEPT\", id:"+concept1.id+"})"
        assert(concept1.toNodeString == desiredString)
    }

    /*Object*/
    test("method parseJson"){
        assert(Concept.parseJson(concept1.toJson) == concept1)
    }

    test("method parseRow"){
        NeoDAO.addConceptToDB(concept1)
        val statement = Statement.getConceptById(concept1.id)
        val row = statement.apply.head
        assert(Concept.parseRow(row) == concept1)
        NeoDAO.removeConceptFromDB(concept1)
    }

    ignore("method addPropertyToConcept"){
        assert(NeoDAO.addConceptToDB(concept1))
        assert(NeoDAO.addInstance(thomas))
        assert(! thomas.properties.contains(prop3))
        assert(! concept1.properties.contains(prop3))
        val conceptWithProperty = Concept.addPropertyToConcept(concept1, prop3, 17)
        //Problem: The instances are not correctly updated
        val updatedInstanceList = Concept.getInstancesOf(concept1.id)
        assert(conceptWithProperty.properties.contains(prop3))
        assert(updatedInstanceList.forall(_.properties.map(_.property).contains(prop3)))
        assert(NeoDAO.removeConceptFromDB(concept1))
    }

    /* Getters */
    test("method getById"){
        assert(NeoDAO.addConceptToDB(concept1))
        assert(Concept.getById(concept1.id).get == concept1)
        assert(NeoDAO.removeConceptFromDB(concept1))
    }

    /*method findAll*/
    test("method getParents"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
        assert(Concept.getParents(concept1.hashCode()).contains(concept2))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
    }
    /*method getChildren*/

    test("method getRelations") {
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
        NeoDAO.addInstance(thomas)
        val relationsList = Concept.getRelations(concept1.id)
        assert(relationsList.contains((relation1, concept2)))
        assert(! relationsList.contains((Relation("INSTANCE_OF"), thomas)))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
    }

    /*noInstance*/

    test("method getReachableRelations"){
        NeoDAO.addConceptToDB(concept1)
        NeoDAO.addConceptToDB(concept2)
        NeoDAO.addConceptToDB(concept3)
        NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
        NeoDAO.addRelationToDB(concept2.id, relation1, concept3.id)
        val relationList = Concept.getReachableRelations(concept1.id)
        assert(relationList.contains((relation1, concept3)))
        NeoDAO.removeConceptFromDB(concept1)
        NeoDAO.removeConceptFromDB(concept2)
        NeoDAO.removeConceptFromDB(concept3)
    }

    /*method getParentsRelations*/
    /*method notASubtype*/

    ignore("method getInstancesOf should return the instances of a given concept and also its children"){
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

    ignore("method getInstancesOfSelf"){
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

}
