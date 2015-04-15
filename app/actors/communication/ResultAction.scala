package actors.communication

import actors.WorldCommunication
import models.interaction.LogInteraction

/**
 *
 * @param actionLog
 */
case class ResultAction(actionLog: (List[LogInteraction], List[LogInteraction], List[LogInteraction])) extends WorldCommunication
