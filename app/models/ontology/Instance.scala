package models.ontology

import models.custom_types.{Coordinates, Label}
import models.map.WorldMap


/**
 * Model for an instance of the ontology
 * @author Thomas GIOVANNINI
 * @param label of the instance
 */
case class Instance(label:          Label,
                    coordinates:    Coordinates,
                    properties:     List[Property],
                    concepts:       List[Concept])

