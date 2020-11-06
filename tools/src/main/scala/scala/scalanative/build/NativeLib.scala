package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path}
import java.util.regex._

/** Original jar or dir path and generated dir path for native code */
private[scalanative] case class NativeLib(src: Path, dest: Path)

/** Utilities for dealing with native library code */
private[scalanative] object NativeLib {
  private val fileSep      = File.separator
  private val jarExtension = ".jar"

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
  private def srcPatterns(path: Path): String =
    srcExtensions.mkString(s"glob:${srcPathPattern(path)}**{", ",", "}")

  /** Used to find native source files in jar files */
  private val jarSrcRegex: String = {
    val regexExtensions = srcExtensions.mkString("""(\""", """|\""", ")")
    s"""^${codeDir}${fileSep}(.+)${regexExtensions}$$"""
  }

  private def srcPathPattern(path: Path): String =
    s"${path.toString()}${fileSep}${codeDir}${fileSep}"

  /**
   * Used to create hash of the directory to copy
   *
   * @param path The classpath entry
   * @return the file pattern
   */
  def allFilesPattern(path: Path): String =
    s"glob:${srcPathPattern(path)}**"

  /**
   * This method guarantees that only code copied and generated
   * into the `native` directory and also in the `scala-native`
   * sub directory gets picked up for compilation.
   *
   * @param workdir    The base working directory
   * @param nativelibs The Paths to the native libs
   * @return the source pattern
   */
  def destSrcPatterns(workdir: Path, nativelibs: Seq[Path]): String = {
    val pathPat = destPathPattern(workdir, nativelibs)
    srcExtensions.mkString(s"glob:${pathPat}**{", ",", "}")
  }

  /**
   * Allow all the object files ".o" to be found with one
   * directory recursion.
   *
   * @param workdir    The base working directory
   * @param nativelibs The Paths to the native libs
   * @return the object file pattern
   */
  def destObjPatterns(workdir: Path, nativelibs: Seq[Path]): String =
    s"glob:${destPathPattern(workdir, nativelibs)}**${oExt}"

  private def destPathPattern(workdir: Path, nativelibs: Seq[Path]): String = {
    val workdirStr = workdir.toString()
    val nativeDirs = nativelibs.map(_.getFileName().toString())
    val dirPattern = nativeDirs.mkString("{", ",", "}")
    s"${workdirStr}${fileSep}${dirPattern}${fileSep}${codeDir}${fileSep}"
  }

  /** To positively identify nativelib */
  private val nativeLibMarkerFile = "org_scala-native_nativelib.txt"

  /**
   * Find the marker file in the directory.
   *
   * @param path The path we are searching
   * @return the search file pattern
   */
  private def dirMarkerFilePattern(path: Path): String =
    s"glob:${path.toString()}${fileSep}${nativeLibMarkerFile}"

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
        IO.existsInDir(srcPath, dirMarkerFilePattern(srcPath))
    }
    nativeLib match {
      case Some(nl) => nl.dest
      case None =>
        throw new BuildException(
          s"Native Library 'nativelib' not found: $nativeLibs")
    }
  }

  /**
   * The linker uses the VirtualDirectory which is sensitive
   * to empty directories in the classpath or anything other
   * than a jar file.
   *
   * Issue #911:
   * Linking fails in a pure testing project with source only
   * in src/test/scala.
   *
   * Issue #1711:
   * Files put in the lib directory in sbt such as somelib.so will end up
   * on the classpath. Linking fails if the entries on the classpath are
   * not either jars or directories.
   *
   * @param classpath - build tool classpath
   * @return filtered classpath for Scala Native tools
   */
  def filterClasspath(classpath: Seq[Path]): Seq[Path] =
    classpath.filter(p => Files.exists(p) && (isJar(p) || Files.isDirectory(p)))

  private val jarPattern = Pattern.compile(jarSrcRegex)

  private def isNativeFile(name: String): Boolean =
    jarPattern.matcher(name).matches()

  private def hasMarkerFileInJar(name: String): Boolean =
    name.equals(nativeLibMarkerFile)

  private def readDir(path: Path): Option[Path] =
    IO.existsInDir(path, srcPatterns(path)) match {
      case true  => Some(path)
      case false => None
    }

  private def readJar(path: Path): Option[Path] =
    IO.existsInJar(path, isNativeFile) match {
      case true  => Some(path)
      case false => None
    }
}
