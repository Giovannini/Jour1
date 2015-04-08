package models

import models.graph.custom_types.{Label, Coordinates}
import models.graph.ontology.concept.{ConceptDAO, Concept}
import models.graph.ontology.property.{PropertyDAO, Property}
import models.graph.ontology.{ValuedProperty, Instance}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Success, Failure, Try}


/**
 * Model for the world map
 * @author Thomas GIOVANNINI
 * @param label of the map
 * @param description of the map
 * @param width of the map
 * @param height of the map
 */
case class WorldMap(label: Label, description: String, width: Int, height: Int) {

  def clear() = {
    instancesByConcept = instancesByConcept.empty
    instancesByCoordinates = instancesByCoordinates.empty
    instanceIdCounter = 0
  }

  private var instanceIdCounter = 0

  /**
   * Get a new ID for an instance and autoincrement it
   * @author Thomas GIOVANNINI
   * @return a new ID
   */
  def getNewInstanceId: Int = {
    instanceIdCounter = instanceIdCounter + 1
    instanceIdCounter
  }

  private var instancesByConcept = collection.mutable.Map.empty[Long, List[Instance]]
  private var instancesByCoordinates = collection.mutable.Map.empty[Coordinates, List[Instance]]

  /**
   * Get all the instances existing on the world map
   * @author Thomas GIOVANNINI
   * @return a list of all the instances existing on the world map
   */
  def getInstances: List[Instance] = {
    instancesByConcept.values.flatten.toList
  }

  /**
   * Get all the instances that have needs to fulfill
   * @author Thomas GIOVANNINI
   * @return a list of all the instances with needs existing on the world map
   */
  def getInstancesWithNeeds: List[Instance] = {
    instancesByConcept
      .filterKeys(key => ConceptDAO.getById(key).needs.nonEmpty)
      .values
      .flatten
      .toList
  }

  /**
   * Get all the instances on a world map of a desired concept
   * @author Thomas GIOVANNINI
   * @param conceptId from which the instances are desired
   * @return a list of instances
   */
  def getInstancesOf(conceptId: Long): List[Instance] = {
    val conceptInstances = instancesByConcept.getOrElse(conceptId, List())
    val childrenInstances = ConceptDAO.getChildren(conceptId)
      .flatMap {
      concept => instancesByConcept.getOrElse(concept.id, List())
    }
    conceptInstances ::: childrenInstances
  }

  /**
   * Get the id that has instanceId for its ID
   * @author Thomas GIOVANNINI
   * @param instanceId the ID of the desired Instance
   * @return the desired instance
   */
  def getInstanceById(instanceId: Long): Instance = {
    getInstances.find(_.id == instanceId)
      .getOrElse(Instance.error)
  }

  /**
   * Method to get the world map under a Json format
   * @author Thomas GIOVANNINI
   * @return the world map under a Json format
   */
  def toJson: JsValue = {
    Json.obj(
      "width" -> width,
      "height" -> height,
      "instances" -> getInstances.map(_.toJson)
    )
  }

  /**
   * Update all instances of a given concept if a new property is added to it.
   * @author Thomas GIOVANNINI
   * @param concept to which the property is added
   * @param property added to the concept
   */
  //TODO update children
  def updateInstancesOf(concept: Concept, property: Property) = {
    val updatedInstancesList = getInstancesOf(concept.id)
      .map(instance => instance.withProperty(property))
    instancesByConcept(concept.id) = updatedInstancesList
    instancesByCoordinates = collection.mutable.Map(getInstances.groupBy(_.coordinates).toSeq: _*)
  }

  /*###########################################################################################################*/
  /*                            Basic functions                                                                */
  /*###########################################################################################################*/
  /**
   * Get the tile at the given coordinates
   * @author Thomas GIOVANNINI
   * @param coordinates of the wanted tile
   * @return the tile at the given coordinates
   */
  def getInstancesAt(coordinates: Coordinates): List[Instance] = {
    instancesByCoordinates.getOrElse(coordinates, List())
  }

  def createInstance(instance: Instance): Unit = {
    Try {
      val conceptID = instance.concept.id
      val coordinates = instance.coordinates
      val createdInstance = instance.withId(getNewInstanceId)
      instancesByConcept(conceptID) =
        createdInstance :: instancesByConcept.getOrElse(conceptID, List())
      instancesByCoordinates(coordinates) =
        createdInstance :: instancesByCoordinates.getOrElse(coordinates, List())
    } match {
      case Success(_) =>
      case Failure(e) =>
        println("Failure while adding an instance to the map in WorldMap.scala:")
        println(e)
    }
  }

  /**
   * Add a copy of an existing instance to the map at the given coordinates
   * @author Thomas GIOVANNINI
   * @param instanceId to add to the map
   * @param groundId where to add it
   */
  def addInstance(instanceId: Long, groundId: Long): Unit = {
    Try {
      val ground = getInstanceById(groundId)
      val coordinates = ground.coordinates
      val instance = getInstanceById(instanceId)
        .withId(getNewInstanceId)
        .at(coordinates)

      val conceptID = instance.concept.id
      instancesByConcept(conceptID) = instance :: instancesByConcept.getOrElse(conceptID, List())
      instancesByCoordinates(coordinates) =
        instance :: instancesByCoordinates.getOrElse(coordinates, List())
    } match {
      case Success(_) =>
      case Failure(e) =>
        println("Failure while adding an instance to the map in WorldMap.scala:")
        println(e)
    }
  }

  /**
   * Remove an instance at the given coordinates from the map
   * @author Thomas GIOVANNINI
   * @param instanceId to remove from the map
   */
  def removeInstance(instanceId: Long): Boolean = {
    Try {
      val instance = getInstanceById(instanceId)
      val conceptId = instance.concept.id
      val coordinates = instance.coordinates
      instancesByConcept(conceptId) = instancesByConcept.getOrElse(conceptId, List()) diff List(instance)
      instancesByCoordinates(coordinates) = instancesByCoordinates.getOrElse(coordinates, List()) diff List(instance)
    } match {
      case Success(_) => true
      case Failure(e) =>
        println("Failure while removing an instance from the map in WorldMap.scala:")
        println(e)
        false
    }
  }

  def modifyPropertyWithParam(instanceId: Long, propertyString: String, propertyValue: String): Unit = {
    val instance = getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    val property2 = PropertyDAO.getByName(propertyValue)

    val valueOfProperty: Double = instance.getValueForProperty(property2)
    val modifiedInstance = instance.modifyValueOfProperty(ValuedProperty(property,valueOfProperty))

    val conceptId = instance.concept.id
    val coordinates = instance.coordinates
    println("======= > "+modifiedInstance)
    instancesByConcept(conceptId) =
      modifiedInstance :: (instancesByConcept.getOrElse(conceptId, List()) diff List(instance))
    instancesByCoordinates(coordinates) =
      modifiedInstance :: (instancesByCoordinates.getOrElse(coordinates, List()) diff List(instance))

  }
  def modifyProperty(instanceId: Long, propertyString: String, propertyValue: Double): Unit = {
    val instance = getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    val modifiedInstance = instance.modifyValueOfProperty(ValuedProperty(property, propertyValue))

    val conceptId = instance.concept.id
    val coordinates = instance.coordinates
    instancesByConcept(conceptId) =
      modifiedInstance :: (instancesByConcept.getOrElse(conceptId, List()) diff List(instance))
    instancesByCoordinates(coordinates) =
      modifiedInstance :: (instancesByCoordinates.getOrElse(coordinates, List()) diff List(instance))

  }

  def addToProperty(instanceId: Long, propertyString: String, valueToAdd: Double): Unit = {
    val instance = getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    val newValue = instance.getValueForProperty(property) + valueToAdd
    val modifiedInstance = instance.modifyValueOfProperty(ValuedProperty(property, newValue))

    val conceptId = instance.concept.id
    val coordinates = instance.coordinates
    instancesByConcept(conceptId) = modifiedInstance :: (instancesByConcept.getOrElse(conceptId, List()) diff List(instance))
    instancesByCoordinates(coordinates) =
      modifiedInstance :: (instancesByCoordinates.getOrElse(coordinates, List()) diff List(instance))

  }

  def updateInstance(oldInstance: Instance, newInstance: Instance): Unit = {
    val conceptId = oldInstance.concept.id
    val oldCoordinates = oldInstance.coordinates
    val newCoordinates = newInstance.coordinates
    val updatedInstance = newInstance.withId(oldInstance.id)
    instancesByConcept(conceptId) =
      updatedInstance :: (instancesByConcept.getOrElse(conceptId, List()) diff List(oldInstance))
    instancesByCoordinates(oldCoordinates) =
      instancesByCoordinates.getOrElse(oldCoordinates, List()) diff List(oldInstance)
    instancesByCoordinates(newCoordinates) =
      updatedInstance :: instancesByCoordinates.getOrElse(newCoordinates, List())
  }
}
