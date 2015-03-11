package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.Statement
import models.graph.ontology.property.Property
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.{JsValue, JsNumber, Json, JsString}

/**
 * Class test for object Property
 */
class Property$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val prop1 = Property("P1", "Int", 0)
  val prop2 = Property("P2", "String", "Hello")
  val concept1 = Concept("Woman", List(prop1, prop2), List())

  test("method toJson"){
    val json = Json.parse("{\"label\":\"P1\",\"valueType\":\"Int\",\"defaultValue\":0}")
    assert(json == prop1.toJson)
  }

  test("method parseJson"){
    val json: JsValue = Json.obj(
      "label" -> JsString("P1"),
      "valueType" -> JsString("Int"),
      "defaultValue" -> JsNumber(0)
    )
    assert(Property.parseJson(json) == prop1)
  }

  test("method rowToPropertiesList"){
    NeoDAO.addConceptToDB(concept1)
    val statement = Statement.getConceptById(concept1.id)
    val row = statement.apply.head
    val propList = Property.rowToPropertiesList(row)
    assert(propList.head.defaultValue == concept1.properties.head.defaultValue)
    assert(propList.tail.head.defaultValue == concept1.properties.tail.head.defaultValue)
    NeoDAO.removeConceptFromDB(concept1)
  }

}
