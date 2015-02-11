package models.utils


/**
 * Created by giovannini on 2/10/15.
 */
case class ActionParser(actionManager: ActionManager) {

  /**
   * Parse an action from client-side and execute it.
   * @author Thomas GIOVANNINI
   * @param actionReference the reference of the action, its id
   * @param instancesId the instances arguments of the action
   * @return true if the execution went well
   *         false else
   */
  def parseAction(actionReference: String, instancesId: List[Int]): Boolean = {
    val action = getAction(actionReference)
    val arguments = getArgumentsList(action, instancesId)
    actionManager.execute(action, arguments)
  }

  /**
   * Retrieve action from the actions database and parse it to an action object
   * @author Thomas GIOVANNINI
   * @param actionReference the id of the desired action
   * @return an action object
   */
  def getAction(actionReference: String): Action = {
    //Just for test
    Action("addInstanceAt", "addInstanceAt0", List(), List(),
      List(Argument("instanceId", "Int"), Argument("coordinateX", "Int"), Argument("coordinateY", "Int")))
    /*TODO
     * val fromDBResultAction = database.get(actionReference)
     * parse(fromDBResultAction)
     */
  }

  /**
   * Get the argument list needed to execute an action
   * @param action the action to execute
   * @param ids the ids of the instances needed to execute the actions
   * @return a list of arguments and their values
   */
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
