package models.graph.ontology

import org.anormcypher.CypherResultRow
import play.api.libs.json._

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
case class Property(label: String, valueType: String, defaultValue: Any) {
  require(label.matches("^[A-Z][A-Za-z0-9]*$"))
  override def toString = label+"%"+valueType+"%"+defaultValue

  def toJson : JsValue = Json.obj(
    "label" -> JsString(label),
    "valueType" -> JsString(valueType),
    "defaultValue" -> jsonDefaultValue)

  private val jsonDefaultValue: JsValue = valueType match {
    case "Int" => JsNumber(defaultValue.toString.toInt)
    case "Double" => JsNumber(defaultValue.toString.toDouble)
    case "String" => JsString(defaultValue.toString)
    case "Boolean" => JsBoolean(defaultValue.toString.toBoolean)
    //case "List" => (jsonProperty \ "defaultValue").as[String]/**TODO deal with lists correctly*/
    case _ => JsString(defaultValue.toString)
  }

  def defaultValuedProperty: ValuedProperty = {
    ValuedProperty(this, defaultValue)
  }
}

object Property {

  /**
   * Transform a json representing a property into the Property it represents
   * @author Thomas GIOVANNINI
   * @param jsonProperty the property in json format to parse
   * @return the represented property
   */
  def parseJson(jsonProperty: JsValue): Property = {
    val label = (jsonProperty \ "label").as[String]
    val valueType = (jsonProperty \ "valueType").as[String]
    val defaultValue = valueType match{
      case "Int" => (jsonProperty \ "defaultValue").as[Int]
      case "Double" => (jsonProperty \ "defaultValue").as[Double]
      case "String" => (jsonProperty \ "defaultValue").as[String]
      case "Boolean" => (jsonProperty \ "defaultValue").as[Boolean]
      //case "List" => (jsonProperty \ "defaultValue").as[String]/**TODO deal with lists correctly*/
      case _ => (jsonProperty \ "defaultValue").as[String]
    }
    Property(label, valueType, defaultValue)
  }

  def parseString(stringProperty: String): Property = {
    val propAttributes = stringProperty.split("%")
    val valueType = propAttributes(1)
    val defaultValue = valueType match{
      case "Int" => propAttributes(2).toInt
      case "Double" => propAttributes(2).toDouble
      case "String" => propAttributes(2)
      case "Boolean" => propAttributes(2).toBoolean
      //case "List" => (jsonProperty \ "defaultValue").as[String]/**TODO deal with lists correctly*/
      case _ => propAttributes(2).toString
    }
    Property(propAttributes(0), valueType, defaultValue)
  }

  /**
   * Read a Neo4J row from the DB and convert it to a concept object
   * @author Thomas GIOVANNINI
   * @param row the row read from the db
   *            it should contains a string name label
   *            and a sequence of strings name properties
   * @return the concept translated from the given row
   */
  def rowToPropertiesList(row: CypherResultRow): List[Property] = {
    row[Seq[String]]("concept_prop") // get the properties sequence from the row
      .toList
      .map { property =>
        val p = property.split("%")
        p(1) match {
            case "Int" => Property(p(0), p(1), p(2).toInt)
            case "Double" => Property(p(0), p(1), p(2).toDouble)
            case "String" => Property(p(0), p(1), p(2))
            case "Boolean" => Property(p(0), p(1), p(2).toBoolean)
            //case "List" => (jsonProperty \ "defaultValue").as[String]/**TODO deal with lists correctly*/
            case _ => Property(p(0), p(1), p(2))
          }
        }
  }
}