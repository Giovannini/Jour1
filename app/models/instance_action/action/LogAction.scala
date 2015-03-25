package models.instance_action.action

import scala.util.{Failure, Success, Try}

/**
 * Created by giovannini on 3/24/15.
 */
case class LogAction(value: String){



}

object LogAction {

  val nothing = LogAction("ERROR")

  def parseAction(log: LogAction): Unit = {
    Try {
      val splitted = log.value.split(" ")
      splitted(0) match {
        case "ADD" =>
          //Application.map.addInstance()
        case "REMOVE" =>
        case "UPDATE_PROPERTY" =>
        case error =>
          println("Error, log ")
          println(error)
          println("is unknown.")
      }
    } match {
      case Success(_) => println("Done")
      case Failure(e) =>
        println("Error while parsing action log:")
        println(e)
    }
  }

 /* def parseJson(json: JsValue): LogAction = {
    val action = InstanceAction.pars
  }*/
}
