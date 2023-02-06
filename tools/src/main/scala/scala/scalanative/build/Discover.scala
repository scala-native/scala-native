package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.util.Try
import scala.sys.process._
import scala.scalanative.build.IO.RichPath

/** Utilities for discovery of command-line tools and settings required to build
 *  Scala Native applications.
 */
object Discover {

  /** Compilation mode name from SCALANATIVE_MODE env var or default. */
  def mode(): Mode =
    getenv("SCALANATIVE_MODE").map(build.Mode(_)).getOrElse(build.Mode.default)

  def optimize(): Boolean =
    getenv("SCALANATIVE_OPTIMIZE").forall(_.toBoolean)

  /** LTO variant used for release mode from SCALANATIVE_LTO env var or default.
   */
  def LTO(): LTO =
    getenv("SCALANATIVE_LTO").map(build.LTO(_)).getOrElse(build.LTO.None)

  /** GC variant used from SCALANATIVE_GC env var or default. */
  def GC(): GC =
    getenv("SCALANATIVE_GC").map(build.GC(_)).getOrElse(build.GC.default)

  /** Use the clang binary on the path or via LLVM_BIN env var. */
  def clang(): Path = {
    val path = discover("clang", "LLVM_BIN")
    checkClangVersion(path)
    path
  }

  /** Use the clang++ binary on the path or via LLVM_BIN env var. */
  def clangpp(): Path = {
    val path = discover("clang++", "LLVM_BIN")
    checkClangVersion(path)
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
      // dirs: standard, macports, brew M1 arm
      val includeDirs =
        getenv("SCALANATIVE_INCLUDE_DIRS")
          .map(_.split(File.pathSeparatorChar).toSeq)
          .getOrElse(
            filterExisting(
              Seq(
                "/usr/local/include",
                "/opt/local/include",
                "/opt/homebrew/include"
              )
            )
          )

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
          .getOrElse(
            filterExisting(
              Seq("/usr/local/lib", "/opt/local/lib", "/opt/homebrew/lib")
            )
          )

      (libDirs ++ llvmLibDir).map(s => s"-L$s")
    }
    libs
  }

  private def clangVersionMajorFullTarget(
      clang: String
  ): (Int, String, String) = {
    val versionCommand = Seq(clang, "--version")
    val processLines = Process(versionCommand)
      .lineStream_!(silentLogger())
    val versionString = processLines.headOption
      .getOrElse {
        throw new BuildException(s"""Problem running '${versionCommand
                                     .mkString(" ")}'. Please check clang setup.
              |Refer to ($docSetup)""".stripMargin)
      }

    val targetString = processLines.tail.headOption
      .getOrElse {
        throw new BuildException(s"""Problem running '${versionCommand
                                     .mkString(" ")}'. Please check clang setup.
              |Refer to ($docSetup)""".stripMargin)
      }

    // Apple macOS clang is different vs brew installed or Linux
    // Apple LLVM version 10.0.1 (clang-1001.0.46.4)
    // clang version 11.0.0
    try {
      val versionArray = versionString.split(" ")
      val versionIndex = versionArray.indexWhere(_.equals("version"))
      val version = versionArray(versionIndex + 1)
      val majorVersion = version.split("\\.").head
      (majorVersion.toInt, version, targetString.drop("Target: ".size))
    } catch {
      case t: Throwable =>
        throw new BuildException(s"""Output from '$versionCommand' unexpected.
                |Was expecting '... version n.n.n ...'.
                |Got '$versionString'.
                |Cause: ${t}""".stripMargin)
    }
  }

  /** Tests whether the clang compiler is greater or equal to the minumum
   *  version required.
   */
  private[scalanative] def checkClangVersion(pathToClangBinary: Path): Unit = {
    val (majorVersion, version, _) = clangVersionMajorFullTarget(
      pathToClangBinary.abs
    )

    if (majorVersion < clangMinVersion) {
      throw new BuildException(
        s"""Minimum version of clang is '$clangMinVersion'.
             |Discovered version '$version'.
             |Please refer to ($docSetup)""".stripMargin
      )
    }
  }

  /** Minimum version of clang */
  private[scalanative] final val clangMinVersion = 6

  /** Link to setup documentation */
  private[scalanative] val docSetup =
    "http://www.scala-native.org/en/latest/user/setup.html"

  /** Discover the binary path using environment variables or the command from
   *  the path.
   */
  private[scalanative] def discover(
      binaryName: String,
      envPath: String
  ): Path = {
    val binPath = sys.env.get(envPath)

    val command: Seq[String] = {
      if (Platform.isWindows) {
        val binName = s"${binaryName}.exe"
        val arg = binPath.fold(binName)(p => s"$p:$binName")
        Seq("where", arg)
      } else {
        val arg = binPath.fold(binaryName) { p =>
          Paths.get(p, binaryName).toString()
        }
        Seq("which", arg)
      }
    }

    val path = Process(command)
      .lineStream_!(silentLogger())
      .map { p => Paths.get(p) }
      .headOption
      .getOrElse {
        throw new BuildException(
          s"""'$binaryName' not found in PATH or via '$envPath' environment variable.
            |Please refer to ($docSetup)""".stripMargin
        )
      }
    path
  }

  /** Detect the target architecture.
   *
   *  @param clang
   *    A path to the executable `clang`.
   *  @return
   *    The detected target triple describing the target architecture.
   */
  def targetTriple(clang: Path): String = {
    val (_, _, target) = clangVersionMajorFullTarget(clang.abs)
    target
  }

  private def silentLogger(): ProcessLogger =
    ProcessLogger(_ => (), _ => ())

  private def getenv(key: String): Option[String] =
    Option(System.getenv.get(key))
}
