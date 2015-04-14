package models.graph.property

import play.api.libs.json._

/**
 * Model class to represent a valued property
 * @param property the property which is valued
 * @param value the value for the property
 */
case class ValuedProperty(property: Property, value: Double){
  /**
   * Parse a ValuedProperty to Json
   * @author Thomas GIOVANNINI
   * @return the parsed valued property to json format
   */
  def toJson : JsValue = Json.obj(
    "property" -> property.toJson,
    "value" -> JsNumber(value)
  )

  /**
   * The toString method
   * @author Thomas GIOVANNINI
   * @return the string value for a ValuedProperty
   */
  override def toString = property + " -> " + value
}

object ValuedProperty {

  val error = ValuedProperty(Property.error, 0L)

  /**
   * Method to parse a json to a ValuedProperty
   * @author Thomas GIOVANNINI
   * @param jsonVP the json value to parse
   * @return a Valued property
   */
  def parseJson(jsonVP: JsValue): ValuedProperty = {
    val property = Property.parseJson(jsonVP \ "property")
    val value = (jsonVP \ "value").as[Double]
    ValuedProperty(property, value)
  }

  /**
   * Method to parse a string ("property%value") to a ValuedProperty
   * @author Thomas GIOVANNINI
   * @param string to parse
   * @return the proper ValuedProperty
   */
  def parse(string: String): ValuedProperty = {
    val splitted = string.split(" -> ")
    val property = Property.parseString(splitted(0))
    val value = splitted(1).toDouble
    ValuedProperty(PropertyDAO.getByName(property.label), value)
  }

  /**
   * Builds a new list from this one without any duplicate property elements.
   * @author Thomas GIOVANNINI
   * @param rulesList the list to reduce
   * @return A new list which contains the first occurrence of every property of this list. 
   */
  def distinctProperties(rulesList: List[ValuedProperty])= {
    /*
     * Remove from a long list of rules the recurrent ones, keeping the first coming.
     * @author Thomas GIOVANNINI
     * @param rulesList the list to reduce
     * @param matchedProperties the already matched properties that shouldn't be added
     * @return a shorter rules list
     */
    def distinctPropertiesRec(rulesList: List[ValuedProperty], matchedProperties: List[Property])
      : List[ValuedProperty]  = rulesList match {
      case head :: tail =>
        if (matchedProperties.contains(head.property)) distinctPropertiesRec(tail, matchedProperties)
        else head :: distinctPropertiesRec(tail, head.property :: matchedProperties)
      case _ => List()
    }

    distinctPropertiesRec(rulesList, List())
  }
}
