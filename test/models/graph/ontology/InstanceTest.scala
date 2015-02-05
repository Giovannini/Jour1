package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Statement, Coordinates}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.{JsNumber, Json, JsValue}

/**
 * Test class for the model Instance
 */
class InstanceTest extends FunSuite {

  val prop1 = Property("P1")
  val prop2 = Property("P2")
  val prop3 = Property("P3")
  val concept1 = Concept("C1", List(prop1, prop2))
  val concept2 = Concept("C2", List(prop1, prop3))
  val concept3 = Concept("C3", List(prop1))
  val concept4 = Concept("C4", List(prop1))
  val rel1 = Relation("R1")
  val rel2 = Relation("R2")
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

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  test("method toJson"){
    val jsonInstance: JsValue = Json.obj(
      "label" -> "Thomas",
      "coordinates" -> Json.obj("x" -> JsNumber(0), "y" -> JsNumber(0)),
      "properties" -> Json.arr( Json.obj("property" -> "P1", "value" -> "GIOVA"),
                                Json.obj("property" -> "P2", "value" -> "Thomas")),
      "concept" -> JsNumber(concept1.id))
    assert(thomas.toJson == jsonInstance)
  }

  test("method toNodeString"){
    val nodeStringInstance = "(thomas { label: \"Thomas\", properties: [\"P1 -> GIOVA\", \"P2 -> Thomas\"], coordinate_x: 0, coordinate_y: 0, concept: "+concept1.id+", type: \"INSTANCE\", id: "+thomas.hashCode+"})"
    assert(thomas.toNodeString == nodeStringInstance)
  }

  test("an instance is valid if its properties match its concept properties else it cannot exists"){
    intercept[Exception] {
      Instance("Thomas", Coordinates(0, 0),
        List(ValuedProperty(prop1, "GIOVA"),
          ValuedProperty(prop3, "22")),
        concept1)
    }
    Instance("Thomas", Coordinates(0, 0),
      List(ValuedProperty(prop1, "GIOVA"),
        ValuedProperty(prop2, "Thomas")),
      concept1)
  }

  test("parseJson should return the correct concept if in DB"){
    val jsonInstance = thomas.toJson
    assert(Instance.parseJson(jsonInstance) == Instance("XXX", Coordinates(0,0), List(), Concept("XXX", List())))
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addInstance(thomas)
    assert(Instance.parseJson(jsonInstance) == thomas)
    NeoDAO.removeConceptFromDB(concept1)
  }

  test("method parseRowGivenConcept"){
    NeoDAO.addConceptToDB(concept2)
    NeoDAO.addInstance(aurelie)
    val statement = Statement.getInstances(concept2.id)
    val row = statement.apply().head
    assert(Instance.parseRowGivenConcept(row, concept2.id) == aurelie)
    NeoDAO.removeConceptFromDB(concept2)
  }
}
