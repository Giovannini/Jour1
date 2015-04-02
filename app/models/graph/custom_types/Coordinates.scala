package models.graph.custom_types

import controllers.Application
import play.api.libs.json.JsValue

/**
 * Model for coordinates
 * @author Thomas GIOVANNINI
 * @param x coordinate
 * @param y coordinate
 */
case class Coordinates(x: Int, y: Int) {

  def this(tuple: (Int, Int)) = this(tuple._1, tuple._2)

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
    Coordinates.getNextCoordinates.map(_ + this).contains(other)
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
//    math.sqrt(math.pow(xDistance, 2) + math.pow(yDistance, 2))
    math.abs(xDistance) + math.abs(yDistance)
  }

  /**
   * Get a list of coordinates at a given distance of this one
   * @author Thomas GIOVANNINI
   * @param radius to look into
   * @return a list of coordinates at a given distance of this one
   */
  def getNearCoordinate(radius: Int): List[Coordinates] = {
    val valueList = ((-radius) to radius).toList
    valueList.flatMap(value => valueList.map((_, value)))
      .filter(tuple => math.pow(tuple._1, 2) + math.pow(tuple._2, 2) < radius * radius)
      .map(tuple => new Coordinates(tuple) + this)
      .filter { coordinate =>
        coordinate.x >= 0 && coordinate.x < Coordinates.maxWidth &&
        coordinate.y >= 0 && coordinate.y < Coordinates.maxHeight
      }
  }
}

object Coordinates {

  val maxWidth = Application.map.width
  val maxHeight = Application.map.height

  val getNextCoordinates: List[Coordinates] = {
    List(Coordinates(0, 1), Coordinates(0, -1), Coordinates(1, 0), Coordinates(-1, 0))
  }

  def parseJson(json: JsValue): Coordinates = {
    val x = (json \ "x").as[Int]
    val y = (json \ "y").as[Int]
    Coordinates(x, y)
  }
}
