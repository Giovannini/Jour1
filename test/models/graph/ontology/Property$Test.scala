package models.graph.ontology

import models.graph.custom_types.Statement
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite
import play.api.libs.json.JsString

/**
 * Class test for object Property
 */
class Property$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val propName = Property("Name")
  val propAge = Property("Age")
  val conceptWoman = Concept("Woman", List(propName, propAge))

  test("method toJson"){
    val json = JsString("Name")
    assert(json == propName.toJson)
  }

  test("method parseJson"){
    val json = JsString("Name")
    assert(Property.parseJson(json) == propName)
  }

  test("method rowToPropertiesList"){
    val statement = Statement.getConceptById(conceptWoman.id)
    val row = statement.apply().head
    assert(Property.rowToPropertiesList(row) == List(propName, propAge))
  }
}
