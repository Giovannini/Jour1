package models.graph.ontology

import models.graph.custom_types.Coordinates
import models.graph.ontology.property.Property
import models.graph.ontology.relation.Relation
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

/**
 * Test class for the model Instance
 */
class InstanceTest extends FunSuite {

  val prop1 = Property(0L, "P1", "Int", 0)
  val prop2 = Property(0L, "P2", "String", "Hello")
  val prop3 = Property(0L, "P3", "Boolean", false)
  val concept1 = Concept("C1", List(prop1, prop2), List())
  val concept2 = Concept("C2", List(prop1, prop3), List())
  val rel1 = Relation("R1")
  val rel2 = Relation("R2")
  val relSubtype = Relation("SUBTYPE_OF")
  val thomas = Instance(1, "Thomas",
    Coordinates(0, 0),
    List(ValuedProperty(prop1, 5),
      ValuedProperty(prop2, "Thomas")),
    concept1)
  val aurelie = Instance(2, "Aurelie",
    Coordinates(0, 0),
    List(ValuedProperty(prop1, "LORGEOUX"),
      ValuedProperty(prop3, "22")),
    concept2)

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  test("method toJson"){
    val jsonInstance: JsValue = Json.obj(
      "id" -> JsNumber(1),
      "label" -> "Thomas",
      "coordinates" -> Json.obj("x" -> JsNumber(0), "y" -> JsNumber(0)),
      "properties" -> Json.arr(
        Json.obj("property" ->
          Json.obj(
            "label" -> JsString("P1"),
            "valueType" -> JsString("Int"),
            "defaultValue" -> JsNumber(0)),
          "value" -> JsNumber(5)),
        Json.obj("property" ->
          Json.obj(
            "label" -> JsString("P2"),
            "valueType" -> JsString("String"),
            "defaultValue" -> JsString("Hello")),
          "value" -> "Thomas")),
      "concept" -> JsNumber(concept1.id))
    assert(thomas.toJson == jsonInstance)
  }

  test("an instance is valid if its properties match its concept properties else it cannot exists"){
    intercept[Exception] {
      Instance(1, "Thomas", Coordinates(0, 0),
        List(ValuedProperty(prop1, 5),
          ValuedProperty(prop3, "22")),
        concept1)
    }
    assert(Instance(1, "Thomas", Coordinates(0, 0),
      List(ValuedProperty(prop1, 5),
        ValuedProperty(prop2, true)),
      concept1).isValid)
  }

  /*test("parseJson should return the correct instance"){
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addConceptToDB(concept2)
    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
    val jsonInstance = thomas.toJson
    assert(Instance.parseJson(jsonInstance) == thomas)
    NeoDAO.removeConceptFromDB(concept1)
    NeoDAO.removeConceptFromDB(concept2)
  }*/

  test("method createRandomInstance must create valid instances"){
    assert(Instance.createRandomInstanceOf(concept1).isValid)
  }

}
