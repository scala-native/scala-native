package scala.scalanative
package junit

// Ported from Scala.js

import sbt.testing._

final class JUnitFramework extends Framework {

  val name: String = "Scala Native JUnit test framework"

  private object JUnitFingerprint extends AnnotatedFingerprint {
    override def annotationName(): String = "org.junit.Test"

    override def isModule(): Boolean = false
  }

  def fingerprints(): Array[Fingerprint] = {
    Array(JUnitFingerprint)
  }

  def runner(args: Array[String],
             remoteArgs: Array[String],
             testClassLoader: ClassLoader): Runner = {
    new JUnitRunner(args, remoteArgs, parseRunSettings(args))
  }

  /** Scala Native specific: Creates a worker for a given run.
   *  Ported from Scala.js
   *  The worker may send a message to the controller runner by calling `send`.
   *
   *  Important: this method name cannot be changed.
   *  It must keep the name `slaveRunner` for compatibility with Scala.js.
   *  This is a public to-be-overridden API that testing frameworks must implement.
   *  Currently testing frameworks can have a unique implementation of Framework for JVM, JS and Native.
   *  If we change that method name, they will need a separate source file for Native.
   *
   *  Similarly, Scala.js cannot change the name of that method in its API either,
   *  since that would be a binary breaking that would break all published testing frameworks, which is not acceptable.
   *
   *  Introducing another method and deprecating this one as a forwarder is not an option,
   *  because testing frameworks must implement that method, not call it.)
   */
  def slaveRunner(args: Array[String],
                  remoteArgs: Array[String],
                  testClassLoader: ClassLoader,
                  send: String => Unit): Runner = {
    new JUnitRunner(args, remoteArgs, parseRunSettings(args))
  }

  private def parseRunSettings(args: Array[String]): RunSettings = {
    var verbose              = false
    var noColor              = false
    var decodeScalaNames     = false
    var logAssert            = false
    var notLogExceptionClass = false
    for (str <- args) {
      str match {
        case "-v" => verbose = true
        case "-n" => noColor = true
        case "-s" => decodeScalaNames = true
        case "-a" => logAssert = true
        case "-c" => notLogExceptionClass = true

        case s if s.startsWith("-tests=") =>
          throw new UnsupportedOperationException("-tests")

        case s if s.startsWith("--tests=") =>
          throw new UnsupportedOperationException("--tests")

        case s if s.startsWith("--ignore-runners=") =>
          throw new UnsupportedOperationException("--ignore-runners")

        case s if s.startsWith("--run-listener=") =>
          throw new UnsupportedOperationException("--run-listener")

        case s if s.startsWith("--include-categories=") =>
          throw new UnsupportedOperationException("--include-categories")

        case s if s.startsWith("--exclude-categories=") =>
          throw new UnsupportedOperationException("--exclude-categories")

        case s if s.startsWith("-D") && s.contains("=") =>
          throw new UnsupportedOperationException("-Dkey=value")

        case s if !s.startsWith("-") && !s.startsWith("+") =>
          throw new UnsupportedOperationException(s)

        case _ =>
      }
    }
    for (s <- args) {
      s match {
        case "+v" => verbose = false
        case "+n" => noColor = false
        case "+s" => decodeScalaNames = false
        case "+a" => logAssert = false
        case "+c" => notLogExceptionClass = false
        case _    =>
      }
    }
    new RunSettings(!noColor,
                    decodeScalaNames,
                    verbose,
                    logAssert,
                    notLogExceptionClass)
  }
}
