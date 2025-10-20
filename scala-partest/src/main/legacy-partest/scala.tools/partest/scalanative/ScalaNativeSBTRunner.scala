// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest
package scalanative

import _root_.sbt.testing.*
import java.io.File
import java.net.URLClassLoader
import scala.tools.partest.nest.*
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
  System.setProperty("partest.root", testRoot.getAbsolutePath())

  // Partests take at least 5h. We double, just to be sure. (default is 4 hours)
  System.setProperty("partest.timeout", "10 hours")

  override val suiteRunner =
    new SuiteRunner(
      testSourcePath = config.optSourcePath orElse Option(
        "test/files"
      ) getOrElse PartestDefaults.sourcePath,
      fileManager = new FileManager(testClassLoader = testClassLoader),
      updateCheck = config.optUpdateCheck,
      failed = config.optFailed,
      nestUI = nestUI,
      javaCmdPath = Option(javaCmd)
        .map(_.getAbsolutePath) getOrElse PartestDefaults.javaCmd,
      javacCmdPath = Option(javacCmd)
        .map(_.getAbsolutePath) getOrElse PartestDefaults.javacCmd,
      scalacExtraArgs = scalacArgs,
      javaOpts = javaOpts
    ) with ScalaNativeSuiteRunner {

      val options: ScalaNativePartestOptions = ScalaNativeSBTRunner.this.options
      val scalaVersion: String = ScalaNativeSBTRunner.this.scalaVersion

      override def onFinishTest(
          testFile: File,
          result: TestState
      ): TestState = {
        eventHandler.handle(new Event {
          def fullyQualifiedName: String = testFile.testIdent
          def fingerprint: Fingerprint = partestFingerprint
          def selector: Selector = new TestSelector(testFile.testIdent)
          val (status, throwable) = makeStatus(result)
          def duration: Long = -1L
        })
        result
      }
    }
}
