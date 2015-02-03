package models.graph.ontology

import models.graph.custom_types.{Statement, Coordinates}
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Test class for object Relation
 */
class RelationTest extends FunSuite {

  val propName = Property("Name")
  val conceptCat = Concept("Cat", List(propName))
  val relPet = Relation("PET")

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  test("method parseRow"){
    val conceptId = conceptCat.id
    val statement = Statement.getRelationsOf(conceptId)
    val row = statement.apply().head
    assert(Relation.parseRow(row) == relPet)
  }

}
