package scala.scalanative
package sbtplugin
package testinterface

import java.io.File

import sbt.Logger
import sbt.testing.{Fingerprint, Framework, Runner}

import scala.scalanative.testinterface.serialization.{Command, FrameworkInfo}

class ScalaNativeFramework(val framework: Framework,
                           val id: Int,
                           logger: Logger,
                           testBinary: File,
                           envVars: Map[String, String])
    extends Framework {

  private[this] lazy val frameworkInfo         = fetchFrameworkInfo()
  private[this] var _runner: ScalaNativeRunner = null

  override def name(): String = frameworkInfo.name
  override def fingerprints(): Array[Fingerprint] =
    frameworkInfo.fingerprints.toArray
  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): Runner = {
    if (_runner != null) {
      throw new IllegalStateException(
        "Scala Native test frameworks do not support concurrent runs")
    }

    _runner = new ScalaNativeRunner(this,
                                    testBinary,
                                    logger,
                                    envVars,
                                    args,
                                    remoteArgs)
    _runner
  }

  private[testinterface] def runDone(): Unit = _runner = null

  private def fetchFrameworkInfo(): FrameworkInfo = {
    _runner.send(Command.SendInfo(id, None))
    val Command.SendInfo(_, Some(infos)) = _runner.receive()
    infos
  }

}
