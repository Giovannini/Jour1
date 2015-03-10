package models.graph.custom_types

/**
 * Model for coordinates
 * @author Thomas GIOVANNINI
 * @param x coordinate
 * @param y coordinate
 */
case class Coordinates(x: Int, y: Int) {

  /**
   * Method to add two coordinates
   * @author Thomas GIOVANNINI
   * @param other coordinate to add this one
   * @return the sum of the two coordinates
   */
  def +(other: Coordinates): Coordinates = Coordinates(this.x + other.x, this.y + other.y)

  /**
   * Test whether two coordinates are next to each other
   * @param other coordinate to test
   * @return true if they are
   *         false else
   */
  def isNextTo(other: Coordinates): Boolean = {
    this.getDistanceWith(other) <= math.sqrt(2)
  }

  /**
   * Get distance with other coordinate
   * @author Thomas GIOVANNINI
   * @param other coordinate to test
   * @return the distance between them
   */
  def getDistanceWith(other: Coordinates): Double = {
    val xDistance = this.x - other.x
    val yDistance = this.y - other.y
    math.sqrt(math.pow(xDistance, 2) + math.pow(yDistance, 2))
  }

  /**
   * @author Simon
   * @param rayon
   * @return
   */
  def getNearCoordinate(rayon: Int): List[Coordinates] = {
    var listCoord: List[Coordinates] = List()
    for (x <- (-rayon) until rayon; y <- (-rayon) until rayon) {
      val coord = Coordinates(x, y)
      if (this.getDistanceWith(coord) <= rayon) {
        listCoord = coord :: listCoord
      }
    }
    listCoord
  }

  def getNextCoordinates: List[Coordinates] = {
    List(this + Coordinates(0, 1),
          this + Coordinates(0, -1),
          this + Coordinates(1, 0),
          this + Coordinates(-1, 0))
  }
}
