package models.utils


/**
 * Created by giovannini on 2/10/15.
 */
case class ActionParser(actionManager: ActionManager) {

  def parseAction(actionReference: String, instancesId: Int*) = {
    val action = getAction(actionReference)
    val arguments = getArgumentsList(action, instancesId)
    actionManager.execute(action, arguments)
  }

  def getAction(actionReference: String): Action = {
    Action("addInstanceAt", "addInstanceAt0", List(), List(),
      List(Argument("instanceId", "Int"), Argument("coordinateX", "Int"), Argument("coordinateY", "Int")))
  }

  def getArgumentsList(action: Action, ids: Seq[Int]):List[(Argument, Any)] = {
    if(ids.length == 2){
      val instanceId = ids.head
      val groundCoordinates = this.actionManager.map.getInstanceById(ids.last).coordinates
      val xCoordinates = groundCoordinates.x
      val yCoordinates = groundCoordinates.y
      val argsValueList = List(instanceId, xCoordinates, yCoordinates)
      action.arguments.zip(argsValueList)
    }else{
      println("Error while getting arguments list.")
      action.arguments.zip(ids)
    }
  }



}
