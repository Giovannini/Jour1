package models.utils

import models.graph.custom_types.Coordinates
import models.map.WorldMap

/**
 * Created by giovannini on 2/10/15.
 */
object HardCodedMapActions {

  def addInstanceAt(args: Array[Any], map: WorldMap) = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Int])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    map.addInstanceAt(instance, Coordinates(xCoordinate, yCoordinate))
  }

  def removeInstanceAt(args: Array[Any], map: WorldMap) = {
    val instance = map.getInstanceById(args(0).asInstanceOf[Int])
    val xCoordinate = args(1).asInstanceOf[Int]
    val yCoordinate = args(2).asInstanceOf[Int]
    map.removeInstanceAt(instance, Coordinates(xCoordinate, yCoordinate))
  }

}
