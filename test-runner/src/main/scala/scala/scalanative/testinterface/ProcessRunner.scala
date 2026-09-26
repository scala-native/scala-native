package scala.scalanative.testinterface

import java.io.{File, InputStream, OutputStream}

import scala.concurrent.{Future, Promise}

import scala.scalanative.build.Logger

private[testinterface] class ProcessRunner(
    executableFile: File,
    envVars: Map[String, String],
    args: Seq[String],
    logger: Logger,
    port: Int
) extends AutoCloseable {

  private val process = {
    // Optional emulator config used internally for testing non amd64 architectures
    val emulatorOpts: List[String] = sys.env
      .get("CROSSCOMPILING_EMULATOR")
      .map(_.split(" ").toList)
      .filter(_.nonEmpty)
      .getOrElse(Nil)

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
        .redirectInput(ProcessBuilder.Redirect.INHERIT)

    envVars.foreach {
      case (k, v) =>
        builder.environment().put(k, v)
    }

    logger.info(s"Starting process '$executableFile' on port '$port'.")
    builder.start()
  }
  private val outputThreads = List(
    PipeOutputThread.start(process.getErrorStream(), System.err),
    PipeOutputThread.start(process.getInputStream(), System.out)
  )

  private val runnerPromise: Promise[Unit] = Promise[Unit]()
  private val runner = new Thread {
    setName("TestRunner")
    override def run(): Unit = {
      val exitCode = process.waitFor()
      outputThreads.foreach(_.join())
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

private object PipeOutputThread {
  def start(from: InputStream, to: OutputStream): Thread = {
    val thread = new Thread {
      override def run(): Unit = {
        try {
          val buffer = new Array[Byte](8192)
          var byteCount = from.read(buffer)
          while (byteCount > 0) {
            to.write(buffer, 0, byteCount)
            to.flush()
            byteCount = from.read(buffer)
          }
        } finally {
          from.close()
        }
      }
    }
    thread.start()
    thread
  }
}
