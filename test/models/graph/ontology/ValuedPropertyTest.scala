package models.graph.ontology

import models.graph.custom_types.{Coordinates, Statement}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.Json

/**
 * Test class for the object ValuedProperty
 */
class ValuedPropertyTest extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val propName = Property("Name")
  val propAge = Property("Age")
  val conceptWoman = Concept("Woman", List(propName, propAge))
  val aurelie = Instance(
    "Aurelie",
    Coordinates(0, 0),
    List(ValuedProperty(propName, "LORGEOUX"), ValuedProperty(propAge, "22")),
    conceptWoman)

  test("method toJson"){
    val jsonVP = Json.obj("property" -> propAge.label, "value" -> "22")
    assert(ValuedProperty(propAge, "22").toJson == jsonVP)
  }

  test("method parseJson"){
    val jsonVP = ValuedProperty(propAge, "22").toJson
    assert(ValuedProperty.parseJson(jsonVP) == ValuedProperty(propAge, "22"))
  }

  test("method rowToPropertiesList"){
    val statement = Statement.getInstances(conceptWoman.id)
    val row = statement.apply().head
    assert(ValuedProperty.rowToPropertiesList(row) ==
      List(ValuedProperty(propName, "LORGEOUX"), ValuedProperty(propAge, "22")))
  }

  test("method parse"){
    val prop = Property("Property")
    val value = "value"
    val stringToParse = "Property -> value"
    assert(ValuedProperty.parse(stringToParse) == ValuedProperty(prop, value))
  }
}
