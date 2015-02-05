package models.graph.ontology

import models.graph.NeoDAO
import models.graph.custom_types.Statement
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.JsString

/**
 * Class test for object Property
 */
class Property$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val prop1 = Property("Name")
  val prop2 = Property("Age")
  val concept1 = Concept("Woman", List(prop1, prop2))

  test("method toJson"){
    val json = JsString("Name")
    assert(json == prop1.toJson)
  }

  test("method parseJson"){
    val json = JsString("Name")
    assert(Property.parseJson(json) == prop1)
  }

  test("method rowToPropertiesList"){
    NeoDAO.addConceptToDB(concept1)
    val statement = Statement.getConceptById(concept1.id)
    val row = statement.apply.head
    assert(Property.rowToPropertiesList(row) == List(prop1, prop2))
    NeoDAO.removeConceptFromDB(concept1)
  }

}
