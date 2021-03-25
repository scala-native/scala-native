package scala.scalanative.build

import java.nio.file.Path
import scalanative.build.IO.RichPath
import scalanative.build.{Platform => BuildPlatform}

object NativePathsExtractor {
  private val dirSeparator             = java.io.File.separatorChar
  private final val SharedDirSeparator = '-'

  object Platform extends DirectoryExtractor("platform")
  object Optional extends DirectoryExtractor("optional")
  object GC       extends DirectoryExtractor("gc")
  object GCShared extends SharedPathExtractor("gc")

  object PosixLib extends DirectoryExtractor("posix")

  object Shared extends SharedPathExtractor()

  object File {
    def unapply(filename: String): Option[(String, Option[String])] =
      filename.split('.') match {
        case Array(filename)      => Some(filename, None)
        case Array(filename, ext) => Some(filename, Some(ext))
        case _                    => None
      }
  }

  sealed abstract class PathExtractor(regexExts: Seq[String]) {
    val regexPathSeparator =
      if (BuildPlatform.isWindows) raw"\\"
      else dirSeparator.toString()

    // Based on fact that all native sources end-up in directory with
    // common pattern in /target/scala-*/native/native-code-*/scala-native/
    val NativeSourcePattern = {
      Seq(".*",
          "native",
          NativeLib.nativeDirectoryPrefix + "-.*",
          NativeLib.codeDir) ++ regexExts :+ "(.*)"
    }.mkString(regexPathSeparator).r
  }

  sealed abstract class DirectoryExtractor(relativePath: String*)
      extends PathExtractor(relativePath) {
    type DirectoryAndRelativeSegments = (String, List[String])

    def unapply(path: Path): Option[DirectoryAndRelativeSegments] =
      unapply(path.abs)

    def unapply(pathAbs: String): Option[DirectoryAndRelativeSegments] = {
      pathAbs match {
        case NativeSourcePattern(relativePath) =>
          val directory = pathAbs.stripSuffix(relativePath)
          val segments = relativePath
            .split(dirSeparator)
            .filterNot(_.isEmpty)
            .toList
          Some(directory -> segments)
        case _ => None
      }
    }
  }

  sealed abstract class SharedPathExtractor(inPaths: String*)
      extends PathExtractor(inPaths :+ raw"shared_?([\w\d-]+)?") {
    type DirectoryAndSharedBy = (String, Option[List[String]])
    
    def unapply(path: Path): Option[DirectoryAndSharedBy] =
      unapply(path.abs)

    def unapply(pathAbs: String): Option[DirectoryAndSharedBy] = {
      pathAbs match {
        case NativeSourcePattern(sharedBy, relativePath) =>
          val directoryPath = pathAbs.stripSuffix(relativePath)
          val sharedWith = Option(sharedBy)
            .map(_.split(SharedDirSeparator))
            .map(_.toList)
          Some(directoryPath -> sharedWith)

        case _ => None
      }
    }
  }

}
