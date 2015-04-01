package actors.communication

import actors.WorldCommunication
import models.interaction.LogInteraction

/**
 * Created by giovannini on 3/30/15.
 */
case class ResultAction(actionLog: List[LogInteraction]) extends WorldCommunication
