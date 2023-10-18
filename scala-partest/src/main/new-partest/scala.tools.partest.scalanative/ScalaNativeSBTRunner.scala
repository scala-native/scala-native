// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest
package scalanative

import _root_.sbt.testing._
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.TimeUnit
import scala.tools.partest.nest._
import scala.tools.partest.sbt.SBTRunner

/* Pre-mixin ScalaNativeSuiteRunner in SBTRunner, because this is looked up
 * via reflection from the sbt partest interface of Scala Native
 */
class ScalaNativeSBTRunner(
    partestFingerprint: Fingerprint,
    eventHandler: EventHandler,
    loggers: Array[Logger],
    testRoot: File,
    testClassLoader: URLClassLoader,
    javaCmd: File,
    javacCmd: File,
    scalacArgs: Array[String],
    args: Array[String],
    val options: ScalaNativePartestOptions,
    val scalaVersion: String
) extends SBTRunner(
      RunnerSpec.forArgs(args),
      partestFingerprint,
      eventHandler,
      loggers,
      "test/files",
      testClassLoader,
      javaCmd,
      javacCmd,
      scalacArgs,
      args
    ) {

  // The test root for partest is read out through the system properties,
  // not passed as an argument
  System.setProperty("partest.root", testRoot.getAbsolutePath)

  // Partests take at least 5h. We double, just to be sure. (default is 4 hours)
  System.setProperty("partest.timeout", "10 hours")

  // Stuff to mix in

  // Stuff we provide

  override def banner: String = {
    import scala.scalanative.nir.Versions.{current => currentVersion}

    super.banner.trim + s"""
   |Scala Native version is: $currentVersion
   |${options.show}
   |""".stripMargin
  }

  override def runTest(testFile: File): TestState = {
    // Mostly copy-pasted from SuiteRunner.runTest(), unfortunately :-(
    val start = System.nanoTime()
    val info = new NativeTestInfo(testFile, listDir)
    val runner = new ScalaNativeRunner(info, this, options)
    var stopwatchDuration: Option[Long] = None

    // when option "--failed" is provided execute test only if log
    // is present (which means it failed before)
    val state =
      if (config.optFailed && !info.logFile.canRead)
        runner.genPass()
      else {
        val (state, durationMs) =
          try runner.run()
          catch {
            case t: Throwable =>
              throw new RuntimeException(s"Error running $testFile", t)
          }
        stopwatchDuration = Some(durationMs)
        val more = reportTest(
          state,
          info,
          durationMs,
          diffOnFail = config.optShowDiff || options.showDiff,
          logOnFail = config.optShowLog
        )
        runner.cleanup(state)
        if (more.isEmpty) state
        else {
          state match {
            case f: TestState.Fail => f.copy(transcript = more.toArray)
            case _                 => state
          }
        }
      }
    val end = System.nanoTime()
    val durationMs =
      stopwatchDuration.getOrElse(TimeUnit.NANOSECONDS.toMillis(end - start))
    onFinishTest(testFile, state, durationMs)
  }

  override def runTestsForFiles(
      kindFiles: Array[File],
      kind: String
  ): Array[TestState] = {
    super.runTestsForFiles(kindFiles.filter(testFilter), kind)
  }

  private lazy val listDir =
    s"/scala/tools/partest/scalanative/$scalaVersion"

  private lazy val denylistedTests = {
    val source = scala.io.Source
      .fromURL(getClass.getResource(s"$listDir/DenylistedTests.txt"))

    val files = for {
      line <- source.getLines()
      trimmed = line.trim
      if trimmed != "" && !trimmed.startsWith("#")
    } yield {
      extendShortTestName(trimmed)
    }

    files.toSet
  }

  private def extendShortTestName(testName: String): File = {
    val f = (pathSettings.srcDir / testName).jfile
    require(f.exists(), s"$testName does not exist")
    f
  }

  private lazy val testFilter: File => Boolean = {
    import ScalaNativePartestOptions._
    options.testFilter match {
      case DenylistedTests  => denylistedTests
      case AllowlistedTests => n => !denylistedTests.contains(n)
      case SomeTests(names) => names.map(extendShortTestName).toSet
    }
  }
}
