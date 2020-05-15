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
}
