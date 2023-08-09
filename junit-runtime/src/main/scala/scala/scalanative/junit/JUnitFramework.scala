package scala.scalanative
package junit

// Ported from Scala.js

import sbt.testing._

final class JUnitFramework extends Framework {

  override def name(): String = "Scala Native JUnit test framework"

  private object JUnitFingerprint extends AnnotatedFingerprint {
    override def annotationName(): String = "org.junit.Test"

    override def isModule(): Boolean = false
  }

  def fingerprints(): Array[Fingerprint] = {
    Array(JUnitFingerprint)
  }

  def runner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader
  ): Runner = {
    new JUnitRunner(args, remoteArgs, parseRunSettings(args))
  }

  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit
  ): Runner = {
    new JUnitRunner(args, remoteArgs, parseRunSettings(args))
  }

  private def parseRunSettings(args: Array[String]): RunSettings = {
    var noColor = false
    var decodeScalaNames = false
    var logAssert = true
    var logExceptionClass = true
    var verbosity: RunSettings.Verbosity = RunSettings.Verbosity.Terse

    def unsupported(name: String) =
      throw new UnsupportedOperationException(name)

      for (str <- args) str match {
        case "-v" => verbosity = RunSettings.Verbosity.Started
        case "+v" => verbosity = RunSettings.Verbosity.Terse
        case s"--verbosity=$id" =>
          verbosity = RunSettings.Verbosity.ofOrdinal(id.toInt)
        case "-n" => noColor = true
        case "-s" => decodeScalaNames = true
        case "-a" => logAssert = true
        case "-c" => logExceptionClass = false

        case "-q"                       => unsupported("-q")
        case s"--tests=$v"              => unsupported("--tests")
        case s"--ignore-runners=$v"     => unsupported("--ignore-runners")
        case s"--run-listener=$v"       => unsupported("--run-listener")
        case s"--include-categories=$v" => unsupported("--include-categories")
        case s"--exclude-categories=$v" => unsupported("--exclude-categories")
        case s"-D$key=$value"           => unsupported("-Dkey=value")
        case s"--summary=$id"           => unsupported("--summary")
        case s if !s.startsWith("-") && !s.startsWith("+") => unsupported(s)
        case _                                             => ()
      }
    for (s <- args) {
      s match {
        case "+q" => unsupported("+q")
        case "+n" => noColor = false
        case "+s" => decodeScalaNames = false
        case "+a" => logAssert = false
        case "+c" => logExceptionClass = true
        case _    => ()
      }
    }
    new RunSettings(
      !noColor,
      decodeScalaNames,
      verbosity,
      logAssert,
      logExceptionClass
    )
  }
}
