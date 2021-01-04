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

  /** Use the clang binary on the path or via CLANG_PATH env var. */
  def clang(): Path = {
    val path = discover("clang")
    checkThatClangIsRecentEnough(path)
    path
  }

  /** Use the clang++ binary on the path or via CLANGPP_PATH env var. */
  def clangpp(): Path = {
    val path = discover("clang++")
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
   *  Clang must be greater or equal to the minumum version and
   *  by checking for a built-in #define which is more reliable
   *  than testing for a specific version.
   */
  private[scalanative] def checkThatClangIsRecentEnough(
      pathToClangBinary: Path): Unit = {

    // this could be obsolete with our current minimum version
    def checkDefinesBuiltIn(clang: String): Unit = {
      val defineStrs = "__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__"
      def commandLineToListBuiltInDefines(clang: String) =
        Process(Seq("echo", "")) #| Process(Seq(clang, "-dM", "-E", "-"))
      def splitIntoLines(s: String): Array[String] =
        s.split(f"%n")
      def removeLeadingDefine(s: String): String =
        s.substring(s.indexOf(' ') + 1)

      val output = commandLineToListBuiltInDefines(clang).!!
      val defines: Seq[String] =
        splitIntoLines(output).map(removeLeadingDefine).to[Seq]
      if (!defines.contains(defineStrs)) {
        throw new BuildException(s"""Defines '$defineStrs' are expected.
                                    |Minimum version of clang is '$clangMinVersion'.
                                    |Please refer to ($docSetup)""".stripMargin)
      }
    }

    def checkVersion(clang: String): Unit = {
      def versionMajorFull(clang: String): (Int, String) = {
        val versionCommand = s"$clang --version"
        val versionString = Process(versionCommand)
          .lineStream_!(silentLogger())
          .headOption
          .getOrElse {
            throw new BuildException(
              s"""Problem running '$versionCommand'. Please check clang setup.
                 |Refer to ($docSetup)""".stripMargin)
          }
        // Apple macOS clang is different vs brew installed or Linux
        // Apple LLVM version 10.0.1 (clang-1001.0.46.4)
        // clang version 11.0.0
        try {
          val versionArray = versionString.split(" ")
          val versionIndex = versionArray.indexWhere(_.equals("version"))
          val version      = versionArray(versionIndex + 1)
          val majorVersion = version.split("\\.").head
          (majorVersion.toInt, version)
        } catch {
          case t: Throwable =>
            throw new BuildException(
              s"""Output from '$versionCommand' unexpected.
                 |Was expecting '... version n.n.n ...'.
                 |Got '$versionString'.
                 |Cause: ${t}""".stripMargin)
        }
      }

      val (majorVersion, version) = versionMajorFull(clang)

      if (majorVersion < clangMinVersion) {
        throw new BuildException(
          s"""Minimum version of clang is '$clangMinVersion'.
             |Discovered version '$version'.
             |Please refer to ($docSetup)""".stripMargin)
      }
    }

    val clangStr = pathToClangBinary.abs
    checkVersion(clangStr)
    checkDefinesBuiltIn(clangStr)
  }

  /** Minimum version of clang */
  private[scalanative] final val clangMinVersion = 6

  /** Link to setup documentation */
  private[scalanative] val docSetup =
    "http://www.scala-native.org/en/latest/user/setup.html"

  /** Discover the binary path using environment
   *  variables or the command from the path.
   */
  private[scalanative] def discover(binaryName: String): Path = {
    val envName =
      if (binaryName == "clang") "CLANG"
      else if (binaryName == "clang++") "CLANGPP"
      else {
        // shouldn't happen
        throw new BuildException(s"'$binaryName' must be clang or clang++")
      }

    val envPath = s"${envName}_PATH"

    val binaryNameOrPath = sys.env.get(envPath).getOrElse(binaryName)

    val path = Process(s"which $binaryNameOrPath")
      .lineStream_!(silentLogger())
      .map { p => Paths.get(p) }
      .headOption
      .getOrElse {
        throw new BuildException(
          s"""No '$binaryNameOrPath' found in PATH or via '$envPath' environment variable.
             |Please refer to ($docSetup)""".stripMargin)
      }
    path
  }

  private def silentLogger(): ProcessLogger =
    ProcessLogger(_ => (), _ => ())

  private def getenv(key: String): Option[String] =
    Option(System.getenv.get(key))
}
