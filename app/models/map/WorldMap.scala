package models.map

import models.graph.custom_types.{Label, Coordinates}
import models.graph.ontology.{Concept, Instance}
import play.api.libs.json.{JsValue, Json}


/**
 * Model for the world map
 * @author Thomas GIOVANNINI
 * @param label of the map
 * @param description of the map
 * @param width of the map
 * @param height of the map
 */
case class WorldMap (label: Label,
                    description: String,
                    width: Int,
                    height: Int){

    val instances: collection.mutable.Map[Int, List[Instance]] = collection.mutable.Map.empty[Int, List[Instance]]

    def getInstances: List[Instance] = instances.map(_._2).flatten.toList

    /**
     * Retourne la carte sous la forme Json
     * @author Thomas GIOVANNINI
     * @return la carte sous la forme Json
     */
    def toJson : JsValue = {
        Json.obj(
          "width" -> width,
          "height" -> height,
          "instances" -> instances.map(_._2).flatten.map(_.toJson)
        )
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

    /**
     * Add an instance to a tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param instance to add to the tile
     * @param coordinates of the tile
     */
    def addInstanceAt(instance: Instance, coordinates: Coordinates): Unit = {
        val key = instance.concept.id
        val updatedInstance = Instance(instance.id, instance.label, coordinates, instance.properties, instance.concept)
        instances(key) = updatedInstance :: instances.getOrElse(key, List())
    }

    /**
     * Remove an instance to a tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param instance to remove to the tile
     * @param coordinates of the tile
     */
    def removeInstanceAt(instance: Instance, coordinates: Coordinates): Unit = {
        val key = instance.concept.id
        if(instances.contains(key))
            instances(key) = instances(key) diff List(instance)
    }

    /**
     * Move an instance on the world map
     * @author Thomas GIOVANNINI
     * @param instance to move
     * @param xMove horizontal move
     * @param yMove vertical move
     */
    def move(instance: Instance, xMove: Int, yMove: Int): Unit = {
        val instanceCoordinates = instance.coordinates
        val newCoordinates = instanceCoordinates + Coordinates(xMove, yMove)
        this.removeInstanceAt(instance, instanceCoordinates)
        this.addInstanceAt(instance, newCoordinates)
    }

    /**
     * Return whether an instance of a particular concept is on it or not
     * @author Thomas GIOVANNINI
     * @param concept desired
     * @param on the tile where the method search occurs
     * @return true if the tile contains an instance of the desired concept
     *         false else
     */
    def search(concept: Concept, on: Coordinates) = getInstancesAt(on).map(_.concept).contains(concept)

    /**
     * Return whether the tile has instances on it or not
     * @author Thomas GIOVANNINI
     * @param instance desired
     * @param on the tile where the method search occurs
     * @return true if the tile contains the desired instance
     *         false else
     */
    def search(instance: Instance, on: Coordinates) = getInstancesAt(on).contains(instance)
}
