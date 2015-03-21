package models.graph.ontology

import models.graph.ontology.property.Property
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
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

  val form = Form(mapping(
    "property" -> text,
    "value" -> of[Double]
  )(ValuedProperty.applyForm)(ValuedProperty.unapplyForm))

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param propertyToParse of the valued property
   * @param value of the valued property
   * @return valued property object
   */
  def applyForm(propertyToParse: String, value: Double): ValuedProperty = {
    val property = Property.parseString(propertyToParse)
    ValuedProperty(property, value)
  }

  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param valuedProperty valued property object
   * @return tuple of property and value
   */
  def unapplyForm(valuedProperty: ValuedProperty): Option[(String, Double)] = {
    Some((valuedProperty.property.toString, valuedProperty.value))
  }

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
    ValuedProperty(property, value)
  }

  /**
   * Builds a new list from this one without any duplicate property elements.
   * @author Thomas GIOVANNINI
   * @param rulesList the list to reduce
   * @return A new list which contains the first occurrence of every property of this list. 
   */
  def distinctProperties(rulesList: List[ValuedProperty])= {
    /**
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
