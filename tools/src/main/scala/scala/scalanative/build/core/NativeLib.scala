package scala.scalanative
package build
package core

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Arrays
import java.util.regex._

import scalanative.build.LLVM._

/** Original jar or dir path and generated dir path for native code */
private[scalanative] case class NativeLib(src: Path, dest: Path)

/** Utilities for dealing with native library code */
private[scalanative] object NativeLib {

  /** Name of directory that contains native code: "scala-native"
   */
  val nativeCodeDir = "scala-native"

  /** Finds all the native libs on the classpath.
   *
   *  The method generates a unique directory for each classpath entry that has
   *  native source.
   *
   *  @param classpath
   *    The classpath
   *  @param workdir
   *    The base working directory
   *  @return
   *    The Seq of NativeLib objects
   */
  def findNativeLibs(classpath: Seq[Path], workdir: Path): Seq[NativeLib] = {
    val nativeCodePrefix = "native-code"

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
            .stripSuffix(jarExt)
        NativeLib(
          src = path,
          dest = workdir.resolve(s"$nativeCodePrefix-$name-$index")
        )
      }

    if (extractPaths.isEmpty)
      throw new BuildException(
        s"No Scala Native libraries were found: $classpath"
      )

    if (Files.exists(workdir)) {
      // Fix https://github.com/scala-native/scala-native/pull/2998#discussion_r1023715815
      // Remove all stale native-code-* directories. These can be created if classpath would change
      val expectedPaths = extractPaths.map(_.dest.toAbsolutePath()).toSet
      val nativeCodePattern = raw"$nativeCodePrefix-.*-\d+"
      Files
        .list(workdir)
        .forEach(new java.util.function.Consumer[Path] {
          def accept(path: Path): Unit = {
            def matchesPattern =
              path.getFileName().toString().matches(nativeCodePattern)
            def notIgnored =
              expectedPaths.contains(path.toAbsolutePath())

            if (matchesPattern && notIgnored) {
              IO.deleteRecursive(path)
            }
          }
        })
    }

    extractPaths
  }

  /** Find the native file paths for this native library
   *
   *  @param destPath
   *    The native lib dest path
   *  @return
   *    All file paths to compile
   */
  def findNativePaths(workdir: Path, destPath: Path): Seq[Path] = {
    val srcPatterns = destSrcPattern(workdir, destPath)
    IO.getAll(workdir, srcPatterns)
  }

  /** The linker uses the VirtualDirectory which is sensitive to empty
   *  directories in the classpath or anything other than a jar file.
   *
   *  Issue #911: Linking fails in a pure testing project with source only in
   *  src/test/scala.
   *
   *  Issue #1711: Files put in the lib directory in sbt such as somelib.so will
   *  end up on the classpath. Linking fails if the entries on the classpath are
   *  not either jars or directories.
   *
   *  @param classpath
   *    Build tool classpath
   *  @return
   *    Filtered classpath for Scala Native tools
   */
  def filterClasspath(classpath: Seq[Path]): Seq[Path] =
    classpath.filter(p => Files.exists(p) && (isJar(p) || Files.isDirectory(p)))

  /** Called to unpack jars and copy native code.
   *
   *  @param nativelib
   *    the native lib to copy/unpack
   *  @return
   *    The destination path of the directory
   */
  def unpackNativeCode(nativelib: NativeLib): Path =
    if (isJar(nativelib)) unpackNativeJar(nativelib)
    else copyNativeDir(nativelib)

  /** Unpack the `src` Jar Path to `workdir/dest` where `dest` is the generated
   *  directory where the Scala Native lib or a third party library that
   *  includes native code is copied.
   *
   *  If the same archive has already been unpacked to this location and hasn't
   *  changed, this call has no effect.
   *
   *  @param nativelib
   *    The NativeLib to unpack.
   *  @return
   *    The Path where the nativelib has been unpacked, `workdir/dest`.
   */
  private def unpackNativeJar(nativelib: NativeLib): Path = {
    val target = nativelib.dest
    val source = nativelib.src
    val jarhash = IO.sha1(source)
    val jarhashPath = target.resolve("jarhash")
    def unpacked =
      Files.exists(target) &&
        Files.exists(jarhashPath) &&
        Arrays.equals(jarhash, Files.readAllBytes(jarhashPath))

    if (!unpacked) {
      IO.deleteRecursive(target)
      IO.unzip(source, target)
      IO.write(jarhashPath, jarhash)
    }
    target
  }

  /** Copy project code from project `src` Path to `workdir/dest` Path where it
   *  can be compiled and linked.
   *
   *  This does not copy if no native code has changed.
   *
   *  @param nativelib
   *    The NativeLib to copy.
   *  @return
   *    The Path where the code was copied, `workdir/dest`.
   */
  private def copyNativeDir(nativelib: NativeLib): Path = {
    val target = nativelib.dest
    val source = nativelib.src
    val files = IO.getAll(source, allFilesPattern(source))
    val fileshash = IO.sha1files(files)
    val fileshashPath = target.resolve("fileshash")
    def copied =
      Files.exists(target) &&
        Files.exists(fileshashPath) &&
        Arrays.equals(fileshash, Files.readAllBytes(fileshashPath))
    if (!copied) {
      IO.deleteRecursive(target)
      IO.copyDirectory(source, target)
      IO.write(fileshashPath, fileshash)
    }
    target
  }

  /** Java Archive extension: ".jar" */
  private val jarExt = ".jar"

  /** Does this Path point to a jar file */
  private def isJar(path: Path): Boolean =
    path.toString().endsWith(jarExt)

  /** Is this NativeLib in a jar file */
  private def isJar(nativelib: NativeLib): Boolean = isJar(nativelib.src)

  /** Used to find native source files in jar files */
  private val jarSrcRegex: String = {
    val regexExtensions = srcExtensions.mkString("""(\""", """|\""", ")")
    // Paths in jars always contains '/' separator instead of OS specific one.
    s"""^$nativeCodeDir/(.+)$regexExtensions$$"""
  }

  private val jarPattern = Pattern.compile(jarSrcRegex)

  private def isNativeFile(name: String): Boolean =
    jarPattern.matcher(name).matches()

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

  /** Used to find native source files in directories */
  private def srcPatterns(path: Path): String =
    srcExtensions.mkString(s"glob:${srcPathPattern(path)}**{", ",", "}")

  private def srcPathPattern(path: Path): String =
    makeDirPath(path, nativeCodeDir)

  /** Used to create hash of the directory to copy
   *
   *  @param path
   *    The classpath entry
   *  @return
   *    The file pattern
   */
  private def allFilesPattern(path: Path): String =
    s"glob:${srcPathPattern(path)}**"

  /** This method guarantees that only code copied and generated into the
   *  `native` directory and also in the `scala-native` sub directory gets
   *  picked up for compilation.
   *
   *  @param workdir
   *    The base working directory
   *  @param destPath
   *    The dest Path to the native lib
   *  @return
   *    The source pattern
   */
  private def destSrcPattern(workdir: Path, destPath: Path): String = {
    val dirPattern = s"{${destPath.getFileName()}}"
    val pathPat = makeDirPath(workdir, dirPattern, nativeCodeDir)
    srcExtensions.mkString(s"glob:$pathPat**{", ",", "}")
  }

  private def makeDirPath(path: Path, elems: String*): String = {
    val pathSep = if (Platform.isWindows) raw"\\" else File.separator

    (path.toString.replace(File.separator, pathSep) +: elems)
      .mkString("", pathSep, pathSep)
  }
}
