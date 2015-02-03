package models.custom_types

import models.graph.custom_types.Coordinates
import org.scalatest.FunSuite

/**
 * Test class for Coordinates class
 */
class CoordinatesTest extends FunSuite {

  test("method +"){
    val coord1 = Coordinates(12, 3)
    val coord2 = Coordinates(5, 4)
    assert(coord1 + coord2 == Coordinates(17, 7))
    assert(coord1 + coord2 == Coordinates(12+5, 3+4))
  }
}
