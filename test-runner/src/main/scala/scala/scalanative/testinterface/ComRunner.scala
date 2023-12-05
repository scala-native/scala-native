package scala.scalanative
package testinterface

// Ported from Scala.js

import java.io._
import java.net.{ServerSocket, Socket}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalanative.build.Logger
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/** Represents a distant program with whom we communicate over the network.
 *  @param logger
 *    Logger to log to.
 */
private[testinterface] class ComRunner(
    processRunner: ProcessRunner,
    serverSocket: ServerSocket,
    logger: Logger,
    handleMessage: String => Unit
)(implicit ec: ExecutionContext) extends AutoCloseable {
  import ComRunner._

  processRunner.future.onComplete {
    case Failure(exception) => forceClose(exception)
    case Success(_)         => onNativeTerminated()
  }

  @volatile
  private[this] var state: State = AwaitingConnection(Nil)

  private[this] val promise: Promise[Unit] = Promise[Unit]()

  // TODO replace this with scheduled tasks on the execution context.
  new Thread {
    setName("ComRunner receiver")
    override def run(): Unit = {
      try {
        try {

          /* We need to await the connection unconditionally. Otherwise the Native end
           * might try to connect indefinitely. */
          awaitConnection()

          while (state != Closing) {
            state match {
              case s: AwaitingConnection =>
                throw new IllegalStateException(s"Unexpected state: $s")

              case Closing =>
                /* We can end up here if there is a race between the two read to
                 * state. Do nothing, loop will terminate.
                 */
                ()
              case Connected(_, _, native2jvm) =>
                try {
                  val len = native2jvm.readInt()
                  val carr = Array.fill(len)(native2jvm.readChar())
                  handleMessage(String.valueOf(carr))
                } catch {
                  // Native end terminated gracefully. Close.
                  case _: EOFException => close()
                }
            }
          }
        } catch {
          // We got interrupted by a graceful close. This is OK.
          case _: IOException if state == Closing => ()
        }

        /*
         * Everything got closed. We wait for the run to terminate.
         * We need to wait in order to make sure that closing the
         * underlying run does not fail it. */
        processRunner.future.foreach { _ =>
          processRunner.close()
          promise.trySuccess(())
        }
      } catch {
        case t: Throwable => handleThrowable(t)
      }
    }
  }.start()

  val future: Future[Unit] = promise.future

  def send(msg: String): Unit = synchronized {
    state match {
      case AwaitingConnection(msgs) =>
        state = AwaitingConnection(msg :: msgs)

      case Connected(_, jvm2native, _) =>
        try {
          writeMsg(jvm2native, msg)
          jvm2native.flush()
        } catch {
          case t: Throwable => handleThrowable(t)
        }

      case Closing => () // ignore msg.
    }
  }

  def close(): Unit = synchronized {
    val oldState = state
    state =
      Closing // Signal receiver thread that it is OK if socket read fails.
    oldState match {
      case c: Connected =>
        // Interrupts the receiver thread and signals the VM to terminate.
        closeAll(c)
      case Closing | _: AwaitingConnection => ()
    }
  }

  private def onNativeTerminated(): Unit = {
    close()

    /*
     * Interrupt receiver if we are still waiting for connection.
     * Should only be relevant if we are still awaiting the connection.
     * Note: We cannot do this in close(), otherwise if the JVM side closes
     * before the Native side connected, the Native VM will fail instead of terminate
     * normally.
     */
    serverSocket.close()
  }

  private def forceClose(cause: Throwable): Unit = {
    logger.warn(s"Force close $cause")
    promise.tryFailure(cause)
    close()
    processRunner.close()
    serverSocket.close()
  }

  private def handleThrowable(cause: Throwable): Unit = {
    forceClose(cause)
    if (!NonFatal(cause))
      throw cause
  }

  private def awaitConnection(): Unit = {
    var comSocket: Socket = null
    var jvm2native: DataOutputStream = null
    var native2jvm: DataInputStream = null

    try {
      serverSocket.setSoTimeout(40 * 1000)
      comSocket = serverSocket.accept()
      serverSocket.close() // we don't need it anymore.
      jvm2native = new DataOutputStream(
        new BufferedOutputStream(comSocket.getOutputStream)
      )
      native2jvm = new DataInputStream(
        new BufferedInputStream(comSocket.getInputStream)
      )

      onConnected(Connected(comSocket, jvm2native, native2jvm))
    } catch {
      case t: Throwable =>
        closeAll(comSocket, jvm2native, native2jvm)
        throw t
    }
  }

  private def onConnected(c: Connected): Unit = synchronized {
    state match {
      case AwaitingConnection(msgs) =>
        msgs.reverse.foreach(writeMsg(c.jvm2native, _))
        c.jvm2native.flush()
        state = c

      case _: Connected =>
        throw new IllegalStateException(s"Unexpected state: $state")

      case Closing => closeAll(c)
    }
  }
}

private[testinterface] object ComRunner {
  private def closeAll(c: Closeable*): Unit =
    c.withFilter(_ != null).foreach(_.close())

  private def closeAll(c: Connected): Unit =
    closeAll(c.comSocket, c.jvm2native, c.native2jvm)

  private sealed trait State

  private final case class AwaitingConnection(sendQueue: List[String])
      extends State

  private final case class Connected(
      comSocket: Socket,
      jvm2native: DataOutputStream,
      native2jvm: DataInputStream
  ) extends State

  private case object Closing extends State

  private def writeMsg(s: DataOutputStream, msg: String): Unit = {
    s.writeInt(msg.length)
    s.writeChars(msg)
  }
}
