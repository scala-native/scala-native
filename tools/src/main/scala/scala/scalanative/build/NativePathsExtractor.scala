object NativePathsExtractors {
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
    def unapply(path: Path): Option[List[String]] = unapply(path.abs)

    def unapply(pathAbs: String): Option[List[String]] = {
      val regexPathSeparator = if (build.Platform.isWindows) raw"\\"
      else java.io.File.separator()
      
      // Based on fact that all native sources end-up in directory with
      // common pattern in /target/scala-*/native/native-code-*/scala-native/
      val NativeSourcePattern = {
        Seq(".*",
          "native",
          NativeLib.nativeDirectoryPrefix + "-.*",
          NativeLib.codeDir) ++ paths :+ "(.*)"
      }.mkString(regexPathSeparator).r

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