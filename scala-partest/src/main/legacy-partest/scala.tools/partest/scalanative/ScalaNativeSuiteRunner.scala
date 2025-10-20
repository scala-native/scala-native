// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest.scalanative

import java.io.File
import scala.tools.partest.nest.{PathSettings, SuiteRunner}
import scala.tools.partest.{TestState, timed}

trait ScalaNativeSuiteRunner extends SuiteRunner {

  // Stuff to mix in

  val options: ScalaNativePartestOptions

  /** Full scala version name. Used to discover denylist (etc.) files */
  val scalaVersion: String

  // Stuff we provide

  override def banner: String = {
    import scala.scalanative.nir.Versions.current as currentVersion

    super.banner.trim + s"""
    |Scala Native version is: $currentVersion
    |${options.show}
    """.stripMargin
  }

  override def runTest(testFile: File): TestState = {
    // Mostly copy-pasted from SuiteRunner.runTest(), unfortunately :-(
    val runner = new ScalaNativeRunner(testFile, this, listDir, options)

    // when option "--failed" is provided execute test only if log
    // is present (which means it failed before)
    val state =
      if (failed && !runner.logFile.canRead)
        runner.genPass()
      else {
        val (state, elapsed) =
          try timed(runner.run())
          catch {
            case t: Throwable =>
              throw new RuntimeException(s"Error running $testFile", t)
          }
        nestUI.reportTest(state, runner)
        runner.cleanup()
        state
      }
    onFinishTest(testFile, state)
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
      line <- source.getLines
      trimmed = line.trim
      if trimmed != "" && !trimmed.startsWith("#")
    } yield {
      extendShortTestName(trimmed)
    }

    files.toSet
  }

  private def extendShortTestName(testName: String): File = {
    val f = (PathSettings.srcDir / testName).jfile
    require(f.exists(), s"$testName does not exist")
    f
  }

  private lazy val testFilter: File => Boolean = {
    import ScalaNativePartestOptions.*
    options.testFilter match {
      case DenylistedTests  => denylistedTests
      case AllowlistedTests => n => !denylistedTests.contains(n)
      case SomeTests(names) => names.map(extendShortTestName _).toSet
    }
  }
}
