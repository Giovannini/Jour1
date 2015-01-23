package models.map

import models.custom_types.Coordinates
import models.ontology.Instance

/**
 * Model for a tile on the world map
 * @author Thomas GIOVANNINI
 * @param coordinates of the tile
 * @param instances present on the tile
 */
case class Tile (coordinates: Coordinates, instances: List[Instance]){

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

}

object Tile {

    /**
     * Method to add an instance to a tile
     * @author Thomas GIOVANNINI
     * @param newInstance to add to the tile
     * @param tile to which the instance has to be added
     * @return a copy of the old tile with the new instance added to it
     */
    def addInstanceToTile(newInstance: Instance, tile: Tile): Tile = {
        val instance = Instance(newInstance.label,
            tile.coordinates,
            newInstance.properties,
            newInstance.concepts)
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
        val instanceToRemove =
            Instance(instance.label, tile.coordinates, instance.properties, instance.concepts)
        Tile(tile.coordinates, tile.instances diff List(instanceToRemove))
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
    def updateInstance(oldInstance: Instance, newInstance: Instance, tile: Tile): Tile ={
        if (tile.hasInstance(oldInstance)) {
            val partiallyUpdatedTile = Tile.removeInstanceFromTile(oldInstance, tile)
            Tile.addInstanceToTile(newInstance, partiallyUpdatedTile)
        } else 
            tile
    }

}
