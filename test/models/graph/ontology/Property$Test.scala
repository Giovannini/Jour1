package models.graph.ontology

import models.graph.custom_types.Statement
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Class test for object Property
 */
class Property$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val propName = Property("Name")
  val propAge = Property("Age")
  val conceptWoman = Concept("Woman", List(propName, propAge))

  test("method rowToPropertiesList"){
    val statement = Statement.getConceptById(conceptWoman.id)
    val row = statement.apply().head
    assert(Property.rowToPropertiesList(row) == List(propName, propAge))
  }
}
