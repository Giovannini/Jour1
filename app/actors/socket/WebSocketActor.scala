package actors.socket

import akka.actor.Actor
import play.api.Logger
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json.Json._
import play.api.libs.json.{JsValue, _}

import scala.language.postfixOps


class WebSocketActor extends Actor {

  case class UserChannel(userId: Long, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

  lazy val log = Logger("application." + this.getClass.getName)

  /* Create a scheduler to send a message to this actor every second */
  //val cancellable = context.system.scheduler.schedule(0 second, 1 second, self, UpdateMap(Json.obj()))

  var usersSockets = Map[Long, UserChannel]()
  var usersLogs = Map[Long, JsValue]()

  override def receive = {

    case StartSocket(userId) =>
      log.debug(s"start new socket for user $userId")
      /* Get or create the tuple (Enumerator[JsValue], Channel[JsValue]) for current user. */
      /* Channel allows to write data inside a related enumerator, allowing to create WebSocket
       * or Streams around that enumerator and to write data inside.
       */
      val userChannel: UserChannel = usersSockets.getOrElse(userId, {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        UserChannel(userId, 0, broadcast._1, broadcast._2)
      })
      /* If user open more then one connection, increment just a counter instead of creating
       * another tuple (Enumerator, Channel), and returning current enumerator.
       * That way when we write in a channel, all opened WebSocket for the related user
       * receive the same data
       */
      userChannel.channelsCount = userChannel.channelsCount + 1
      usersSockets += (userId -> userChannel)

      log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      log debug s"channel count : ${usersSockets.size}"

      /* Return the enumerator related to the user channel, this will be used for create the WebSocket*/
      sender ! userChannel.enumerator

    case UpdateMap(newLogs) =>
      usersLogs.foreach {
        case (userId, logs) =>
          usersLogs += (userId -> mergeLogs(logs, newLogs))
      }

    case UpdateClient(userId) =>
      /* Writing data to the channel to send data to all WebSocket opened for every user. */
      val logs = usersLogs(userId)
      val json = Json.obj(
        "event" -> "update",
        "data" -> logs
      )
      usersSockets.get(userId).get.channel push json
      usersLogs += (userId -> Json.obj(
        "add" -> Json.arr(),
        "remove" -> Json.arr()
      ))

    case Start(userId) =>
      usersLogs += (userId -> Json.obj(
        "add" -> Json.arr(),
        "remove" -> Json.arr()
      ))
      usersSockets.get(userId).get.channel push Json.obj("event" -> "started", "data" -> Json.obj())

    case Stop(userId) =>
      removeUserLogs(userId)
      val json = Json.obj(
        "event" -> "stopped",
        "data" -> Json.obj()
      )
      usersSockets.get(userId).get.channel push json

    case SocketClosed(userId) =>
      log debug s"closed socket for $userId"

      val userChannel = usersSockets.get(userId).get
      if (userChannel.channelsCount > 1) {
        userChannel.channelsCount = userChannel.channelsCount - 1
        usersSockets += (userId -> userChannel)
        log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      } else {
        removeUserChannel(userId)
        removeUserLogs(userId)
        log debug s"removed channel and timer for $userId"
      }
  }

  def removeUserLogs(userId: Long) = usersLogs -= userId

  def removeUserChannel(userId: Long) = usersSockets -= userId

  def mergeLogs(oldLogs: JsValue, newLogs: JsValue): JsValue = {
    val jsonAdds = (oldLogs \ "add").as[List[JsValue]] ::: (newLogs \ "add").as[List[JsValue]]
    val jsonRemoves = (oldLogs \ "remove").as[List[JsValue]] ::: (newLogs \ "remove").as[List[JsValue]]
    Json.obj(
      "add" -> jsonAdds,
      "remove" -> jsonRemoves
    )
  }

}


sealed trait SocketMessage

case class StartSocket(userId: Long) extends SocketMessage

case class SocketClosed(userId: Long) extends SocketMessage

case class UpdateMap(logs: JsValue) extends SocketMessage

case class UpdateClient(userId: Long) extends SocketMessage

case class Start(userId: Long) extends SocketMessage

case class Stop(userId: Long) extends SocketMessage
