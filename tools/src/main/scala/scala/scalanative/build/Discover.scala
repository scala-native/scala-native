package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}

import scala.reflect.ClassTag
import scala.sys.process._
import scala.util.Try

import scala.scalanative.build.IO.RichPath

/** Utilities for discovery of command-line tools and settings required to build
 *  Scala Native applications.
 */
object Discover {

  /** Compilation mode name from SCALANATIVE_MODE env var or default. */
  def mode(): Mode =
    getenv("SCALANATIVE_MODE")
      .filterNot(_.isEmpty())
      .map(build.Mode(_))
      .getOrElse(build.Mode.default)

  def optimize(): Boolean =
    getenv("SCALANATIVE_OPTIMIZE").forall(_.toBoolean)

  /** LTO variant used for release mode from SCALANATIVE_LTO env var or default.
   */
  def LTO(): LTO =
    getenv("SCALANATIVE_LTO")
      .filterNot(_.isEmpty())
      .map(build.LTO(_))
      .getOrElse(build.LTO.None)

  /** GC variant used from SCALANATIVE_GC env var or default. */
  def GC(): GC =
    getenv("SCALANATIVE_GC")
      .filterNot(_.isEmpty())
      .map(build.GC(_))
      .getOrElse(build.GC.default)

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

  /** Use llvm-config binary on the path or via LLVM_BIN env var */
  private lazy val llvmConfigCLI: String =
    tryDiscover("llvm-config", "LLVM_BIN")
      .map(_.toAbsolutePath.toString)
      .getOrElse("llvm-config")

  private def filterExisting(paths: Seq[String]): Seq[String] =
    paths.filter(new File(_).exists())

  /** Find default clang compilation options. */
  def compileOptions(): Seq[String] = {
    val includes = {
      val llvmIncludeDir =
        Try(Process(s"$llvmConfigCLI --includedir").lineStream_!.toSeq)
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
        Try(Process(s"$llvmConfigCLI --libdir").lineStream_!.toSeq)
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

  private case class ClangInfo(
      majorVersion: Int,
      fullVersion: String,
      targetTriple: String
  )
  private def clangInfo(implicit config: NativeConfig): ClangInfo =
    cache("clang-info")(config => clangInfo(config.clang))

  private def clangInfo(clang: Path): ClangInfo = {
    val versionCommand = Seq(clang.abs, "--version")
    val cmdString = versionCommand.mkString(" ")
    val processLines = Process(versionCommand)
      .lineStream_!(silentLogger())
      .toList

    val (versionString, targetString) = processLines match {
      case version :: target :: _ => (version, target)
      case _                      =>
        throw new BuildException(
          s"""Problem running '$cmdString'. Please check clang setup.
              |Refer to ($docSetup)""".stripMargin
        )
    }

    // Apple macOS clang is different vs brew installed or Linux
    // Apple LLVM version 10.0.1 (clang-1001.0.46.4)
    // clang version 11.0.0
    try {
      val versionArray = versionString.split(" ")
      val versionIndex = versionArray.indexWhere(_.equals("version"))
      val version = versionArray(versionIndex + 1)
      ClangInfo(
        majorVersion = version.takeWhile(_.isDigit).toInt,
        fullVersion = version,
        targetTriple = targetString.drop("Target: ".size)
      )
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
    val ClangInfo(majorVersion, version, _) = clangInfo(pathToClangBinary)

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

  private[scalanative] def tryDiscover(
      binaryName: String,
      envPath: String
  ): Try[Path] = Try(discover(binaryName, envPath))

  private[scalanative] def tryDiscover(
      binaryName: String
  ): Try[Path] = Try(discover(binaryName))

  /** Discover the binary path using environment variables or the command from
   *  the path.
   */
  private[scalanative] def discover(
      binaryName: String,
      envPath: Option[String]
  ): Path = {
    val binPath = envPath.flatMap(sys.env.get(_))

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
        val envMessage = envPath
          .map(envPath => s"or via '$envPath' environment variable")
          .getOrElse("")
        throw new BuildException(
          s"""'$binaryName' not found in PATH $envMessage.
            |Please refer to ($docSetup)""".stripMargin
        )
      }
    path
  }

  private[scalanative] def discover(binaryName: String, envPath: String): Path =
    discover(binaryName, Some(envPath))

  private[scalanative] def discover(binaryName: String): Path =
    discover(binaryName, None)

  /** Detect the target architecture.
   *
   *  @param clang
   *    A path to the executable `clang`.
   *  @return
   *    The detected target triple describing the target architecture.
   */
  def targetTriple(clang: Path): String = clangInfo(clang).targetTriple

  def targetTriple(implicit config: NativeConfig) = cache("target-triple") {
    _ => clangInfo.targetTriple
  }

  private def silentLogger(): ProcessLogger =
    ProcessLogger(_ => (), _ => ())

  private def getenv(key: String): Option[String] =
    Option(System.getenv.get(key))

  private object cache extends ContextBasedCache[NativeConfig, String, AnyRef]

  private[scalanative] object features {
    import FeatureSupport._

    def opaquePointers(implicit config: NativeConfig): FeatureSupport =
      cache("opaque-pointers") { _ =>
        try {
          val version = clangInfo.majorVersion
          // if version == 13 EnabledWithFlag("--force-opaque-pointers"): works on Unix and probably on Homebrew Clang; on Apple Clang missing or exists with different name
          // if version == 14 EnabledWithFlag("--opaque-pointers"): might require additional flag `--plugin-opt=opaque-pointers` to ld.lld linker on Unix, this opt is missing on ld64.lld in MacOS
          if (version < 15) Unavailable
          else Enabled
        } catch {
          case ex: Exception =>
            System.err.println(
              "Failed to detect version of clang, assuming opaque-pointers are not supported"
            )
            Unavailable
        }
      }

    sealed trait FeatureSupport {
      def isAvailable: Boolean = this match {
        case Unavailable => false
        case _           => true
      }
      def requiredFlag: Option[String] = this match {
        case EnabledWithFlag(flag) => Some(flag)
        case _                     => None
      }
    }
    object FeatureSupport {
      case object Unavailable extends FeatureSupport
      case object Enabled extends FeatureSupport
      case class EnabledWithFlag(compilationFlag: String) extends FeatureSupport
    }
  }

  private class ContextBasedCache[Ctx, Key, Value <: AnyRef] {
    private val cachedValues = scala.collection.mutable.Map.empty[Key, Value]
    private var lastContext: Ctx = _
    def apply[T <: Value: ClassTag](
        key: Key
    )(resolve: Ctx => T)(implicit context: Ctx): T = {
      lastContext match {
        case `context` =>
          val result = cachedValues.getOrElseUpdate(key, resolve(context))
          // Make sure stored value has correct type in case of duplicate keys
          val expectedType = implicitly[ClassTag[T]].runtimeClass
          assert(
            expectedType.isAssignableFrom(result.getClass),
            s"unexpected type of result for entry: `$key`, got ${result
                .getClass()}, expected $expectedType"
          )
          result.asInstanceOf[T]

        case _ =>
          // Context have changed
          cachedValues.clear()
          lastContext = context
          this(key)(resolve) // retry with cleaned cached
      }
    }
  }
}
