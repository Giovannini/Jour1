package models.utils.precondition

import models.map.WorldMap
import models.utils.Argument

/**
 * Created by giovannini on 2/10/15.
 */

case class Precondition(label: String, referenceId: String, subPreconditions: List[Precondition], arguments: List[Argument])
