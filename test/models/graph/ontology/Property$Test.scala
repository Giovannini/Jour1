package models.graph.ontology

import models.graph.ontology.concept.Concept
import models.graph.ontology.property.Property
import org.anormcypher.Neo4jREST
import org.scalatest.FunSuite

/**
 * Class test for object Property
 */
class Property$Test extends FunSuite {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")

  val prop1 = Property(0L, "P1", "Int", 0)
  val prop2 = Property(0L, "P2", "String", "Hello")
  val concept1 = Concept("Woman", List(prop1, prop2), List())

}
