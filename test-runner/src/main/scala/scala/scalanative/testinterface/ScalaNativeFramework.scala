package scala.scalanative
package testinterface

import java.io.File

import sbt.testing.{Fingerprint, Framework, Runner}

import scala.scalanative.build.Logger

class ScalaNativeFramework(val framework: Framework,
                           val id: Int,
                           logger: Logger,
                           testBinary: File,
                           envVars: Map[String, String])
    extends Framework {

  private[this] var _runner: ScalaNativeRunner = _

  override def name(): String                     = framework.name()
  override def fingerprints(): Array[Fingerprint] = framework.fingerprints()

  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): Runner = {
    if (_runner != null) {
      throw new IllegalStateException(
        "Scala Native test frameworks do not support concurrent runs")
    }

    _runner =
      new ScalaNativeRunner(this, testBinary, logger, envVars, args, remoteArgs)
    _runner
  }

  private[testinterface] def runDone(): Unit = _runner = null

  /**
   * Temporarily commented out.
   *
   * The name and fingerprints are no longer obtained via the Runner,
   * but via the enclosed Framework object (which is a NativeFramework).
   * This is required, since we removed the NativeTest SBT configuration
   * (it is now basically just the Test configuration) and the Runner is
   * not available at the moment the above attributes are requested.
   *
   * So, we use the NativeFramework from the JVM to obtain them.
   * This is not very elegant, but does the job for the time being.
   * We should refactor this in a less ugly solution, similar to what
   * Scala.js does with its testing adapter.
   */
  //private def fetchFrameworkInfo(): FrameworkInfo = {
  //  _runner.send(Command.SendInfo(id, None))
  //  val Command.SendInfo(_, Some(infos)) = _runner.receive()
  //  infos
  //}
}
