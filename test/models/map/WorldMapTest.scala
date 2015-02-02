package models.map

import models.graph.custom_types.{Label, Coordinates}
import models.graph.ontology.{Concept, Instance}
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

/**
 * Test class for model WorldMap
 */
class WorldMapTest extends FunSuite with BeforeAndAfter{

    val label = Label("Map")
    val description = "The map of the world"
    val dimension = 5
    val concept1 = Concept("C1", List())
    val concept2 = Concept("C2", List())
    val concept3 = Concept("C3", List())
    def emptyWorldMap = WorldMap(label, description, dimension, dimension)
    val instance1 = Instance("I1", Coordinates(0, 0), List(), concept1)
    val instance2 = Instance("I2", Coordinates(0, 0), List(), concept3)
    val coord_2_3 = Coordinates(2, 3)
    /**
     * An instance has a coordinates property. That means that two instances totally similar
     * excepts in their localization are not considered to be the same.
     * Then once an instance is added on a particular tile and that its position changes, we wont
     * be able to look for the "same" instance algorithmicly speaking; we'll have to look for the
     * instance with its new position.
     * This is why we need the variable "updatedInstance2".
     */
    val updatedInstance1 = Instance("I1", coord_2_3, List(), concept1)
    val updatedInstance2 = Instance("I2", coord_2_3, List(), concept3)

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

    test("the move method should correctly move the instance from a tile to an other"){
        val world = emptyWorldMap
        world.addInstanceAt(instance1, coord_2_3)
        assert(world.getTileAt(coord_2_3).hasInstance(updatedInstance1))
        world.move(updatedInstance1, 1, 1)
        val reUpdatedInstance = Instance(updatedInstance1.label,
            updatedInstance1.coordinates + Coordinates(1, 1),
            updatedInstance1.properties,
            updatedInstance1.concept)
        assert(world.getTileAt(coord_2_3 + Coordinates(1, 1)).hasInstance(reUpdatedInstance))
        assert( ! world.getTileAt(coord_2_3).hasInstance(updatedInstance1))
    }

    test("the search method used on a concept should tell if the concept is on a tile or not"){
        val world = emptyWorldMap
        world.addInstanceAt(instance2, coord_2_3)
        assert(world.search(concept3, world.getTileAt(coord_2_3)))
        assert(! world.search(concept2, world.getTileAt(coord_2_3)))
        assert(! world.search(concept2, world.getTileAt(0,0)))
    }

    test("the search method used on an instance should tell if the instance is on a tile or not"){
        val world = emptyWorldMap
        world.addInstanceAt(instance2, coord_2_3)
        assert(world.getTileAt(coord_2_3).hasInstances)
        assert(world.search(updatedInstance2, world.getTileAt(coord_2_3)))
        assert(! world.search(updatedInstance2, world.getTileAt(0,0)))
        assert(! world.search(updatedInstance1, world.getTileAt(coord_2_3)))
    }

}
