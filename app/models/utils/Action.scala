package models.utils

/**
 * Created by giovannini on 2/11/15.
 */
case class Action(label: String, referenceId: String, preconditions: List[Precondition], subActions: List[Action], arguments: List[Argument])
