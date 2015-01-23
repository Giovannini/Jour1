package models.map

import models.custom_types.{Label, Coordinates}
import models.ontology.Instance
import org.scalatest.FunSuite

/**
 * Test class for the Tile model
 */
class TileTest extends FunSuite {

    val i1 = Instance(Label("I1"), Coordinates(0, 0), List(), List())
    val i2 = Instance(Label("I2"), Coordinates(0, 0), List(), List())
    val updatedI1 = Instance(i1.label, Coordinates(1, 2), i1.properties, i1.concepts)
    val updatedI2 = Instance(i2.label, Coordinates(1, 2), i2.properties, i2.concepts)
    val emptyTile = Tile(Coordinates(1, 2), List())

    test("method hasInstances should return true if tile has any instances"){
        assert(Tile(Coordinates(1, 2), List(i1)).hasInstances)
        assert(! emptyTile.hasInstances)
    }

    test("method hasInstance should return true the desired instance is in"){
        assert(Tile(Coordinates(1, 2), List(i1, i2)).hasInstance(i2))
        assert(! Tile(Coordinates(1, 2), List(i1)).hasInstance(i2))
    }

    test("method addInstanceToTile should correctly add a new instance to the tile"){
        val filledTile = Tile.addInstanceToTile(i1, emptyTile)
        assert(filledTile.hasInstances)
        assert(filledTile.hasInstance(updatedI1))
    }

    test("method removeInstanceFromTile should remove an existing instance from the tile") {
        val tile = Tile(Coordinates(1, 2), List(updatedI1, updatedI2))
        val notFullTile = Tile.removeInstanceFromTile(updatedI1, tile)
        assert(! notFullTile.hasInstance(updatedI1))
        assert(notFullTile.hasInstance(updatedI2))
    }

    test("method updateInstance should update an existing instance in the tile"){
        val tile = Tile(Coordinates(1, 2), List(updatedI1))
        val notUpdatedTile = Tile.updateInstance(updatedI2, updatedI1, tile)
        assert(notUpdatedTile.hasInstance(updatedI1))
        assert(! notUpdatedTile.hasInstance(updatedI2))

        val updatedTile = Tile.updateInstance(updatedI1, updatedI2, tile)
        assert(! updatedTile.hasInstance(updatedI1))
        assert(updatedTile.hasInstance(updatedI2))
    }
}
