package models

import models.graph.custom_types.{Label, Coordinates}
import models.graph.ontology.concept.{ConceptDAO, Concept}
import models.graph.ontology.property.{PropertyDAO, Property}
import models.graph.ontology.Instance
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

  val instances: collection.mutable.Map[Long, List[Instance]] = collection.mutable.Map.empty[Long, List[Instance]]

  /**
   * Get all the instances existing on the world map
   * @author Thomas GIOVANNINI
   * @return a list of all the instances existing on the world map
   */
  def getInstances: List[Instance] = {
    instances.flatMap(_._2).toList
  }

  /**
   * Get all the instances on a world map of a desired concept
   * @author Thomas GIOVANNINI
   * @param conceptId from which the instances are desired
   * @return a list of instances
   */
  def getInstancesOf(conceptId: Long): List[Instance] = {
    val conceptInstances = instances.getOrElse(conceptId, List())
    val childrenInstances = ConceptDAO.getChildren(conceptId)
      .flatMap {
      concept => instances.getOrElse(concept.id, List())
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
      "instances" -> instances.map(_._2).flatten.map(_.toJson)
    )
  }

  /**
   * Update all instances of a given concept if a new property is added to it.
   * @author Thomas GIOVANNINI
   * @param concept to which the property is added
   * @param property added to the concept
   */
  def updateInstancesOf(concept: Concept, property: Property) = {
    val updatedInstancesList = instances.getOrElse(concept.id, List())
      .map(instance => instance.withProperty(property))
    instances(concept.id) = updatedInstancesList
  }

  /*#################*/
  /* Basic functions */
  /*#################*/
  /**
   * Get the tile at the given coordinates
   * @author Thomas GIOVANNINI
   * @param coordX of the wanted tile
   * @param coordY of the wanted tile
   * @return the tile at the given coordinates
   */
  def getInstancesAt(coordX: Int, coordY: Int): List[Instance] = {
    getInstances.filter(instance => instance.coordinates == Coordinates(coordX, coordY))
  }

  /**
   * Get the tile at the given coordinates
   * @author Thomas GIOVANNINI
   * @param coordinates of the wanted tile
   * @return the tile at the given coordinates
   */
  def getInstancesAt(coordinates: Coordinates): List[Instance] = {
    getInstances.filter(instance => instance.coordinates == coordinates)
  }

  def createInstance(instance: Instance): Unit = {
    Try {
//      println("Creating instance " + instance.label + " at " + instance.coordinates)
      val conceptID = instance.concept.id
      val length = instances.getOrElse(conceptID, List()).length
      instances(conceptID) = instance.withId(getNewInstanceId) :: instances.getOrElse(conceptID, List())
    } match {
      case Success(_) =>
      case Failure(e) =>
        println("Failure while adding an instance to the map:")
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
      val instance = getInstanceById(instanceId).withId(getNewInstanceId)
      val ground = getInstanceById(groundId)

      val conceptID = instance.concept.id
      instances(conceptID) = instance.at(ground.coordinates) :: instances.getOrElse(conceptID, List())
    } match {
      case Success(_) =>
      case Failure(e) =>
        println("Failure while adding an instance to the map:")
        println(e)
    }
  }

  /**
   * Remove an instance at the given coordinates from the map
   * @author Thomas GIOVANNINI
   * @param instanceId to remove from the map
   */
  def removeInstance(instanceId: Long): Unit = {
    Try {
      val instance = getInstanceById(instanceId)
      val conceptId = instance.concept.id
      instances(conceptId) = instances.getOrElse(conceptId, List()) diff List(instance)
    } match {
      case Success(_) =>
      case Failure(e) =>
        println("Failure while removing an instance from the map:")
        println(e)
    }
  }

  def modifyProperty(instanceId: Long, propertyString: String, propertyValue: Double): Unit = {
    val instance = getInstanceById(instanceId)
    val property = PropertyDAO.getByName(propertyString)
    val modifiedInstance = instance.modifyValueOfProperty(property, propertyValue)

    val conceptId = instance.concept.id
    instances(conceptId) = modifiedInstance :: (instances.getOrElse(conceptId, List()) diff List(instance))

  }

  def updateInstance(oldInstance: Instance, newInstance: Instance): Unit = {
    val conceptId = oldInstance.concept.id
    instances(conceptId) = newInstance.withId(oldInstance.id) ::
                           (instances.getOrElse(conceptId, List()) diff List(oldInstance))
  }

  /**
   * Return whether an instance of a particular concept is on it or not
   * @author Thomas GIOVANNINI
   * @param concept desired
   * @param on the tile where the method search occurs
   * @return true if the tile contains an instance of the desired concept
   *         false else
   */
  def search(concept: Concept, on: Coordinates) = {
    getInstancesAt(on).map(_.concept).contains(concept)
  }

  /**
   * Return whether the tile has instances on it or not
   * @author Thomas GIOVANNINI
   * @param instance desired
   * @param on the tile where the method search occurs
   * @return true if the tile contains the desired instance
   *         false else
   */
  def search(instance: Instance, on: Coordinates) = {
    getInstancesAt(on).contains(instance)
  }
}
