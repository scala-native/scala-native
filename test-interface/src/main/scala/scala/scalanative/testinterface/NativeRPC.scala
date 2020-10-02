package scala.scalanative.testinterface

import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalanative.testinterface.common.RPCCore
import scala.util.{Failure, Success, Try}
import java.nio.charset.StandardCharsets

/** Native RPC Core. */
private[testinterface] class NativeRPC(clientSocket: Socket) extends RPCCore {
  private lazy val inStream = new DataInputStream(clientSocket.getInputStream)
  private lazy val outStream = new DataOutputStream(
    clientSocket.getOutputStream)

  override def send(msg: String): Unit = {
    outStream.writeInt(msg.length)
    outStream.write(msg.getBytes(StandardCharsets.UTF_16BE))
  }

  @tailrec
  private[testinterface] final def loop(): Int = {
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
        handleMessage(msg)
        scalanative.runtime.loop()
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
