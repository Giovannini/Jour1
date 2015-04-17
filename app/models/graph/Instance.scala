package models.graph

import controllers.Application
import models.graph.concept.{Concept, ConceptDAO}
import models.graph.property.{Property, PropertyDAO, ValuedProperty}
import models.intelligence.MeanOfSatisfaction
import models.intelligence.need.Need
import models.interaction.LogInteraction
import models.interaction.action.InstanceAction
import play.api.Logger
import play.api.libs.json._

import scala.util.{Try, Failure, Success, Random}


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
          concept.properties.map(_.property).toSeq == properties.map(_.property).toSeq)

  lazy val log = Logger("application." + this.getClass.getName)

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
    if (newProperties.map(_.property) == this.concept.properties.map(_.property)) {
      Instance(id, label, coordinates, newProperties, concept)
    }
    else {
      this
    }
  }

  /**
   * Modify the value of a given property of the instance
   * @author Thomas GIOVANNINI
   * @param valuedProperty to update
   * @return a new instance looking like this one but with an updated property
   */
  def modifyValueOfProperty(valuedProperty: ValuedProperty): Instance = {
    Try {
      val index = this.properties.map(_.property).indexOf(valuedProperty.property)
      val modifiedProperties = this.properties.updated(index, valuedProperty)
      Instance(id, label, coordinates, modifiedProperties, concept)
    } match {
      case Success(instance) => instance
      case Failure(e) =>
        log error "Error while modifying value for a property:"
        log error e.toString
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
    Instance(id, label, coordinates, newconcept.properties, newconcept)
  }

  /**
   * Get value for a given property
   * @author Thomas GIOVANNINI
   * @param property of which the value is desired
   * @return the value of the property
   */
  def getValueForProperty(property: Property): Double = {
    //TODO issue with equality here, comparing label for test to success
    this.properties.find(vp => vp.property.label == property.label)
      .getOrElse {
      ValuedProperty.error
    }
      .value
  }


  /*#######################################################################################
   ################################# Intelligence #########################################
   #######################################################################################*/
  /**
   * Retrieve all the instances that are sensed by this instance.
   * @author Thomas GIOVANNINI
   * @return a list of sensed instances.
   */
  def getSensedInstances: List[Instance] = {
    val senseRadius = getValueForProperty(PropertyDAO.getByName("Sense")).toInt
    coordinates.getNearCoordinate(senseRadius)
      .flatMap(Application.map.getInstancesAt)
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
      val consequenceLogs = steps.filter(cs => cs.value <= propertyValue).sortBy(_.value).lastOption match {
        case Some(consequenceStep) =>
          consequenceStep.consequence.effect.logOn(this)
        case _ => List()
      }
      consequenceLogs
    }
  }

    /**
     * Choose the better action an action has to do to fulfill its needs.
     * @author Thomas GIOVANNINI
     * @return an InstanceAction that the instance should do.
     */
    def selectAction(sensedInstances: List[Instance]): (InstanceAction, Instance) = {
      /*
       * Sort needs by order of importance
       * @author Thomas GIOVANNINI
       * @return a sorted list of needs
       */
      def orderNeedsByImportance: List[Need] = {
        this.concept.needs.sortBy(-_.evaluate(this))
      }

      val sortedNeeds = orderNeedsByImportance
      val possibleActions = sortedNeeds.flatMap(_.meansOfSatisfaction).distinct
      val relations = concept.getPossibleActionsAndDestinations

      /*
       * Check if the instance can do an action or not
       * @author Thomas GIOVANNINI
       * @param mean the instance will use
       * @return true if the instance can do the action
       *         false else
       */
      def destinationList(mean: MeanOfSatisfaction): List[Instance] = {
        val destinationList = mean.action.getDestinationList(this, sensedInstances)
        if (mean.destinationConcept == Concept.any) {
          val remainingDestinationConcepts = relations.getOrElse(mean.action, List())
          destinationList.filter(instance => remainingDestinationConcepts.contains(instance.concept))
        } else if (mean.destinationConcept == Concept.self) {
          destinationList.filter(_.concept == this.concept)
        } else {
          destinationList.filter(instance => mean.destinationConcepts.contains(instance.concept))
        }.filter(_.isNotError)
      }

      def retrieveBestAction(possibleActions: List[MeanOfSatisfaction])
      : (InstanceAction, Instance) = {
        def getPseudoRandomly(list: List[((InstanceAction, List[Instance]), Double)])
        : (InstanceAction, List[Instance]) = list match {
          case first :: second :: tail =>
            if (Random.nextDouble() > first._2) {
              first._1
            }
            else {
              getPseudoRandomly(second :: tail)
            }
          case first :: Nil => first._1
          case _ => (InstanceAction.error, List())
        }

        val allDestinations = possibleActions.map(mean => (mean.action, destinationList(mean)))
          .zipWithIndex
          .filter(_._1._2.nonEmpty)
        if (allDestinations.nonEmpty) {
          val prioritySum = allDestinations.map(_._2).sum.toDouble
          val chosenAction = getPseudoRandomly(allDestinations.map(dest => (dest._1, dest._2.toDouble / prioritySum)))
          (chosenAction._1, Random.shuffle(chosenAction._2).head)
        } else {
//          log error ("No action found for instance " + this.label + this.id)
          (InstanceAction.error, this)
        }
      }

      retrieveBestAction(possibleActions)
    }

    def isNotError: Boolean = {
      //TODO compare not only with label
      this.label != Instance.error.label
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
        concept.properties,
        concept)
    }
  }

