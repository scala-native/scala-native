// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest.scalanative

import java.io.File

import scala.tools.partest.nest.{DirectCompiler, SuiteRunner}
import scala.tools.partest.{FileOps, nest}

class ScalaNativeRunner(
    testFile: File,
    suiteRunner: SuiteRunner,
    scalaNativeOverridePath: String,
    options: ScalaNativePartestOptions
) extends nest.Runner(
      testFile,
      suiteRunner,
      new nest.NestUI(diffOnFail = options.showDiff, colorEnabled = true)
    ) {

  override val checkFile: File = {
    scalaNativeConfigFile("check") getOrElse {
      // this is super.checkFile, but apparently we can't do that
      new FileOps(testFile).changeExtension("check")
    }
  }

  private def scalaNativeConfigFile(ext: String): Option[File] = {
    val overrideFile = s"$scalaNativeOverridePath/$kind/$fileBase.$ext"
    val url = getClass.getResource(overrideFile)
    Option(url).map(url => new File(url.toURI))
  }

  override def newCompiler = {
    new DirectCompiler(this) with ScalaNativeDirectCompiler
  }

  override def extraJavaOptions = {
    super.extraJavaOptions ++ options.javaOptions
  }
}
