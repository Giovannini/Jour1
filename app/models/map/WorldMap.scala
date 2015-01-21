package models.map

import models.custom_text.Label


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

    private val map = {
        //Creation of a matrix with a width equal to "width" and an height equal to "height".
        val matrix = Array.ofDim[Tile](width, height)
        initialize(matrix)
    }

    /**
     * Fill a matrix of tile with empty tiles
     * @author Thomas GIOVANNINI
     * @param matrix Tile matrix to initialize
     * @return the initialized matrix
     */
    private def initialize(matrix: Array[Array[Tile]]): Array[Array[Tile]] = {

        for (i <- 1 to width; j <- 1 to height)
            matrix(i)(j) = Tile(i, j, List())

        matrix
    }

    /**
     * Get the tile at the given coordinates
     * @author Thomas GIOVANNINI
     * @param coordX of the wanted tile
     * @param coordY of the wanted tile
     * @return the tile at the given coordinates
     */
    def getTileAt(coordX: Int, coordY: Int): Tile = map(coordX)(coordY)
}
