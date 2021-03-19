package scala.scalanative.build

import java.nio.file.Path
import scalanative.build.IO.RichPath
import scalanative.build.{Platform => BuildPlatform}


object NativePathsExtractor {
  private val dirSeparator       = java.io.File.separatorChar
  private val sharedDirSeparator = '-'

  object Platform extends SubpathExtractor("platform")
  object Optional extends SubpathExtractor("optional")
  object GC       extends SubpathExtractor("gc")
  object PosixLib extends SubpathExtractor("posix")

  object Shared {
    def unapply(directoryName: String): Boolean = directoryName == "shared"
  }

  object SharedBy {
    def unapply(directoryName: String): Option[Seq[String]] = {
      val sharedBy = directoryName.split(sharedDirSeparator)
      if (sharedBy.length <= 1) None
      else Some(sharedBy.toList)
    }
  }

  object File {
    def unapply(filename: String): Option[(String, Option[String])] =
      filename.split('.') match {
        case Array(filename) => Some(filename, None)
        case Array(filename, ext) => Some(filename, Some(ext))
        case _ => None
      }
  }

  sealed abstract class SubpathExtractor(paths: String*) {
    val regexPathSeparator = 
      if (BuildPlatform.isWindows) raw"\\"
      else dirSeparator.toString()

    // Based on fact that all native sources end-up in directory with
    // common pattern in /target/scala-*/native/native-code-*/scala-native/
    val NativeSourcePattern = {
      Seq(".*",
        "native",
        NativeLib.nativeDirectoryPrefix + "-.*",
        NativeLib.codeDir) ++ paths :+ "(.*)"
    }.mkString(regexPathSeparator).r

    def unapply(path: Path): Option[List[String]] = unapply(path.abs)

    def unapply(pathAbs: String): Option[List[String]] = {
      pathAbs match {
        case NativeSourcePattern(relativePath) =>
          Some(
            relativePath
              .split(dirSeparator)
              .filterNot(_.isEmpty)
              .toList)
        case _ => None
      }
    }
  }

}