package models.graph.custom_types

/**
 * Model for coordinates
 * @author Thomas GIOVANNINI
 * @param x coordinate
 * @param y coordinate
 */
case class Coordinates(x: Int, y: Int){

    /**
     * Method to add two coordinates
     * @author Thomas GIOVANNINI
     * @param other coordinate to add this one
     * @return the sum of the two coordinates
     */
    def +(other: Coordinates): Coordinates = Coordinates(this.x + other.x, this.y + other.y)

    def isNextTo(other: Coordinates): Boolean ={
        this.getDistanceWith(other) <= math.sqrt(2)
    }

    def getDistanceWith(other: Coordinates): Double = {
        val xDistance = this.x - other.x
        val yDistance = this.y - other.y
        math.sqrt(math.pow(xDistance, 2) + math.pow(yDistance, 2))
    }

}
