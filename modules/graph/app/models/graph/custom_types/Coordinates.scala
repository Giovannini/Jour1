package models.graph.custom_types

/**
 * Model for coordinates
 * @param x coordinate
 * @param y coordinate
 */
case class Coordinates(x: Int, y: Int){

    def +(other: Coordinates): Coordinates = Coordinates(this.x + other.x, this.y + other.y)

}
