package scala.scalanative
package testinterface

import java.io._
import java.net.{ServerSocket, Socket, SocketTimeoutException}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalanative.build.{BuildException, Logger}
import scala.sys.process._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
 * Represents a distant program with whom we communicate over the network.
 * @param bin    The program to run
 * @param args   Arguments to pass to the program
 * @param logger Logger to log to.
 */
class ComRunner(bin: File,
                envVars: Map[String, String],
                args: Seq[String],
                logger: Logger,
                handleMessage: String => Unit) {
  import ComRunner._
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  @volatile
  private[this] var state: State = AwaitingConnection(Nil)

  private[this] val runPromise: Promise[Unit] = Promise[Unit]()
  private[this] val runner = new Thread {
    override def run(): Unit = {
      val port = serverSocket.getLocalPort
      logger.info(s"Starting process '$bin' on port '$port'.")

      val exitCode =
        Process(command = bin.toString +: port.toString +: args,
                cwd = None,
                extraEnv = envVars.toSeq: _*) ! Logger.toProcessLogger(logger)

      if (exitCode == 0) runPromise.trySuccess(())
      else {
        runPromise.tryFailure(
          new RuntimeException(
            s"Process $bin finished with non-zero value $exitCode"))
      }
    }
  }

  val future: Future[Unit] = runPromise.future

  private[this] val serverSocket: ServerSocket = new ServerSocket(
    /* port = */ 0,
    /* backlog = */ 1
  )
  runner.start()

  // If the run completes, make sure we also complete.
  runPromise.future.onComplete {
    case Failure(t) => forceClose(t)
    case Success(_) => onNativeTerminated()
  }

  // TODO replace this with scheduled tasks on the execution context.
  private[this] val receiver = new Thread {
    setName("ComRunner receiver")

    override def run(): Unit = {
      try {
        try {

          /** We need to await the connection unconditionally. Otherwise the JS end
           * might try to connect indefinitely. */
          awaitConnection()

          while (state != Closing) {
            state match {
              case s: AwaitingConnection =>
                throw new IllegalStateException(s"Unexpected state: $s")

              case Closing =>
              /** We can end up here if there is a race between the two read to
               * state. Do nothing, loop will terminate.
               */
              case Connected(_, _, native2jvm) =>
                try {
                  val len  = native2jvm.readInt()
                  val carr = Array.fill(len)(native2jvm.readChar())
                  handleMessage(String.valueOf(carr))
                } catch {
                  // JS end terminated gracefully. Close.
                  case _: EOFException => close()
                }
            }
          }
        } catch {
          // We got interrupted by a graceful close. This is OK.
          case _: IOException if state == Closing => ()
        }

        /**
         * Everything got closed. We wait for the run to terminate.
         * We need to wait in order to make sure that closing the
         * underlying run does not fail it. */
        runPromise.future.foreach { _ => close() }
      } catch {
        case t: Throwable => handleThrowable(t)
      }
    }
  }

  receiver.start()

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
    state = Closing // Signal receiver thread that it is OK if socket read fails.

    oldState match {
      case c: Connected =>
        closeAll(
          c
        ) // Interrupts the receiver thread and signals the VM to terminate.
      case Closing | _: AwaitingConnection => ()
    }
  }

  private def onNativeTerminated(): Unit = {
    close()

    /**
     * Interrupt receiver if we are still waiting for connection.
     * Should only be relevant if we are still awaiting the connection.
     * Note: We cannot do this in close(), otherwise if the JVM side closes
     * before the JS side connected, the JS VM will fail instead of terminate
     * normally.
     */
    serverSocket.close()
  }

  private def forceClose(cause: Throwable): Unit = {
    logger.warn(s"Force close $cause")
    runPromise.tryFailure(cause)
    close()
    serverSocket.close()
  }

  private def handleThrowable(cause: Throwable): Unit = {
    forceClose(cause)
    if (!NonFatal(cause))
      throw cause
  }

  private def awaitConnection(): Unit = {
    var comSocket: Socket            = null
    var jvm2native: DataOutputStream = null
    var native2jvm: DataInputStream  = null

    try {
      logger.info("Awaiting connection")
      serverSocket.setSoTimeout(40 * 1000)
      comSocket = serverSocket.accept()
      serverSocket.close() // we don't need it anymore.
      jvm2native = new DataOutputStream(
        new BufferedOutputStream(comSocket.getOutputStream))
      native2jvm = new DataInputStream(
        new BufferedInputStream(comSocket.getInputStream))

      onConnected(Connected(comSocket, jvm2native, native2jvm))
    } catch {
      case _: SocketTimeoutException =>
        val ex = new BuildException(
          "The test program never connected to the test runner.")
        runPromise.tryFailure(ex)
        throw ex
      case t: Throwable =>
        closeAll(comSocket, jvm2native, native2jvm)
        throw t
    }
  }

  private def onConnected(c: Connected): Unit = synchronized {
    logger.info("Connected")
    state match {
      case AwaitingConnection(msgs) =>
        msgs.reverse.foreach(writeMsg(c.jvm2native, _))
        c.jvm2native.flush()
        state = c

      case _: Connected =>
        throw new IllegalStateException(s"Unexpected state: $state")

      case Closing =>
        closeAll(c)
    }
  }
}

object ComRunner {
  private def closeAll(c: Closeable*): Unit =
    c.withFilter(_ != null).foreach(_.close())

  private def closeAll(c: Connected): Unit =
    closeAll(c.comSocket, c.jvm2native, c.native2jvm)

  private sealed trait State

  private final case class AwaitingConnection(sendQueue: List[String])
      extends State

  private final case class Connected(comSocket: Socket,
                                     jvm2native: DataOutputStream,
                                     native2jvm: DataInputStream)
      extends State

  private final case object Closing extends State

  private def writeMsg(s: DataOutputStream, msg: String): Unit = {
    s.writeInt(msg.length)
    s.writeChars(msg)
  }
}
