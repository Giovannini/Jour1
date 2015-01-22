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

object Tile{

    /**
     * Method to add an instance to a tile
     * @author Thomas GIOVANNINI
     * @param newInstance to add to the tile
     * @param tile to which the instance has to be added
     * @return a copy of the old tile with the new instance added to it
     */
    def addInstanceToTile(newInstance: Instance, tile: Tile): Tile =
        Tile(tile.coordinates, newInstance :: tile.instances)


    /**
     * Method to remove an instance to a tile
     * @author Thomas GIOVANNINI
     * @param instanceToRemove the instance to remove to the tile
     * @param tile the tile to which the instance has to be removed
     * @return if the instance is in the tile, a copy of the old tile with the new instance removed
     *         from it
     *         else the same old tile
     */
    def removeInstanceToTile(instanceToRemove: Instance, tile: Tile): Tile = {
        val indexOfTheInstanceToRemove = tile.instances.indexOf(instanceToRemove)

        if(indexOfTheInstanceToRemove != -1)
            Tile(tile.coordinates, tile.instances.drop(indexOfTheInstanceToRemove))
        else
            tile
    }

}
