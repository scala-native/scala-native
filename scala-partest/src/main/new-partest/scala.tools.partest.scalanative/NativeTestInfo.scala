package scala.tools.partest.scalanative

import java.io.File
import java.nio.file.Files
import scala.tools.partest.FileOps
import scala.tools.partest.nest.TestInfo

class NativeTestInfo(testFile: File, scalaNativeOverridePath: String)
    extends TestInfo(testFile) {
  override val checkFile: File = {
    val overrideFile =
      s"$scalaNativeOverridePath/$kind/$fileBase.check"
    Option(getClass.getResource(overrideFile))
      .map { url =>
        try new File(url.toURI)
        catch {
          case _: Exception =>
            // URI points to inner JAR, read content and store it in temp file
            val tempFile = Files.createTempFile(fileBase, ".check")
            val input = scala.io.Source.fromInputStream(
              getClass.getResourceAsStream(overrideFile)
            )
            try Files.write(tempFile, input.mkString.getBytes())
            finally input.close()
            tempFile.toFile
        }
      }
      .getOrElse {
        // this is super.checkFile, but apparently we can't do that
        new FileOps(testFile).changeExtension("check")
      }
  }
}
