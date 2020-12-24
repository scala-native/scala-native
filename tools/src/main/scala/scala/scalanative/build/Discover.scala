package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.util.Try
import scala.sys.process._
import scalanative.build.IO.RichPath

/** Utilities for discovery of command-line tools and settings required
 *  to build Scala Native applications.
 */
object Discover {

  /** Compilation mode name from SCALANATIVE_MODE env var or default. */
  def mode(): Mode =
    getenv("SCALANATIVE_MODE").map(build.Mode(_)).getOrElse(build.Mode.default)

  def optimize(): Boolean =
    getenv("SCALANATIVE_OPTIMIZE").forall(_.toBoolean)

  /** LTO variant used for release mode from SCALANATIVE_LTO env var or default. */
  def LTO(): LTO =
    getenv("SCALANATIVE_LTO").map(build.LTO(_)).getOrElse(build.LTO.None)

  /** GC variant used from SCALANATIVE_GC env var or default. */
  def GC(): GC =
    getenv("SCALANATIVE_GC").map(build.GC(_)).getOrElse(build.GC.default)

  /** Find the newest compatible clang binary. */
  def clang(): Path = {
    val path = discover("clang", clangVersions)
    checkThatClangIsRecentEnough(path)
    path
  }

  /** Find the newest compatible clang++ binary. */
  def clangpp(): Path = {
    val path = discover("clang++", clangVersions)
    checkThatClangIsRecentEnough(path)
    path
  }

  private def filterExisting(paths: Seq[String]): Seq[String] =
    paths.filter(new File(_).exists())

  /** Find default clang compilation options. */
  def compileOptions(): Seq[String] = {
    val includes = {
      val llvmIncludeDir =
        Try(Process("llvm-config --includedir").lineStream_!.toSeq)
          .getOrElse(Seq.empty)

      val includeDirs =
        getenv("SCALANATIVE_INCLUDE_DIRS")
          .map(_.split(File.pathSeparatorChar).toSeq)
          .getOrElse(
            filterExisting(Seq("/usr/local/include", "/opt/local/include")))

      (includeDirs ++ llvmIncludeDir).map(s => s"-I$s")
    }
    includes :+ "-Qunused-arguments"
  }

  /** Find default options passed to the system's native linker. */
  def linkingOptions(): Seq[String] = {
    val libs = {
      val llvmLibDir =
        Try(Process("llvm-config --libdir").lineStream_!.toSeq)
          .getOrElse(Seq.empty)

      val libDirs =
        getenv("SCALANATIVE_LIB_DIRS")
          .map(_.split(File.pathSeparatorChar).toSeq)
          .getOrElse(filterExisting(Seq("/usr/local/lib", "/opt/local/lib")))

      (libDirs ++ llvmLibDir).map(s => s"-L$s")
    }
    libs
  }

  /** Tests whether the clang compiler is recent enough.
   *  It is determined through looking up a built-in #define which
   *  is more reliable than testing for a specific version.
   */
  private[scalanative] def checkThatClangIsRecentEnough(
      pathToClangBinary: Path): Unit = {
    def maybePath(p: Path) = p match {
      case path if Files.exists(path) => Some(path.abs)
      case none                       => None
    }

    def definesBuiltIn(
        pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Process(Seq("echo", "")) #| Process(Seq(clang, "-dM", "-E", "-"))
      def splitIntoLines(s: String): Array[String] =
        s.split(f"%n")
      def removeLeadingDefine(s: String): String =
        s.substring(s.indexOf(' ') + 1)

      for {
        clang <- pathToClangBinary
        output = commandLineToListBuiltInDefines(clang).!!
        lines  = splitIntoLines(output)
      } yield lines map removeLeadingDefine
    }

    val clang                = maybePath(pathToClangBinary)
    val defines: Seq[String] = definesBuiltIn(clang).to[Seq].flatten
    val clangIsRecentEnough =
      defines.contains("__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__")

    if (!clangIsRecentEnough) {
      throw new BuildException(
        s"No recent installation of clang found " +
          s"at $pathToClangBinary.\nSee http://scala-native.readthedocs.io" +
          s"/en/latest/user/setup.html for details.")
    }
  }

  /** Versions of clang which are known to work with Scala Native. */
  private[scalanative] val clangVersions =
    Seq(
      ("11", ""),
      ("10", ""),
      ("9", ""),
      ("8", ""),
      ("7", ""),
      ("6", "0"),
      ("5", "0")
    )

  /** Discover concrete binary path using command name and
   *  a sequence of potential supported versions.
   */
  private[scalanative] def discover(
      binaryName: String,
      binaryVersions: Seq[(String, String)]): Path = {
    val docSetup =
      "http://www.scala-native.org/en/latest/user/setup.html"

    val envName =
      if (binaryName == "clang") "CLANG"
      else if (binaryName == "clang++") "CLANGPP"
      else binaryName

    sys.env.get(s"${envName}_PATH") match {
      case Some(path) => Paths.get(path)
      case None => {
        val binaryNames = binaryVersions.flatMap {
          case (major, minor) =>
            val sep = if (minor == "") "" else "."
            Seq(s"$binaryName$major$minor", s"$binaryName-$major${sep}$minor")
        } :+ binaryName

        Process("which" +: binaryNames)
          .lineStream_!(silentLogger())
          .map(Paths.get(_))
          .headOption
          .getOrElse {
            throw new BuildException(
              s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang ($docSetup)")
          }
      }
    }
  }

  private def silentLogger(): ProcessLogger =
    ProcessLogger(_ => (), _ => ())

  private def getenv(key: String): Option[String] =
    Option(System.getenv.get(key))
}
