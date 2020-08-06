package scala.scalanative.testinterface

import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalanative.testinterface.common.RPCCore
import scala.util.{Failure, Success, Try}

/** Native RPC Core. */
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

    private lazy val inStream  = new DataInputStream(socket.getInputStream)
    private lazy val outStream = new DataOutputStream(socket.getOutputStream)

    def init(onReceive: String => Unit): Unit = {
      if (messageHandler != null) sys.error("Com already initialized")
      messageHandler = onReceive
      messageQueue.foreach(onReceive)
    }

    def send(msg: String): Unit = {
      outStream.writeInt(msg.length)
      outStream.write(msg.getBytes("UTF-16"))
    }

    private def await(future: Future[_]): Unit = {
      scala.scalanative.runtime.loop()
      future.value.get.get
    }

    @tailrec
    def loop(): Int = {
      def tryRead: Try[Boolean] = Try {
        val msgLength = inStream.readInt()

        /**
         * Current implementation of DataInputStream does not check for EOF,
         * in this case we need to follow up base `read` behaviour which is returning -1 value to signal EOF
         * TODO Fix this after merging changes due to #1868
         */
        if (msgLength < 0) true
        else {
          val msg = Array.fill(msgLength)(inStream.readChar).mkString
          if (messageHandler == null) messageQueue.enqueue(msg)
          else
            await {
              Future.fromTry {
                Try(messageHandler(msg))
              }
            }
          false
        }
      }

      tryRead match {
        case Success(isEOF) => if (isEOF) 0 else loop()
        case Failure(exception) =>
          System.err.println(s"NativeRPC loop failed: $exception")
          -1
      }
    }
  }

}
