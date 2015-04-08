//package models.graph.ontology
//
//import models.graph.NeoDAO
//import models.graph.custom_types.{Coordinates, Statement}
//import models.graph.ontology.concept.Concept
//import models.graph.ontology.property.Property
//import org.anormcypher.Neo4jREST
//import org.scalatest.FunSuite
//import play.api.libs.json.Json
//
///**
// * Test class for the object ValuedProperty
// */
//class ValuedPropertyTest extends FunSuite {
//
//  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/")
//
//  val prop1 = Property(0L, "P1", "Int", 0)
//  val prop2 = Property(0L, "P2", "String", "thomas")
//  val prop3 = Property(0L, "P3", "Boolean", false)
//  val concept1 = Concept("C1", List(prop1, prop2), List(prop3.defaultValuedProperty))
//  val aurelie = Instance(0,
//    "Aurelie",
//    Coordinates(0, 0),
//    List(ValuedProperty(prop1, "LORGEOUX"), ValuedProperty(prop2, "22")),
//    concept1)
//
//  test("method toJson"){
//    val jsonVP = Json.parse("{\"property\":{\"label\":\"P2\",\"valueType\":\"String\",\"defaultValue\":\"thomas\"},\"value\":\"22\"}")
//    assert(ValuedProperty(prop2, "22").toJson == jsonVP)
//  }
//
//  test("method parseJson"){
//    val jsonVP = ValuedProperty(prop2, "22").toJson
//    assert(ValuedProperty.parseJson(jsonVP) == ValuedProperty(prop2, "22"))
//  }
//
//  test("method rowToPropertiesList"){
//    NeoDAO.addConceptToDB(concept1)
//    val statement = Statement.getConceptById(concept1.id)
//    val row = statement.apply.head
//    assert(ValuedProperty.rowToPropertiesList(row, "concept_rules") ==
//      List(prop3.defaultValuedProperty))
//    NeoDAO.removeConceptFromDB(concept1)
//  }
//
//  test("method parse"){
//    val prop = Property(0L, "Property", "Int", 0)
//    val value = 5
//    val stringToParse = prop +"%%5"
//    val parsedVP = ValuedProperty.parse(stringToParse)
//    assert(parsedVP.property.label == prop.label)
//    assert(parsedVP.property.valueType == prop.valueType)
//    assert(parsedVP.property.defaultValue == prop.defaultValue)
//    assert(parsedVP.value == value)
//  }
//
//  test("method keepHighestLevelRules"){
//    val vpList = List(
//      ValuedProperty(prop1, 5),
//      ValuedProperty(prop2, "Yo"),
//      ValuedProperty(prop3, true),
//      ValuedProperty(prop1, 11),
//      ValuedProperty(prop1, 17),
//      ValuedProperty(prop2, "Man")
//    )
//  }
//}
