package scala.scalanative.testinterface

import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalanative.testinterface.common.RPCCore
import scala.util.{Failure, Success, Try}
import scalanative.junit.async._

/** Native RPC Core. Uses `scalajsCom`. */
private[testinterface] object NativeRPC extends RPCCore {
  Com.init(handleMessage)

  def loop(clientSocket: Socket): Int = {
    Com.socket = clientSocket
    Com.loop()
  }

  override protected def send(msg: String): Unit = Com.send(msg)

  private object Com {
    private[this] val messageQueue =
      scala.collection.mutable.Queue.empty[String]
    private var messageHandler: String => Unit = _
    private[NativeRPC] var socket: Socket      = _

    def init(onReceive: String => Unit): Unit = {
      if (messageHandler != null) sys.error("Com already initialized")
      messageHandler = onReceive
      messageQueue.foreach(onReceive)
    }

    def send(msg: String): Unit = {
      val outStream = new DataOutputStream(socket.getOutputStream)
      outStream.writeInt(msg.length)
      outStream.write(msg.getBytes("UTF-16"))
    }

    @tailrec
    def loop(): Int =
      if (socket.isClosed) {
        println("Socket closed")
        0
      } else
        Try {
          val inStream = new DataInputStream(socket.getInputStream)
          while (inStream.available > 4) {
            val msgLength = inStream.readInt()
            val msgBytes  = msgLength * 2

            if (inStream.available() < msgBytes) ()
            else {
              val buff = new Array[Byte](msgBytes)
              inStream.read(buff, 0, msgBytes)
              val str = new String(buff, "UTF-16")
              if (messageHandler == null) messageQueue.enqueue(str)
              else
                await {
                  Future.fromTry {
                    Try(messageHandler(str))
                  }
                }
            }
          }
        } match {
          case Failure(exception) =>
            println(s"NativeRPC loop failed: $exception"); -1
          case Success(_) =>
            Thread.sleep(100)
            loop()
        }
  }

}
