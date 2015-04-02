package models.graph.ontology

import models.graph.custom_types.Coordinates
import models.graph.ontology.concept.need.{MeanOfSatisfaction, Need}
import models.graph.ontology.concept.{Concept, ConceptDAO}
import models.graph.ontology.property.{Property, PropertyType}
import models.interaction.LogInteraction
import models.interaction.action.InstanceAction
import play.api.libs.json._


/**
 * Model for an instance of the ontology
 * @author Thomas GIOVANNINI
 * @param label of the instance
 */
case class Instance(
  id: Long,
  label: String,
  coordinates: Coordinates,
  properties: List[ValuedProperty],
  concept: Concept) {

  require(label.matches("^[A-Z][A-Za-z0-9_ ]*$") &&
          concept.properties.toSeq == properties.map(_.property).toSeq)

  /**
   * Parse an Instance to Json
   * @author Thomas GIOVANNINI
   * @return the Json form of the instance
   */
  def toJson: JsValue = Json.obj(
    "id" -> id,
    "label" -> label,
    "coordinates" -> Json.obj("x" -> JsNumber(coordinates.x), "y" -> JsNumber(coordinates.y)),
    "properties" -> properties.map(_.toJson),
    "concept" -> JsNumber(concept.id)
  )

  override def hashCode = label.hashCode + concept.hashCode

  /*###############
    Modifications
  ###############*/
  /**
   * Method to give id to the instance
   * @param newId to give to the instance
   * @return the same instance with updated id
   */
  def withId(newId: Long): Instance = {
    Instance(newId, label, coordinates, properties, concept)
  }

  /**
   * Modify the label of an instance
   * @param newLabel for the instance
   * @return a new instance looking lke this one but with an updated label
   */
  def withLabel(newLabel: String): Instance = {
    Instance(id, newLabel, coordinates, properties, concept)
  }

  /**
   * Method to give coordinates to the instance
   * @param newCoordinates to give to the instance
   * @return the same instance with updated coordinates
   */
  def at(newCoordinates: Coordinates): Instance = {
    Instance(id, label, newCoordinates, properties, concept)
  }

  /**
   * Method to add a property to the instance
   * @author Thomas GIOVANNINI
   * @param property to add
   * @return the same instance with the new property added
   */
  def withProperty(property: Property): Instance = {
    Instance(id, label, coordinates, property.defaultValuedProperty :: properties, concept)
  }

  /**
   * Update the list of properties of a given instance
   * @author Thomas GIOVANNINI
   * @param newProperties for the instance
   * @return a new instance looking like this one but with updated properties
   */
  def withProperties(newProperties: List[ValuedProperty]): Instance = {
    if (newProperties.map(_.property) == this.concept.properties) {
      Instance(id, label, coordinates, newProperties, concept)
    }
    else {
      this
    }
  }

  /**
   * Update a list of needs for an instance
   * @return a list of logs to update the instance
   */
  def applyConsequencies(): List[LogInteraction] = {
    concept.needs.flatMap { need =>
      val property = need.affectedProperty
      val propertyValue = this.getValueForProperty(property)
      val steps = need.consequencesSteps
      steps.filter(cs => cs.value <= propertyValue).lastOption match {
        case Some(consequenceStep) => consequenceStep.consequence.effect.logOn(this)
        case _ => List()
      }
    }
  }

  /**
   * Modify the value of a given property of the instance
   * @author Thomas GIOVANNINI
   * @param valuedProperty to update
   * @return a new instance looking like this one but with an updated property
   */
  def modifyValueOfProperty(valuedProperty: ValuedProperty): Instance = {
    def modifyPropertyRec(vp: ValuedProperty, remainingProperties: List[ValuedProperty]): List[ValuedProperty] = {
      remainingProperties match {
        case head :: tail =>
          if (head.property == vp.property) {
            vp :: tail
          }
          else {
            head :: modifyPropertyRec(vp, tail)
          }
        case _ => List()
      }
    }

    if (this.concept.properties.contains(valuedProperty.property)) {
      val modifiedProperties = modifyPropertyRec(valuedProperty, this.properties)
      Instance(id, label, coordinates, modifiedProperties, concept)
    } else {
      this
    }
  }

  /**
   * Modify the concept of an instance
   * @author Thomas GIOVANNINI
   * @param newconcept for the instance
   * @return a new instance looking like this one but with an other concept
   */
  def ofConcept(newconcept: Concept): Instance = {
    Instance(id, label, coordinates, newconcept.properties.map(_.defaultValuedProperty), newconcept)
  }

  /**
   * Retrieve all the instances that are sensed by this instance.
   * @author Thomas GIOVANNINI
   * @return a list of sensed instances.
   */
  def getSensedInstances: List[Coordinates] = {
    val senseRadius = properties
      .find(_.property == Property("Sense", PropertyType.Int, 5))
      .getOrElse(ValuedProperty.error)
      .value
      .toInt
    coordinates.getNearCoordinate(senseRadius)
  }

  /**
   * Choose the better action an action has to do to fulfill its needs.
   * @author Thomas GIOVANNINI
   * @return an InstanceAction that the instance should do.
   */
  def selectAction(sensedInstances: List[Instance]): (InstanceAction, Instance) = {
    /**
     * Sort needs by order of importance
     * @author Thomas GIOVANNINI
     * @return a sorted list of needs
     */
    def orderNeedsByImportance: List[Need] = {
      this.concept.needs.sortBy(-_.evaluate(this))
    }

    val possibleActions = orderNeedsByImportance.flatMap(_.meansOfSatisfaction).distinct
    val relations = concept.getPossibleActionsAndDestinations

    /**
     * Check if the instance can do an action or not
     * @author Thomas GIOVANNINI
     * @param mean the instance will use
     * @return true if the instance can do the action
     *         false else
     */
    def destinationList(mean: MeanOfSatisfaction): List[Instance] = {
      val destinationList = mean.action.getDestinationList(this, sensedInstances)
      //TODO change that using any
      if (mean.destinationConcept == Concept.error) {
        destinationList.filter(instance => relations.getOrElse(mean.action, List()).contains(instance.concept))
      }
      else {
        destinationList.filter(instance => mean.destinationConcepts.contains(instance.concept))
      }
    }

    def retrieveBestAction(possibleActions: List[MeanOfSatisfaction])
    : (InstanceAction, Instance) = possibleActions match {
      case head :: tail =>
        val destinationsList = destinationList(head)
        if (destinationsList.nonEmpty) {
          (head.action, destinationsList.head)
        }
        else {
          retrieveBestAction(tail)
        }
      case _ =>
        println("No action found for instance " + this.label + this.id)
        (InstanceAction.error, this)
    }

    retrieveBestAction(possibleActions)
  }

  /**
   * Get value for a given property
   * @author Thomas GIOVANNINI
   * @param property of which the value is desired
   * @return the value of the property
   */
  def getValueForProperty(property: Property): Double = {
    properties.find(_.property == property).getOrElse(ValuedProperty.error).value
  }
}

object Instance {

  val error = Instance(0, "XXX", Coordinates(0, 0), List(), Concept.error)

  /**
   * Transform a json representing an instance into the Instance it represents
   * @author Thomas GIOVANNINI
   * @param jsonInstance the instance to parse
   * @return the represented instance
   */
  def parseJson(jsonInstance: JsValue): Instance = {
    val id = (jsonInstance \ "id").as[Long]
    val label = (jsonInstance \ "label").as[String]
    val coordinates = Coordinates.parseJson(jsonInstance \ "coordinates")
    val properties = (jsonInstance \ "properties").as[List[JsValue]].map(ValuedProperty.parseJson)
    val conceptId = (jsonInstance \ "concept").as[Long]
    ConceptDAO.getById(conceptId) match {
      // TODO better verification
      case Concept.error => error
      case concept => Instance(id, label, coordinates, properties, concept)
    }
  }

  /**
   * Create an instance of a certain concept with random attributes
   * @author Thomas GIOVANNINI
   * @param concept the concept of which the instance is desired
   * @return a new instance
   */
  def createRandomInstanceOf(concept: Concept): Instance = {
    Instance(0,
      concept.label,
      Coordinates(0, 0),
      concept.properties.map(_.defaultValuedProperty),
      concept)
  }
}

