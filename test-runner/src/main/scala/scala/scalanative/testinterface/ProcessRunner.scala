package scala.scalanative.testinterface

import java.io.File
import scala.concurrent.{Future, Promise}
import scala.scalanative.build.Logger

private[testinterface] class ProcessRunner(executableFile: File,
                                           envVars: Map[String, String],
                                           args: Seq[String],
                                           logger: Logger,
                                           port: Int)
    extends AutoCloseable {

  private[this] val process = {
    val builder =
      new ProcessBuilder(executableFile.toString +: port.toString +: args: _*)
        .inheritIO()

    envVars.foreach {
      case (k, v) =>
        builder.environment().put(k, v)
    }

    logger.info(s"Starting process '$executableFile' on port '$port'.")
    builder.start()
  }

  private[this] val runnerPromise: Promise[Unit] = Promise[Unit]()
  private[this] val runner = new Thread {
    setName("TestRunner")
    override def run(): Unit = {
      val exitCode = process.waitFor()
      if (exitCode == 0) runnerPromise.trySuccess(())
      else {
        runnerPromise.tryFailure(
          new RuntimeException(
            s"Process $executableFile finished with non-zero value $exitCode"))
      }
    }
  }
  runner.start()
  val future: Future[Unit] = runnerPromise.future

  override def close(): Unit = {
    process.destroyForcibly()
  }
}
