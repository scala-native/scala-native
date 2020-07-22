package scala.scalanative
package build

import java.nio.file.Path

/** Original jar or dir path and generated dir path for native code */
private[scalanative] case class NativeLib(src: Path, dest: Path)

private[scalanative] object NativeLib {

  /** List of source patterns used */
  val jarExtension  = ".jar"
  val srcExtensions = Seq(".c", ".cpp", ".S", ".h", ".hpp")
  val srcPatterns   = srcExtensions.mkString("glob:**{", ",", "}")

  /** To positively identify nativelib */
  val nativeLibMarkerFile = "org_scala-native_nativelib.txt"

  val dirMarkerFilePattern = "glob:**" + nativeLibMarkerFile

  def isJar(path: Path): Boolean = path.toString().endsWith(jarExtension)

  def isJar(nativelib: NativeLib): Boolean = isJar(nativelib.src)

  def findNativeLibs(classpath: Seq[Path], workdir: Path): Seq[NativeLib] = {
    val nativeLibPaths = classpath.flatMap { path =>
      if (isJar(path)) readJar(path)
      else readDir(path)
    }

    val extractPaths =
      for ((path, index) <- nativeLibPaths.zipWithIndex) yield {
        val name =
          path
            .getName(path.getNameCount() - 1)
            .toString()
            .stripSuffix(jarExtension)
        NativeLib(src = path,
                  dest = workdir.resolve(s"native-code-$name-$index"))
      }

    if (extractPaths.isEmpty)
      throw new BuildException(
        s"No Scala Native libraries were found: $classpath")
    else
      extractPaths
  }

  def findNativeLib(nativeLibs: Seq[NativeLib]): Path = {
    val nativeLib = nativeLibs.find { nl =>
      val srcPath = nl.src
      if (isJar(srcPath))
        IO.existsInJar(srcPath, hasMarkerFileInJar)
      else
        IO.existsInDir(srcPath, dirMarkerFilePattern)
    }
    nativeLib match {
      case Some(nl) => nl.dest
      case None =>
        throw new BuildException(
          s"Native Library 'nativelib' not found: $nativeLibs")
    }
  }

  private def isNativeFile(name: String): Boolean =
    srcExtensions.exists(name.endsWith(_))

  private def hasMarkerFileInJar(name: String): Boolean =
    name.equals(nativeLibMarkerFile)

  private def readDir(path: Path): Option[Path] =
    IO.existsInDir(path, srcPatterns) match {
      case true  => Some(path)
      case false => None
    }

  private def readJar(path: Path): Option[Path] =
    IO.existsInJar(path, isNativeFile) match {
      case true  => Some(path)
      case false => None
    }
}
