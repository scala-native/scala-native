package scala.scalanative
package testinterface

import java.io._
import java.net.{ServerSocket, SocketTimeoutException}

import scala.annotation.tailrec
import scala.sys.process._

import scalanative.build.Logger
import scalanative.testinterface.serialization._
import scalanative.testinterface.serialization.Log.Level

/**
 * Represents a program with whom we communicate over the network.
 * @param bin    The program to run
 * @param args   Arguments to pass to the program
 * @param logger Logger to log to.
 */
class ComRunner(bin: File,
                envVars: Map[String, String],
                args: Seq[String],
                logger: Logger) {
  private[this] class Partner() {
    var process: Option[Process] = None

    def startOn(port: Int): Unit = {
      logger.info(s"Starting process '$bin' on port '$port'.")

      val pb =
        Process(bin.toString +: port.toString +: args, None, envVars.toSeq: _*)

      val rmtLogger = ProcessLogger(
        out => System.out.printf(s"snO: ${out}\n"),
        err => System.err.printf(s"snE: ${err}\n")
      )

      this.process = Some(pb.run(rmtLogger))

      // No need to wait here for Process to exit.
      // Exit code will be synchronously checked only as needed.
      //
      // Child process will execute until either it or this process exits.
      //
      // On successful completion of Suites, the former should happen due
      // to the "RunnerDone" handshake in ScalaNativeRunner.scala.
      //
      // On grevious I/O error conditions, exception handling will flow
      // through fatalError() below and terminate the partner, if necessary.
    }

    def exitCode(timeoutSeconds: Int = 14): Option[Int] = {
      // exitCode is valuable enough that it is worthwhile waiting
      // a decent, but not infinite, interval for it to become available.
      Thread.`yield`()
      process.flatMap(p => {
        val step  = 500 // milliseconds
        var count = timeoutSeconds * 1000
        while ((count > 0) && p.isAlive()) {
          Thread.sleep(step)
          count -= step
        }
        if (count <= 0) None else Some(p.exitValue())
      })
    }

    def exit(): Unit = {
      this.exitCode() match {
        case None =>
          logger.error(
            "Partner exit code is not available: forcing partner to exit.")
          process.map(p => p.destroy())

        case Some(c) =>
          // Provide any available clues as to what might have gone wrong.
          if (c == 0)
            logger.info("Partner exited with success code: 0")
          else {
            logger.error(s"Partner exited with error code: ${c}")
            // Guess/hope that shell is Unix compatible.
            if (c > 128) {
              // This code runs in the JVM, so no easy way to call
              // strerror() to translate errno code to text. The
              // downshifted errno as a hint of what went wrong has
              // a sporting chance of being better than nothing at all.
              logger.error(s"May be Unix fatal error signal: ${c - 128}")
            }
          }
      }
    }
  }

  private[this] val partner = new Partner()

  def fatalError(message: String): Unit = {
    logger.error(message)

    partner.exit()

    logger.error("BEWARE: Inconsistent state.")
    logger.error("- Fault may be in next test in Suite containing last [ok].")
    logger.error("- Not all tests in Suites may have run.")
    logger.error("- Suite reported as having error may be wrong.")
    logger.error("- Not all Suites may have run.")
    logger.error(
      "- Reports may be more accurate if Sbt parallelExecution is false.")

    // Sbt TestFramework considers InterruptedException to be fatal and
    // will not catch it.
    throw new InterruptedException("Fatal error on partner socket")
  }

  private[this] val socket = {
    val serverSocket = new ServerSocket( /* port = */ 0, /* backlog = */ 1)
    try {
      // 40 seconds is an arbitrary small, but not too small, time to wait.
      serverSocket.setSoTimeout(40 * 1000)
      partner.startOn(serverSocket.getLocalPort)
      serverSocket.accept()
    } catch {
      case ex: SocketTimeoutException =>
        fatalError(
          "Sbt test runner timed out waiting for connection"
            + " from native program.")
        throw ex // Never gets here after fatalError(), keep compiler happy.
    } finally {
      // Close immediately; only one connection is expected.
      serverSocket.close()
    }
  }

  private[this] val in = new DataInputStream(
    new BufferedInputStream(socket.getInputStream))

  private[this] val out = new DataOutputStream(
    new BufferedOutputStream(socket.getOutputStream))

  /** Send message `msg` to the partner native program. */
  def send(msg: Message): Unit =
    synchronized {
      try SerializedOutputStream(out)(_.writeMessage(msg))
      catch {
        case ex: Throwable =>
          fatalError(
            "Fatal error while sending message" +
              " to Scala Native test program.")
          throw ex // Never gets here after fatalError(), keep compiler happy.
      }
    }

  /** Wait for a message to arrive from the partner native program. */
  def receive(): Message =
    synchronized {
      try {
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
        loop()
      } catch {
        case ex: Throwable =>
          fatalError(
            "Fatal error while receiving message" +
              " from Scala Native test program.")
          throw ex // Never gets here after fatalError(), keep compiler happy.

      }
    }

  def close(): Unit = {
    // No partner.exit() needed here. Partner should be exiting on its own
    // because sole caller has completed RunnerDone send & receive sequence.
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
