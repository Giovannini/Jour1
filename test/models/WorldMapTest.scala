package models

import models.graph.{Coordinates, Instance}
import models.graph.custom_types.Label
import models.graph.concept.Concept
import models.map.WorldMap
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

/**
 * Test class for model WorldMap
 */
class WorldMapTest extends FunSuite with BeforeAndAfter{

    val label = "Map"
    val description = "The map of the world"
    val dimension = 5
    val concept1 = Concept("C1", List(), List())
    val concept2 = Concept("C2", List(), List())
    val concept3 = Concept("C3", List(), List())
    def emptyWorldMap = WorldMap(label, description, dimension, dimension)
    val coord_2_3 = Coordinates(2, 3)
    val instance1 = Instance(1, "I1", coord_2_3, List(), concept1)
    val instance2 = Instance(2, "I2", Coordinates(0, 0), List(), concept3)
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
        val updatedInstance = worldMap.addInstance(instance1)
        assert(worldMap.getInstancesAt(coord_2_3) == List(updatedInstance))
    }

    test("method addInstance should correctly add an instance to the map") {
        val worldMap = emptyWorldMap
        val updatedInstance = worldMap.addInstance(instance1)
        assert(worldMap.getInstances.contains(updatedInstance))
        assert(worldMap.getInstances.length == 1)
    }

}
