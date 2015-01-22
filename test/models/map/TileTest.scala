package models.map

import models.custom_types.{Label, Coordinates}
import models.ontology.Instance
import org.scalatest.FunSuite

/**
 * Created by giovannini on 1/22/15.
 */
class TileTest extends FunSuite {

    test("method hasInstances should return true if tile has any instances"){
        val i1 = Instance(Label("I1"), Coordinates(0, 0), List(), List())
        assert(Tile(Coordinates(0, 0), List(i1)).hasInstances)
        assert( ! Tile(Coordinates(0, 0), List()).hasInstances)
    }

    test("method hasInstance should return true the desired instance is in"){
        val i1 = Instance(Label("I1"), Coordinates(0, 0), List(), List())
        val i2 = Instance(Label("I2"), Coordinates(0, 0), List(), List())
        assert(Tile(Coordinates(0, 0), List(i1, i2)).hasInstance(i2))
        assert(! Tile(Coordinates(0, 0), List(i1)).hasInstance(i2))
    }

    test("method addInstanceToTile should correctly add a new instance to the tile"){
        val i1 = Instance(Label("I1"), Coordinates(0, 0), List(), List())
        val emptyTile = Tile(Coordinates(0, 0), List())
        assert(! emptyTile.hasInstances)
        val filledTile = Tile.addInstanceToTile(i1, emptyTile)
        assert(filledTile.hasInstances)
        assert(filledTile.hasInstance(i1))

        val i2 = Instance(Label("I2"), Coordinates(0, 0), List(), List())
        val emptyTile2 = Tile(Coordinates(0, 1), List())
        assert(! emptyTile2.hasInstances)
        val filledTile2 = Tile.addInstanceToTile(i2, emptyTile2)
        assert(! filledTile2.hasInstances)
        assert(! filledTile2.hasInstance(i2))
    }
}
