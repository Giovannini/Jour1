package models.map

import models.ontology.Instance

/**
 * Model for a tile on the world map
 * @author Thomas GIOVANNINI
 * @param coordX
 * @param coordY
 * @param instances
 */
case class Tile (coordX: Int, coordY: Int, instances: List[Instance])
