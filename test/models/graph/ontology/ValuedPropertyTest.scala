package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.{Coordinates, Statement}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.Json

/**
 * Test class for the object ValuedProperty
 */
class ValuedPropertyTest extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val prop1 = Property("P1", "Int", 0)
  val prop2 = Property("P2", "String", "thomas")
  val concept1 = Concept("C1", List(prop1, prop2))
  val aurelie = Instance(
    "Aurelie",
    Coordinates(0, 0),
    List(ValuedProperty(prop1, "LORGEOUX"), ValuedProperty(prop2, "22")),
    concept1)

  test("method toJson"){
    val jsonVP = Json.parse("{\"property\":{\"label\":\"P2\",\"valueType\":\"String\",\"defaultValue\":\"thomas\"},\"value\":\"22\"}")
    assert(ValuedProperty(prop2, "22").toJson == jsonVP)
  }

  test("method parseJson"){
    val jsonVP = ValuedProperty(prop2, "22").toJson
    assert(ValuedProperty.parseJson(jsonVP) == ValuedProperty(prop2, "22"))
  }

  ignore("method rowToPropertiesList"){
    NeoDAO.addConceptToDB(concept1)
    NeoDAO.addInstance(aurelie)
    val statement = Statement.getInstances(concept1.id)
    val row = statement.apply.head
    assert(ValuedProperty.rowToPropertiesList(row) ==
      List(ValuedProperty(prop1, "LORGEOUX"), ValuedProperty(prop2, "22")))
    NeoDAO.removeConceptFromDB(concept1)
  }

  test("method parse"){
    val prop = Property("Property", "Int", 0)
    val value = 5
    val stringToParse = prop +"%%5"
    val parsedVP = ValuedProperty.parse(stringToParse)
    assert(parsedVP.property.label == prop.label)
    assert(parsedVP.property.valueType == prop.valueType)
    assert(parsedVP.property.defaultValue == prop.defaultValue)
    assert(parsedVP.value == value)
  }
}
