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
    val concept1 = Concept("C1", List(), List())
    val concept2 = Concept("C2", List(), List())
    val concept3 = Concept("C3", List(), List())
    def emptyWorldMap = WorldMap(label, description, dimension, dimension)
    val instance1 = Instance(1, "I1", Coordinates(0, 0), List(), concept1)
    val instance2 = Instance(2, "I2", Coordinates(0, 0), List(), concept3)
    val coord_2_3 = Coordinates(2, 3)
    /**
     * An instance has a coordinates property. That means that two instances totally similar
     * excepts in their localization are not considered to be the same.
     * Then once an instance is added on a particular tile and that its position changes, we wont
     * be able to look for the "same" instance algorithmicly speaking; we'll have to look for the
     * instance with its new position.
     * This is why we need the variable "updatedInstance2".
     */
    val updatedInstance1 = Instance(1, "I1", coord_2_3, List(), concept1)
    val updatedInstance2 = Instance(2, "I2", coord_2_3, List(), concept3)

    test("At its creation, a map has no instance"){
        val worldMap = emptyWorldMap
        assert(worldMap.getInstances.isEmpty)
    }

    test("method getInstancesAt"){
        val worldMap = emptyWorldMap
        val updatedInstance = worldMap.addInstanceAt(instance1, coord_2_3)
        assert(worldMap.getInstancesAt(coord_2_3) == List(updatedInstance))
    }

    test("method addInstanceAt should correctly add an instance on the desired tile only") {
        val worldMap = emptyWorldMap
        val updatedInstance = worldMap.addInstanceAt(instance1, coord_2_3)
        assert(worldMap.getInstances.contains(updatedInstance))
        assert(worldMap.getInstances.length == 1)
    }

    test("method removeInstanceAt should remove an existing instance on the desired tile") {
        val worldMap = emptyWorldMap
        val updatedInstance = worldMap.addInstanceAt(instance1, coord_2_3)
        worldMap.removeInstanceAt(instance2, coord_2_3)
        assert(worldMap.getInstances.nonEmpty) //No change
        worldMap.removeInstanceAt(updatedInstance, coord_2_3)
        assert(worldMap.getInstances.isEmpty)
    }

    test("the move method should correctly move the instance from a tile to an other"){
        val world = emptyWorldMap
        val updatedInstance = world.addInstanceAt(instance1, coord_2_3)
        val reUpdatedInstance = world.move(updatedInstance, 1, 1)
        assert(world.getInstancesAt(coord_2_3 + Coordinates(1, 1)).contains(reUpdatedInstance))
        assert(! world.getInstancesAt(coord_2_3).contains(updatedInstance1))
    }

    test("the search method used on a concept should tell if the concept is on a tile or not"){
        val world = emptyWorldMap
        world.addInstanceAt(instance2, coord_2_3)
        assert(world.search(concept3, coord_2_3))
        assert(! world.search(concept2, coord_2_3))
        assert(! world.search(concept2, Coordinates(0,0)))
    }

    test("the search method used on an instance should tell if the instance is on a tile or not"){
        val world = emptyWorldMap
        val updatedInstance = world.addInstanceAt(instance2, coord_2_3)
        assert(world.getInstancesAt(coord_2_3).nonEmpty)
        assert(world.search(updatedInstance, coord_2_3))
        assert(! world.search(updatedInstance, Coordinates(0,0)))
        assert(! world.search(instance2, coord_2_3))
    }

}
