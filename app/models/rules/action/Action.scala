package models.rules.action

import models.rules.Argument
import models.rules.precondition.Precondition


/**
 * Action model
 */
case class Action(label: String,
                  referenceId: String,
                  preconditions: List[Precondition],
                  subActions: List[Action],
                  arguments: List[Argument])