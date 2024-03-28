package scala.scalanative
package testinterface

import java.io.{DataInputStream, DataOutputStream, EOFException}
import java.net.Socket
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.scalanative.testinterface.common.RPCCore
import scala.util.{Failure, Success, Try}
import java.nio.charset.StandardCharsets
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/** Native RPC Core. */
private[testinterface] class NativeRPC(clientSocket: Socket)(implicit
    ec: ExecutionContext
) extends RPCCore {
  private lazy val inStream = new DataInputStream(clientSocket.getInputStream)
  private lazy val outStream = new DataOutputStream(
    clientSocket.getOutputStream
  )

  override def send(msg: String): Unit = {
    outStream.writeInt(msg.length)
    outStream.write(msg.getBytes(StandardCharsets.UTF_16BE))
  }

  @tailrec
  private[testinterface] final def loop(): Int = {
    val msgLength =
      try {
        inStream.readInt()
      } catch {
        case _: EOFException => 0 // leave loop
      }

    if (msgLength <= 0) {
      0 // Always 0, all errors reported by Exception.
    } else {
      val msg = Array.fill(msgLength)(inStream.readChar).mkString
      handleMessage(msg)
      // We cannot control which ExecutionContext implementation is used by users
      // Run the queue execution context loop just to be sure we don't create deadlock
      runtime.loop()
      loop()
    }
  }
}
