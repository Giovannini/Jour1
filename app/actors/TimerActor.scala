package actors

import play.api.libs.json._
import play.api.libs.json.Json._

import akka.actor.Actor

import play.api.libs.iteratee.{Concurrent, Enumerator}

import play.api.libs.iteratee.Concurrent.Channel
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._

/**
 * User: Luigi Antonini
 * Date: 19/07/13
 * Time: 15.38
 */
class TimerActor extends Actor {

  // crate a scheduler to send a message to this actor every socket
  val cancellable = context.system.scheduler.schedule(0 second, 1 second, self, UpdateTime())

  case class UserChannel(userId: Int, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

  lazy val log = Logger("application." + this.getClass.getName)

  var webSockets = Map[Int, UserChannel]()// Map relating every user with his UserChannel

  var usersTimes = Map[Int, Int]()// Map relating every user with his current time

  override def receive = {
    case StartSocket(userId) =>
      log.debug(s"start new socket for user $userId")
      /*
       * Get current user channel in the map if exists
       * else create a new one for him
       */
      //NOOBS NOTE:
      /*
       Channel is very useful class, it allows to write data inside its related enumerator,
       allowing to create WebSocket or Streams around that enumerator and write data inside
       using its related Channel */
      val userChannel: UserChannel = webSockets.get(userId).getOrElse {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        UserChannel(userId, 0, broadcast._1, broadcast._2)
      }

      /* If user open more than one connection, increment just a counter instead of create
         another tuple (Enumerator, Channel), and return current enumerator.
         That way when we write in the channel,
         all opened WebSocket of that user receive the same data */
      userChannel.channelsCount = userChannel.channelsCount + 1
      webSockets += (userId -> userChannel)

      log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      log debug s"channel count : ${webSockets.size}"

      /* Return the enumerator related to the user channel, this will be used to create the WebSocket. */
      sender ! userChannel.enumerator

    case UpdateTime() =>
      usersTimes.foreach {
        case (userId, millis) =>
          usersTimes += (userId -> (millis + 1000))
          val json = Map("data" -> toJson(millis))

          // writing data to the channel will send data to all WebSocket opened for every user
          webSockets.get(userId).get.channel push Json.toJson(json)
      }


    case Start(userId) =>
      usersTimes += (userId -> 0)

    case Stop(userId) =>
      removeUserTimer(userId)
      val json = Map("data" -> toJson(0))
      webSockets.get(userId).get.channel push Json.toJson(json)

    case SocketClosed(userId) =>
      log debug s"closed socket for $userId"
      val userChannel = webSockets.get(userId).get

      if (userChannel.channelsCount > 1) {
        userChannel.channelsCount = userChannel.channelsCount - 1
        webSockets += (userId -> userChannel)
        log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      } else {
        removeUserChannel(userId)
        removeUserTimer(userId)
        log debug s"removed channel and timer for $userId"
      }

  }

  def removeUserTimer(userId: Int) = usersTimes -= userId
  def removeUserChannel(userId: Int) = webSockets -= userId

}


sealed trait SocketMessage

case class StartSocket(userId: Int) extends SocketMessage

case class SocketClosed(userId: Int) extends SocketMessage

case class UpdateTime() extends SocketMessage

case class Start(userId: Int) extends SocketMessage

case class Stop(userId: Int) extends SocketMessage

