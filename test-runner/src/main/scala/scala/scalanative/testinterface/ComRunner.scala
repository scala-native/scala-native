package scala.scalanative
package testinterface

import java.io._
import java.net.{ServerSocket, SocketTimeoutException}

import scala.annotation.tailrec
import scala.sys.process._

import scalanative.build.{BuildException, Logger}
import scalanative.testinterface.serialization.Log.Level
import scalanative.testinterface.serialization._

/**
 * Represents a distant program with whom we communicate over the network.
 * @param bin    The program to run
 * @param args   Arguments to pass to the program
 * @param logger Logger to log to.
 */
class ComRunner(bin: File,
                envVars: Map[String, String],
                args: Seq[String],
                logger: Logger) {

  private[this] val runner = new Thread {
    override def run(): Unit = {
      val port = serverSocket.getLocalPort
      logger.info(s"Starting process '$bin' on port '$port'.")
      Process(bin.toString +: port.toString +: args, None, envVars.toSeq: _*) ! Logger
        .toProcessLogger(logger)
    }
  }

  private[this] var serverSocket: ServerSocket = _
  private[this] val socket =
    try {
      serverSocket = new ServerSocket( /* port = */ 0, /* backlog = */ 1)

      runner.start()

      serverSocket.setSoTimeout(40 * 1000)
      serverSocket.accept()
    } catch {
      case _: SocketTimeoutException =>
        throw new BuildException(
          "The test program never connected to the test runner.")
    } finally {
      // We can close it immediately, since we won't receive another connection.
      serverSocket.close()
    }

  private[this] val in = new DataInputStream(
    new BufferedInputStream(socket.getInputStream))
  private[this] val out = new DataOutputStream(
    new BufferedOutputStream(socket.getOutputStream))

  /** Send message `msg` to the distant program. */
  def send(msg: Message): Unit = {
    synchronized { // Here, not at def, to workaround SN Issue #1091.
      try SerializedOutputStream(out)(_.writeMessage(msg))
      catch {
        case ex: Throwable =>
          close()
          throw ex
      }
    }
  }

  /** Wait for a message to arrive from the distant program. */
  def receive(): Message = {
    @tailrec
    def loop(): Message = {
      SerializedInputStream.next(in)(_.readMessage()) match {
        case logMsg: Log =>
          log(logMsg)
          loop()
        case other =>
          other
      }
    }

    synchronized { // Here, not at def, to workaround SN Issue #1091.
      try {
        loop()
      } catch {
        case _: EOFException =>
          close()
          throw new BuildException(
            s"EOF on connection with remote runner on port ${serverSocket.getLocalPort}")
        case ex: Throwable =>
          close()
          throw ex
      }
    }
  }

  def close(): Unit = {
    in.close()
    out.close()
    socket.close()
  }

  private def log(message: Log): Unit =
    message.level match {
      case Level.Info  => logger.info(message.message)
      case Level.Warn  => logger.warn(message.message)
      case Level.Error => logger.error(message.message)
      case Level.Trace =>
        logger.debug(message.message)
        message.throwable.foreach { t =>
          logger.debug(t.getMessage)
          t.getStackTrace.foreach(ste => logger.debug(s"\t$ste"))
        }
      case Level.Debug => logger.debug(message.message)
    }

}
