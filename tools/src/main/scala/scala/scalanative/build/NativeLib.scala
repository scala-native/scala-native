package scala.scalanative
package build

import java.io.File
import java.nio.file.Path
import java.util.regex._

/** Original jar or dir path and generated dir path for native code */
private[scalanative] case class NativeLib(src: Path, dest: Path)

private[scalanative] object NativeLib {
  val fs           = File.separator
  val jarExtension = ".jar"

  /** Object file extension: ".o" */
  val oExt = ".o"

  /** C++ file extension: ".cpp" */
  val cppExt = ".cpp"

  /** List of source patterns used: ".c, .cpp, .S" */
  val srcExtensions = Seq(".c", cppExt, ".S")

  /**
   * Name of directory that contains native code: "scala-native"
   */
  val codeDir = "scala-native"

  /** Used to find native source files in directories */
  val srcPatterns = srcExtensions.mkString(
    s"glob:**${fs}classes${fs}${codeDir}${fs}**{",
    ",",
    "}")

  /**
   * This method guarantees that only code copied and generated
   * into the `native` directory and also in the `scala-native`
   * sub directory gets picked up for compilation.
   *
   * Note: this assumes that the `workdir` ends with `native`.
   *
   * @param genDir the generated directory for `scalalib` or `*` for all dirs
   * @return the source pattern
   */
  def destSrcPatterns(genDir: String = "*"): String =
    srcExtensions.mkString(
      s"glob:**${fs}native${fs}${genDir}${fs}${codeDir}${fs}**{",
      ",",
      "}")

  /** Used to find native source files in jar files */
  private def jarRegex(): String = {
    val regexExtensions = srcExtensions.mkString("""(\""", """|\""", ")")
    s"""^${codeDir}${fs}(.+)${regexExtensions}$$"""
  }

  val jarPattern = Pattern.compile(jarRegex())

  /** To positively identify nativelib */
  private val nativeLibMarkerFile = "org_scala-native_nativelib.txt"

  /** Note: assumes that code is compiled into the `classes` directory */
  private val dirMarkerFilePattern =
    s"glob:**${fs}classes${fs}" + nativeLibMarkerFile

  /** Does this Path point to a jar file */
  def isJar(path: Path): Boolean = path.toString().endsWith(jarExtension)

  /** Is this NativeLib in a jar file */
  def isJar(nativelib: NativeLib): Boolean = isJar(nativelib.src)

  /**
   * Finds all the native libs on the classpath.
   *
   * The method generates a unique directory for each classpath
   * entry that has native source.
   *
   * @param classpath the classpath
   * @param workdir the base working directory
   * @return the Seq of NativeLib objects
   */
  def findNativeLibs(classpath: Seq[Path], workdir: Path): Seq[NativeLib] = {
    val nativeLibPaths = classpath.flatMap { path =>
      if (isJar(path)) readJar(path)
      else readDir(path)
    }

    val extractPaths =
      for ((path, index) <- nativeLibPaths.zipWithIndex) yield {
        val name =
          path
            .getFileName()
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

  /**
   * Find the Scala Native `nativelib` from within all the
   * other libraries with native code.
   *
   * @param nativeLibs - the Seq of discovered native libs
   * @return the Scala Native `nativelib`
   */
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
    jarPattern.matcher(name).matches()

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
