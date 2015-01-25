package models.map

import models.custom_types.{Coordinates, Label}
import models.ontology.{Concept, Instance}


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

    /*private var time = 0*/

    private val map = {
        val matrix = Array.ofDim[Tile](width, height) //Creation of an empty matrix
        fillWithEmptyTiles(matrix)
    }

    /*private def timeFlows(): Unit = time = time + 1*/

    /**
     * Fill a matrix of tile with empty tiles
     * @author Thomas GIOVANNINI
     * @param matrix Tile matrix to initialize
     * @return the initialized matrix
     */
    private def fillWithEmptyTiles(matrix: Array[Array[Tile]]): Array[Array[Tile]] = {
        for (i <- 0 until width; j <- 0 until height)
            matrix(i)(j) = Tile(Coordinates(i, j), List())
        matrix
    }

    /* Basic functions */
    /**
     * Get the tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param coordX of the wanted tile
     * @param coordY of the wanted tile
     * @return the tile at the given coordinates
     */
    def getTileAt(coordX: Int, coordY: Int): Tile = map(coordX)(coordY)

    /**
     * Get the tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param coordinates of the wanted tile
     * @return the tile at the given coordinates
     */
    def getTileAt(coordinates: Coordinates): Tile = getTileAt(coordinates.x, coordinates.y)

    /**
     * Add an instance to a tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param instance to add to the tile
     * @param coordinates of the tile
     */
    def addInstanceAt(instance: Instance, coordinates: Coordinates): Unit = {
        map(coordinates.x)(coordinates.y) =
          Tile.addInstanceToTile(instance, getTileAt(coordinates))
    }

    /**
     * Remove an instance to a tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param instance to remove to the tile
     * @param coordinates of the tile
     */
    def removeInstanceAt(instance: Instance, coordinates: Coordinates): Unit = {
        map(coordinates.x)(coordinates.y) =
          Tile.removeInstanceFromTile(instance, getTileAt(coordinates))
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
    def search(concept: Concept, on: Tile) = on.hasConcept(concept)

    /**
     * Return whether the tile has instances on it or not
     * @author Thomas GIOVANNINI
     * @param instance desired
     * @param on the tile where the method search occurs
     * @return true if the tile contains the desired instance
     *         false else
     */
    def search(instance: Instance, on: Tile) = on.hasInstance(instance)
}
