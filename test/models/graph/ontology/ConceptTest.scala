//package models.graph.ontology
//
//import models.graph.NeoDAO
//import models.graph.custom_types.{DisplayProperty, Coordinates, Statement}
//import models.graph.ontology.concept.{ConceptDAO, Concept}
//import models.graph.ontology.property.Property
//import models.graph.ontology.relation.Relation
//import org.anormcypher.Neo4jREST
//import org.scalatest.FunSuite
//import play.api.libs.json.{JsNumber, JsString, Json}
//
///**
// * Test class for concept class
// */
//class ConceptTest extends FunSuite {
//
//  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")
//
//  val prop1 = Property(0L, "P1", "Int", 0)
//  val prop2 = Property(0L, "P2", "String", "Hello")
//  val prop3 = Property(0L, "P3", "Boolean", false)
//  val rule1 = ValuedProperty(prop1, 5)
//  val rule2 = prop3.defaultValuedProperty
//  val rule3 = prop1.defaultValuedProperty
//  val rule4 = prop2.defaultValuedProperty
//  val concept1 = Concept("C1", List(prop1, prop2), List(rule1, rule2), DisplayProperty())
//  val concept2 = Concept("C2", List(prop1, prop3), List(rule3, rule4), DisplayProperty())
//  val concept3 = Concept("C3", List(prop1), List(), DisplayProperty())
//  val relation1 = Relation("R1")
//  val relation2 = Relation("R2")
//  val relSubtype = Relation("SUBTYPE_OF")
//  val thomas = Instance(1, "Thomas",
//    Coordinates(0, 0),
//    List(ValuedProperty(prop1, "GIOVA"),
//      ValuedProperty(prop2, "Thomas")),
//    concept1)
//  val aurelie = Instance(2, "Aurelie",
//    Coordinates(0, 0),
//    List(ValuedProperty(prop1, "LORGEOUX"),
//      ValuedProperty(prop3, "22")),
//    concept2)
//
//  Statement.clearDB.execute
//
//
//  test("method toJson") {
//    val supposedJsonValue = Json.obj(
//      "label" -> JsString("C1"),
//      "properties" -> Json.arr(
//        prop1.id,
//        prop2.id
//      ),
//      "rules" -> Json.arr(
//        rule1.toJson,
//        rule2.toJson
//      ),
//      "type" -> JsString("CONCEPT"),
//      "id" -> JsNumber(concept1.id),
//      "display" -> concept1.displayProperty.toJson
//    )
//    assert(concept1.toJson == supposedJsonValue)
//  }
//
//  test("method toNodeString") {
//    val desiredString = "(c1 { label: \"C1\", properties: [\"P1%Int%0\",\"P2%String%Hello\"], rules: [" +
//      concept1.rules.map(p => "\"" + p + "\"").mkString(",") + "], color: \"#AAAAAA\", type: \"CONCEPT\", id:" + concept1.id + "})"
//    assert(concept1.toNodeString == desiredString)
//  }
//
//  /*test("method getAllProperties") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
//    val returnedPropertiesSet = Set[Property](prop1, prop2, prop3)
//    assert(concept1.getAllProperties == returnedPropertiesSet)
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//  }*/
//
//  /*test("method getAllRules") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
//    val returnedRulesList = List(rule1, rule2, rule4)
//    assert(concept1.getAllRules == returnedRulesList)
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//  }*/
//
//  /*test("method getParents") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addConceptToDB(concept3)
//    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
//    NeoDAO.addRelationToDB(concept1.id, relation1, concept3.id)
//    assert(concept1.getParents == List(concept2))
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//    NeoDAO.removeConceptFromDB(concept3)
//  }*/
//
//  /*Object*/
//  test("method parseJson") {
//    assert(Concept.parseJson(concept1.toJson) == concept1)
//  }
//
//  test("method parseRow") {
//    NeoDAO.addConceptToDB(concept1)
//    val statement = Statement.getConceptById(concept1.id)
//    val row = statement.apply.head
//    assert(ConceptDAO.parseRow(row) == concept1)
//    NeoDAO.removeConceptFromDB(concept1)
//  }
//
//  test("method addPropertyToConcept") {
//    assert(NeoDAO.addConceptToDB(concept1))
//    assert(!concept1.properties.contains(prop3))
//    val conceptWithProperty = ConceptDAO.addPropertyToConcept(concept1, prop3)
//    assert(conceptWithProperty.properties.contains(prop3))
//    assert(NeoDAO.removeConceptFromDB(concept1))
//  }
//
//  test("method removePropertyFromConcept") {
//    assert(NeoDAO.addConceptToDB(concept1))
//    assert(concept1.properties.contains(prop2))
//    val conceptWithProperty = ConceptDAO.removePropertyFromConcept(concept1, prop2)
//    assert(!conceptWithProperty.properties.contains(prop2))
//    assert(NeoDAO.removeConceptFromDB(concept1))
//  }
//
//  test("method addRuleToConcept") {
//    assert(NeoDAO.addConceptToDB(concept1))
//    assert(!concept1.rules.contains(rule3))
//    val conceptWithProperty = ConceptDAO.addRuleToConcept(concept1, rule3)
//    assert(conceptWithProperty.rules.contains(rule3))
//    assert(NeoDAO.removeConceptFromDB(conceptWithProperty))
//  }
//
//  test("method removeRuleFromConcept") {
//    assert(NeoDAO.addConceptToDB(concept1))
//    assert(concept1.rules.contains(rule2))
//    val conceptWithProperty = ConceptDAO.removeRuleFromConcept(concept1, rule2)
//    assert(!conceptWithProperty.rules.contains(rule2))
//    assert(NeoDAO.removeConceptFromDB(concept1))
//  }
//
//  /* Getters */
//  test("method getById") {
//    assert(NeoDAO.addConceptToDB(concept1))
//    assert(ConceptDAO.getById(concept1.id) == concept1)
//    assert(NeoDAO.removeConceptFromDB(concept1))
//  }
//
//  /*method findAll*/
//  /*test("method getParents by id") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
//    assert(Concept.getParents(concept1.hashCode()).contains(concept2))
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//  }*/
//
//  /*test("method getChildren by id") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
//    assert(Concept.getChildren(concept2.id).contains(concept1))
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//  }*/
//
//  /*test("method getRelationsFrom") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
//    val relationsList = Concept.getRelationsFrom(concept1.id)
//    assert(relationsList.contains((relation1, concept2)))
//    assert(!relationsList.contains((Relation("INSTANCE_OF"), thomas)))
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//  }*/
//
//  /*test("method getRelationsTo") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addConceptToDB(concept3)
//    NeoDAO.addRelationToDB(concept1.id, relation1, concept2.id)
//    NeoDAO.addRelationToDB(concept2.id, relation2, concept3.id)
//    val relationsList = Concept.getRelationsTo(concept2.id)
//    assert(relationsList.contains((relation1, concept1)))
//    assert(!relationsList.contains((relation2, concept3)))
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//    NeoDAO.removeConceptFromDB(concept3)
//  }*/
//
//  /*noInstance*/
//
//  /*test("method getReachableRelations") {
//    NeoDAO.addConceptToDB(concept1)
//    NeoDAO.addConceptToDB(concept2)
//    NeoDAO.addConceptToDB(concept3)
//    NeoDAO.addRelationToDB(concept1.id, relSubtype, concept2.id)
//    NeoDAO.addRelationToDB(concept2.id, relation1, concept3.id)
//    val relationList = Concept.getReachableRelations(concept1.id)
//    assert(relationList.contains((relation1, concept3)))
//    NeoDAO.removeConceptFromDB(concept1)
//    NeoDAO.removeConceptFromDB(concept2)
//    NeoDAO.removeConceptFromDB(concept3)
//  }*/
//
//  /*method getParentsRelations*/
//  /*method notASubtype*/
//
//}
