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

  test("method isNextTo"){
    val coord1 = Coordinates(5, 5)
    val coord2 = Coordinates(1, 1)
    val coord3 = Coordinates(5, 7)
    val coord4 = Coordinates(5, 6)
    val coord5 = Coordinates(6, 6)
    assert(coord1.isNextTo(coord1))
    assert(! coord1.isNextTo(coord2))
    assert(! coord1.isNextTo(coord3))
    assert(coord1.isNextTo(coord4))
    assert(coord1.isNextTo(coord5))
  }
}
