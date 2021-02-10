package scala.tools.partest.scalanative

import java.io.File
import scala.tools.partest.FileOps
import scala.tools.partest.nest.TestInfo

class NativeTestInfo(testFile: File, scalaNativeOverridePath: String)
    extends TestInfo(testFile) {
  override val checkFile: File = {
    val overrideFile =
      s"$scalaNativeOverridePath/$kind/$fileBase.check"
    val url = getClass.getResource(overrideFile)
    Option(url).map(url => new File(url.toURI)).getOrElse {
      // this is super.checkFile, but apparently we can't do that
      new FileOps(testFile).changeExtension("check")
    }
  }
}
