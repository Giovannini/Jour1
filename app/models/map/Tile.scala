package models.map

import models.graph.NeoDAO
import models.graph.custom_types.Coordinates
import models.graph.ontology.{Concept, Instance}
import play.api.libs.json.{JsValue, Json}

/**
 * Model for a tile on the world map
 * @author Thomas GIOVANNINI
 * @param coordinates of the tile
 * @param instances present on the tile
 */
case class Tile(coordinates: Coordinates, instances: List[Instance]) {

  /**
   * Transform a tile to json format
   * @author Thomas GIOVANNINI
   * @return the json format of the Tile
   */
  def toJson: JsValue = Json.obj(
    "instances" -> instances.map(_.toJson)
  )

  /**
   * Return whether the tile has instances on it or not
   * @author Thomas GIOVANNINI
   * @return true if the tile contains instances
   *         false else
   */
  def hasInstances = instances.nonEmpty

  /**
   * Return whether the tile has instances on it or not
   * @author Thomas GIOVANNINI
   * @param instance desired
   * @return true if the tile contains the desired instance
   *         false else
   */
  def hasInstance(instance: Instance) = instances.contains(instance)

  /**
   * Return whether an instance of a particular concept is on it or not
   * @author Thomas GIOVANNINI
   * @param concept desired
   * @return true if the tile contains an instance of the desired concept
   *         false else
   */
  def hasConcept(concept: Concept) = instances
    .map(_.concept)
    .contains(concept)

}

object Tile {

  def parseJson(jsonTile: JsValue): Tile = {
    val x_coordinate = (jsonTile \ "coordinates" \ "x").as[Int]
    val y_coordinate = (jsonTile \ "coordinates" \ "y").as[Int]
    val coordinates = Coordinates(x_coordinate, y_coordinate)
    val instances = (jsonTile \\ "instances").toList.map(Instance.parseJson)
    Tile(coordinates, instances)
  }

  /**
   * Method to add an instance to a tile
   * @author Thomas GIOVANNINI
   * @param newInstance to add to the tile
   * @param tile to which the instance has to be added
   * @return a copy of the old tile with the new instance added to it
   */
  def addInstanceToTile(newInstance: Instance, tile: Tile): Tile = {
    val instance = Instance(newInstance.id,
      newInstance.label,
      tile.coordinates,
      newInstance.properties,
      newInstance.concept)
    /*Une instance n'est jamais ajouté au graphe mais à une map.*/
    //NeoDAO.addInstance(instance)
    Tile(tile.coordinates, instance :: tile.instances)
  }

  /**
   * Method to remove an instance from a tile
   * @author Thomas GIOVANNINI
   * @param instance the instance to remove to the tile
   * @param tile the tile to which the instance has to be removed
   * @return if the instance is in the tile, a copy of the old tile with the new instance removed
   *         from it
   *         else the same old tile
   */
  def removeInstanceFromTile(instance: Instance, tile: Tile): Tile = {
    NeoDAO.removeInstance(instance)
    Tile(tile.coordinates, tile.instances diff List(instance))
  }

  /**
   * Method to update an instance on a tile
   * @author Thomas GIOVANNINI
   * @param oldInstance the instance to modify
   * @param newInstance the final instance
   * @param tile the tile to which the instance has to be updated
   * @return if oldInstance is on the tile, a tile with the newInstance replacing the old one
   *         else the same tile
   */
  def updateInstance(oldInstance: Instance, newInstance: Instance, tile: Tile): Tile = {
    def updateInstanceInList(oldOne: Instance, newOne: Instance, instances: List[Instance]): List[Instance] = {
      instances match {
        case h::t =>
          if(h.hashCode == oldOne.hashCode) newOne::t
          else h::updateInstanceInList(oldOne, newOne, t)
        case _ => List()
      }
    }
    Tile(tile.coordinates, updateInstanceInList(oldInstance, newInstance, tile.instances))
  }

}
