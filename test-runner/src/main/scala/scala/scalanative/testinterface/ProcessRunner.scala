package scala.scalanative.testinterface

import java.io.File
import scala.concurrent.{Future, Promise}
import scala.scalanative.build.Logger

private[testinterface] class ProcessRunner(
    executableFile: File,
    envVars: Map[String, String],
    args: Seq[String],
    logger: Logger,
    port: Int
) extends AutoCloseable {

  private[this] val process = {
    // Optional emualator config used internally for testing non amd64 architectures
    val emulatorOpts: List[String] = {
      val optEmulator =
        sys.props
          .get("scala.scalanative.testinterface.processrunner.emulator")
          .filter(_.nonEmpty)
      val optEmulatorOptions = sys.props
        .get("scala.scalanative.testinterface.processrunner.emulator-args")
        .map(_.split(" ").toList)
        .getOrElse(Nil)
      optEmulator.toList ++ optEmulatorOptions
    }
    if (emulatorOpts.nonEmpty) {
      logger.info(s"Using test process emulator: ${emulatorOpts.mkString(" ")}")
    }

    val builder =
      new ProcessBuilder(
        emulatorOpts ++:
          executableFile.getAbsolutePath() +:
          port.toString +:
          args: _*
      )
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
            s"Process $executableFile finished with non-zero value $exitCode (0x${exitCode.toHexString})"
          )
        )
        // Similarly to Bash programs, exitcode values higher
        // than 128 signify program end by fatal signal
        if (exitCode > 128)
          logger.error(
            s"Test runner interrupted by fatal signal ${exitCode - 128}"
          )
      }
    }
  }
  runner.start()
  val future: Future[Unit] = runnerPromise.future

  override def close(): Unit = {
    process.destroyForcibly()
  }
}
