package scala.scalanative
package sbtplugin
package testinterface

import java.io._

import sbt.{Logger, MessageOnlyException, Process}

import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration
import scala.scalanative.testinterface.serialization._
import java.net.{ServerSocket, SocketTimeoutException}

import scala.scalanative.testinterface.serialization.Log.Level

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
      import sbt.Process._
      val port = serverSocket.getLocalPort
      logger.info(s"Starting process '$bin' on port '$port'.")
      Process(bin.toString +: port.toString +: args, None, envVars.toSeq: _*) ! logger
    }
  }

  private[this] var serverSocket: ServerSocket = _
  private[this] val socket =
    try {
      serverSocket = new ServerSocket(0)

      runner.start()

      serverSocket.setSoTimeout(30 * 1000)
      serverSocket.accept()
    } catch {
      case _: SocketTimeoutException =>
        throw new MessageOnlyException(
          "The test program never connected to sbt.")
    } finally {
      // We can close it immediately, since we won't receive another connection.
      serverSocket.close()
    }

  private[this] val in = new DataInputStream(
    new BufferedInputStream(socket.getInputStream))
  private[this] val out = new DataOutputStream(
    new BufferedOutputStream(socket.getOutputStream))

  /** Send message `msg` to the distant program. */
  def send(msg: Message): Unit = synchronized {
    try SerializedOutputStream(out)(_.writeMessage(msg))
    catch {
      case ex: Throwable =>
        close()
        throw ex
    }
  }

  /** Wait for a message to arrive from the distant program. */
  def receive(timeout: Duration = Duration.Inf): Message =
    synchronized {
      in.mark(Int.MaxValue)
      val savedSoTimeout = socket.getSoTimeout()
      try {
        val deadLineMs = if (timeout.isFinite()) timeout.toMillis else 0L
        socket.setSoTimeout((deadLineMs min Int.MaxValue).toInt)

        val result =
          SerializedInputStream.next(in)(_.readMessage()) match {
            case logMsg: Log =>
              log(logMsg)
              receive(timeout)
            case other =>
              other
          }

        in.mark(0)
        result

      } catch {
        case _: EOFException =>
          close()
          throw new MessageOnlyException(
            s"EOF on connection with remote runner on port ${serverSocket.getLocalPort}")
        case _: SocketTimeoutException =>
          close()
          throw new TimeoutException("Timeout expired")
        case ex: Throwable =>
          close()
          throw ex
      } finally {
        socket.setSoTimeout(savedSoTimeout)
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
      case Level.Trace => message.throwable.foreach(logger.trace(_))
      case Level.Debug => logger.debug(message.message)
    }

}
