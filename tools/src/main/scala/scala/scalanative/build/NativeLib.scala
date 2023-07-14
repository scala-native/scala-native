package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Arrays
import java.util.regex._

import scala.concurrent._
import scala.util.Failure
import scala.util.Success

import scalanative.build.IO.RichPath
import scala.scalanative.linker.ReachabilityAnalysis

/** Original jar or dir path and generated dir path for native code */
private[scalanative] case class NativeLib(src: Path, dest: Path)

/** Utilities for dealing with native library code */
private[scalanative] object NativeLib {

  /** Name of directory that contains native code: "scala-native" */
  val nativeCodeDir = "scala-native"

  /** Project Descriptor properties file: "scala-native.properties" */
  private val nativeProjectProps = s"${nativeCodeDir}.properties"

  /** Compiles the native code from the library
   *
   *  @param config
   *    the configuration options
   *  @param linkerResult
   *    needed for configuration based on NIR
   *  @param nativeLib
   *    the native lib to unpack
   *  @return
   *    the paths to the objects
   */
  def compileNativeLibrary(
      config: Config,
      analysis: ReachabilityAnalysis.Result,
      nativeLib: NativeLib
  )(implicit ec: ExecutionContext): Future[Seq[Path]] = {
    val destPath = unpackNativeCode(nativeLib)
    val paths = findNativePaths(config.workDir, destPath)
    val projConfig = configureNativelib(config, analysis, destPath)
    LLVM.compile(projConfig, paths)
  }

  /** Update the project configuration the if a project `Descriptor` is present.
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param linkerResult
   *    The results from the linker.
   *  @param destPath
   *    The unpacked location of the Scala Native nativelib.
   *  @return
   *    The config for this native library.
   */
  private def configureNativelib(
      config: Config,
      analysis: ReachabilityAnalysis.Result,
      destPath: Path
  ): Config = {
    val nativeCodePath = destPath.resolve(nativeCodeDir)
    // check if filtering is needed, o.w. return all paths
    findDescriptor(nativeCodePath).fold(config) { filepath =>

      val desc =
        Descriptor.load(filepath) match {
          case Success(v) => v
          case Failure(e) =>
            throw new BuildException(
              s"Problem reading $nativeProjectProps: ${e.getMessage}"
            )
        }

      config.logger.debug(desc.toString())

      val projectConfig = desc match {
        case Descriptor(Some(groupId), Some(artifactId), _)
            if (groupId == "org.scala-native" && artifactId == "nativelib") =>
          createGcConfig(nativeCodePath, config)
        case Descriptor(_, _, _) =>
          createLinkConfig(desc, analysis, config)
      }

      projectConfig
    }
  }

  private def createLinkConfig(
      desc: Descriptor,
      analysis: ReachabilityAnalysis.Result,
      config: Config
  ): Config = {
    val linkDefines =
      desc.link
        .filter(name => analysis.links.exists(_.name == name))
        .map(name => s"-DSCALANATIVE_LINK_${name.toUpperCase}")

    config.withCompilerConfig(
      _.withCompileOptions(_ ++ linkDefines)
    )
  }

  private def createGcConfig(
      nativeCodePath: Path,
      config: Config
  ): Config = {
    /* A conditional compilation define is used to compile the
     * correct garbage collector code because code is shared.
     * This avoids handling all the paths needed and compiling
     * all the GC code for a given platform.
     *
     * Note: The zone directory is also part of the garbage collection
     * system and shares code from the gc directory.
     */
    val gcFlag = {
      val gc = config.compilerConfig.gc.toString
      s"-DSCALANATIVE_GC_${gc.toUpperCase}"
    }

    val gcPath = nativeCodePath.resolve("gc").abs

    config.withCompilerConfig(
      _.withCompileOptions(_ :+ ("-I" + gcPath) :+ gcFlag)
    )
  }

  /** Check for compile Descriptor in destination native code directory.
   *
   *  @param nativeCodePath
   *    The native code directory
   *  @return
   *    The optional path to the file or none
   */
  private def findDescriptor(nativeCodePath: Path): Option[Path] = {
    val file = nativeCodePath.resolve(nativeProjectProps)
    if (Files.exists(file)) Some(file)
    else None
  }

  /** Finds all the native libs on the classpath.
   *
   *  The method generates a unique directory for each classpath entry that has
   *  native source.
   *
   *  @param classpath
   *    The classpath
   *  @param workDir
   *    The base working directory
   *  @return
   *    The Seq of NativeLib objects
   */
  def findNativeLibs(config: Config): Seq[NativeLib] = {
    val workDir = config.workDir
    val classpath = config.classPath
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
          dest = workDir.resolve(s"$nativeCodePrefix-$name-$index")
        )
      }

    if (extractPaths.isEmpty)
      throw new BuildException(
        s"No Scala Native libraries were found: $classpath"
      )

    if (Files.exists(workDir)) {
      // Fix https://github.com/scala-native/scala-native/pull/2998#discussion_r1023715815
      // Remove all stale native-code-* directories. These can be created if classpath would change
      val expectedPaths = extractPaths.map(_.dest.toAbsolutePath()).toSet
      val nativeCodePattern = raw"$nativeCodePrefix-.*-\d+"
      Files
        .list(workDir)
        .filter(_.getFileName().toString() matches nativeCodePattern)
        .filter(p => !expectedPaths.contains(p.toAbsolutePath()))
        .forEach(IO.deleteRecursive(_))
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
  private def findNativePaths(workDir: Path, destPath: Path): Seq[Path] = {
    val srcPatterns = destSrcPattern(workDir, destPath)
    IO.getAll(workDir, srcPatterns)
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

  /** Called to unpack jars and copy native code. Creates a hash of the jar or
   *  directory and then only replaces the code when changes occur. The function
   *  does a full replace and not a diff change. This makes sure deleted files
   *  are removed from the work area.
   *
   *  @param nativelib
   *    the native lib directory/jar to copy/unpack respectively
   *  @return
   *    The destination path of the directory
   */
  private def unpackNativeCode(nativelib: NativeLib): Path =
    if (isJar(nativelib)) unpackNativeJar(nativelib)
    else copyNativeDir(nativelib)

  /** Unpack the `src` Jar Path to `workDir/dest` where `dest` is the generated
   *  directory where the Scala Native lib or a third party library that
   *  includes native code is copied.
   *
   *  If the same archive has already been unpacked to this location and hasn't
   *  changed, this call has no effect.
   *
   *  @param nativelib
   *    The NativeLib to unpack.
   *  @return
   *    The Path where the nativelib has been unpacked, `workDir/dest`.
   */
  private def unpackNativeJar(nativelib: NativeLib): Path = {
    val target = nativelib.dest
    val source = nativelib.src
    def unpack(): Unit = {
      IO.deleteRecursive(target)
      IO.unzip(source, target)
    }

    if (Platform.isJVM) {
      val jarhash = IO.sha1(source)
      val jarhashPath = target.resolve("jarhash")
      def unpacked =
        Files.exists(target) &&
          Files.exists(jarhashPath) &&
          Arrays.equals(jarhash, Files.readAllBytes(jarhashPath))
      if (!unpacked) {
        unpack()
        IO.write(jarhashPath, jarhash)
      }
    } else unpack()
    target
  }

  /** Copy project code from project `src` Path to `workDir/dest` Path where it
   *  can be compiled and linked.
   *
   *  This does not copy if no native code has changed.
   *
   *  @param nativelib
   *    The NativeLib to copy.
   *  @return
   *    The Path where the code was copied, `workDir/dest`.
   */
  private def copyNativeDir(nativelib: NativeLib): Path = {
    val target = nativelib.dest
    val source = nativelib.src
    def copy() = {
      IO.deleteRecursive(target)
      IO.copyDirectory(source, target)
    }
    if (Platform.isJVM) {
      val files = IO.getAll(source, allFilesPattern(source))
      val fileshash = IO.sha1files(files)
      val fileshashPath = target.resolve("fileshash")
      def copied =
        Files.exists(target) &&
          Files.exists(fileshashPath) &&
          Arrays.equals(fileshash, Files.readAllBytes(fileshashPath))
      if (!copied) {
        copy()
        IO.write(fileshashPath, fileshash)
      }
    } else copy()
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
    val regexExtensions = LLVM.srcExtensions.mkString("""(\""", """|\""", ")")
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
    LLVM.srcExtensions.mkString(s"glob:${srcPathPattern(path)}**{", ",", "}")

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
   *  @param workDir
   *    The base working directory
   *  @param destPath
   *    The dest Path to the native lib
   *  @return
   *    The source pattern
   */
  private def destSrcPattern(workDir: Path, destPath: Path): String = {
    val dirPattern = s"{${destPath.getFileName()}}"
    val pathPat = makeDirPath(workDir, dirPattern, nativeCodeDir)
    LLVM.srcExtensions.mkString(s"glob:$pathPat**{", ",", "}")
  }

  private def makeDirPath(path: Path, elems: String*): String = {
    val pathSep = if (Platform.isWindows) raw"\\" else File.separator

    (path.toString.replace(File.separator, pathSep) +: elems)
      .mkString("", pathSep, pathSep)
  }
}
