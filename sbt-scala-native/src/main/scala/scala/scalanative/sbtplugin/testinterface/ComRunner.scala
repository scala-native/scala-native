package scala.scalanative
package sbtplugin
package testinterface

import java.io.{Serializable => _, _}

import sbt.{Logger, MessageOnlyException}

import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration
import scala.compat.Platform.EOL
import scala.scalanative.testinterface.serialization._
import Serializer.EitherSerializer
import java.net.{ConnectException, Socket, SocketTimeoutException}

import scala.scalanative.testinterface.serialization.Log.Level

/**
 * Represents a distant program with whom we communicate over the network.
 * @param bin    The program to run
 * @param args   Arguments to pass to the program
 * @param logger Logger to log to.
 */
class ComRunner(bin: File, args: Seq[String], logger: Logger) {

  /** Port over which we communicate with the distant program */
  val port: Int = scala.util.Random.nextInt(1000) + 9000

  private[this] val runner = new Thread {
    override def run(): Unit = {
      import sbt.Process._
      running = true
      logger.info(s"Starting process '$bin' on port '$port'.")
      Seq(bin.getAbsolutePath, port.toString) ++ args ! logger
      running = false
    }
  }

  private[this] var running: Boolean = false

  runner.start()

  private[this] val socket = getSocket(retries = 5)
  private[this] val in = new DataInputStream(
    new BufferedInputStream(socket.getInputStream))
  private[this] val out = new DataOutputStream(
    new BufferedOutputStream(socket.getOutputStream))

  /** Send message `v` to the distant program. */
  def send[T: Serializable](v: T): Unit = synchronized {
    val msg = Serializer.serialize(v).mkString(EOL).getBytes("UTF-8")
    out.writeInt(msg.length)
    out.write(msg)
    out.flush()
  }

  /** Wait for a message to arrive from the distant program. */
  def receive[T: Serializable](timeout: Duration = Duration.Inf): T =
    synchronized {
      in.mark(Int.MaxValue)
      val savedSoTimeout = socket.getSoTimeout()
      try {
        val deadLineMs = if (timeout.isFinite()) timeout.toMillis else 0L
        socket.setSoTimeout((deadLineMs min Int.MaxValue).toInt)

        val msgLen  = in.readInt()
        val buf     = new Array[Byte](msgLen)
        var readLen = 0
        while (readLen < msgLen) {
          socket.setSoTimeout((deadLineMs min Int.MaxValue).toInt)
          readLen += in.read(buf, readLen, msgLen - readLen)
        }

        in.mark(0)

        Serializer.deserialize[Either[Log, T]](new String(buf, "UTF-8").lines) match {
          case Left(logMsg) =>
            log(logMsg)
            receive[T](timeout)

          case Right(msg) =>
            msg
        }

      } catch {
        case _: EOFException =>
          throw new MessageOnlyException(
            s"EOF on connection with remote runner on port $port")
        case _: SocketTimeoutException =>
          in.reset()
          throw new TimeoutException("Timeout expired")
      } finally {
        socket.setSoTimeout(savedSoTimeout)
      }
    }

  def close(): Unit = {
    in.close()
    out.close()
    socket.close()
  }

  private[this] def getSocket(retries: Int): Socket =
    if (retries < 0)
      throw new Exception("Couldn't communicate with remote runner.")
    else {
      try new Socket("localhost", port)
      catch {
        case _: ConnectException =>
          Thread.sleep(100)
          getSocket(retries - 1)
      }
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
