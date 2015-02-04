package models.graph.ontology

import models.graph.custom_types.{Statement, Coordinates}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.{JsNumber, Json, JsValue}

/**
 * Test class for the model Instance
 */
class InstanceTest extends FunSuite {

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

  test("method toJson"){
    val jsonInstance: JsValue = Json.obj(
      "label" -> "Thomas",
      "coordinates" -> Json.obj("x" -> JsNumber(0), "y" -> JsNumber(0)),
      "properties" -> Json.arr( Json.obj("property" -> "Name", "value" -> "GIOVA"),
                                Json.obj("property" -> "FirstName", "value" -> "Thomas")),
      "concept" -> JsNumber(conceptMan.id))
    assert(thomas.toJson == jsonInstance)
  }

  test("method toNodeString"){
    val nodeStringInstance = "(thomas { label: \"Thomas\", properties: [\"Name -> GIOVA\", \"FirstName -> Thomas\"], coordinate_x: 0, coordinate_y: 0, concept: 1669084034, type: \"INSTANCE\", id: -25837707})"
    assert(thomas.toNodeString == nodeStringInstance)
  }

  test("an instance is valid if its properties match its concept properties else it cannot exists"){
    intercept[Exception] {
      Instance("Thomas", Coordinates(0, 0),
        List(ValuedProperty(propName, "GIOVA"),
          ValuedProperty(propAge, "22")),
        conceptMan)
    }
    Instance("Thomas", Coordinates(0, 0),
      List(ValuedProperty(propName, "GIOVA"),
        ValuedProperty(propFirstName, "Thomas")),
      conceptMan)
  }

  test("parseJson should return teh correct concept"){
    val jsonInstance = thomas.toJson
    assert(Instance.parseJson(jsonInstance = jsonInstance) == thomas)
  }

  test("method parseRowGivenConcept"){
    val conceptId = conceptWoman.id
    val statement = Statement.getInstances(conceptId)
    val row = statement.apply().head
    assert(Instance.parseRowGivenConcept(row, conceptWoman.id) == aurelie)
  }
}
