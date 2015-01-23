package models.map

import models.ontology.Instance
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import models.custom_types.{Coordinates, Label}

/**
 * Test class for model WorldMap
 */
class WorldMapTest extends FunSuite with BeforeAndAfter{

    val label = Label("Map")
    val description = "The map of the world"
    val dimension = 5
    def emptyWorldMap = WorldMap(label, description, dimension, dimension)
    val instance1 = Instance(Label("I1"), Coordinates(0, 0), List(), List())
    val instance2 = Instance(Label("I2"), Coordinates(0, 0), List(), List())
    val updatedInstance1 = Instance(Label("I1"), Coordinates(2, 3), List(), List())

    test("At its creation, a map is filled with empty tiles"){
        val worldMap = emptyWorldMap
        for(i <- 0 until dimension; j <- 0 until dimension){
            assert(! worldMap.getTileAt(i, j).hasInstances)
        }
    }

    test("method addInstanceAt should correctly add an instance on the desired tile only") {
        val worldMap = emptyWorldMap
        worldMap.addInstanceAt(instance1, Coordinates(2, 3))
        for(i <- 0 until dimension; j <- 0 until dimension){
            if (i == 2 && j == 3)
                assert(worldMap.getTileAt(2, 3).hasInstance(updatedInstance1))
            else
                assert(! worldMap.getTileAt(i, j).hasInstances)
        }
    }

    test("method removeInstanceAt should remove an existing instance on the desired tile") {
        val worldMap = emptyWorldMap
        worldMap.addInstanceAt(instance1, Coordinates(2, 3))
        worldMap.removeInstanceAt(instance2, Coordinates(2, 3))
        assert(worldMap == emptyWorldMap) //No change

        worldMap.removeInstanceAt(updatedInstance1, Coordinates(2, 3))
        for(i <- 0 until dimension; j <- 0 until dimension){
            assert(! worldMap.getTileAt(i, j).hasInstances)
        }

    }

}
