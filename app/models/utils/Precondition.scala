package models.utils

import models.graph.ontology.Instance
import models.map.WorldMap

/**
 * Created by giovannini on 2/10/15.
 */

case class Precondition(label: String, referenceId: String, subPreconditions: List[Precondition], arguments: List[Argument])

class PreconditionManager(map: WorldMap){
  def isNextTo(element1: Int, element2: Int):Boolean = {
    val instance1 = map.getInstanceById(element1)
    val instance2 = map.getInstanceById(element2)
    instance1.coordinates.isNextTo(instance2.coordinates)
  }
}

object Precondition{

  /*def parsePrecondition(preconditionToString: String, arguments: List[Argument]) = {
    preconditionToString match {
      case strString if =>
    }
  }*/
}
