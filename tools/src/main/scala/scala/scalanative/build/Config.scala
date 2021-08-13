package scala.scalanative
package build

import java.nio.file.{Path, Paths}
import scala.scalanative.nir.Val

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  /** Directory to emit intermediate compilation results. */
  def workdir: Path

  /** Path to the nativelib jar. */
  @deprecated("Not needed: discovery is internal", "0.4.0")
  def nativelib: Path

  /** Entry point for linking. */
  def mainClass: String

  /** Sequence of all NIR locations. */
  def classPath: Seq[Path]

  /** The logger used by the toolchain. */
  def logger: Logger

  def compilerConfig: NativeConfig

  /** Create a new config with given directory. */
  def withWorkdir(value: Path): Config

  /** Create a new config with given path to nativelib. */
  @deprecated("Not needed: discovery is internal", "0.4.0")
  def withNativelib(value: Path): Config

  /** Create new config with given mainClass point. */
  def withMainClass(value: String): Config

  /** Create a new config with given nir paths. */
  def withClassPath(value: Seq[Path]): Config

  /** Create a new config with the given logger. */
  def withLogger(value: Logger): Config

  def withCompilerConfig(value: NativeConfig): Config

  def withCompilerConfig(fn: NativeConfig => NativeConfig): Config

  /** The garbage collector to use. */
  def gc: GC = compilerConfig.gc

  /** Compilation mode. */
  def mode: Mode = compilerConfig.mode

  /** The path to the `clang` executable. */
  def clang: Path = compilerConfig.clang

  /** The path to the `clang++` executable. */
  def clangPP: Path = compilerConfig.clangPP

  /** The options passed to LLVM's linker. */
  def linkingOptions: Seq[String] = compilerConfig.linkingOptions

  /** The compilation options passed to LLVM. */
  def compileOptions: Seq[String] = compilerConfig.compileOptions

  /** Should stubs be linked? */
  def linkStubs: Boolean = compilerConfig.linkStubs

  /** The LTO mode to use used during a release build. */
  def LTO: LTO = compilerConfig.lto

  /** Shall linker check that NIR is well-formed after every phase? */
  def check: Boolean = compilerConfig.check

  /** Shall linker dump intermediate NIR after every phase? */
  def dump: Boolean = compilerConfig.dump

  /** Should address sanitizer be used? */
  def asan: Boolean = compilerConfig.asan

  def is32: Boolean =
    compilerConfig.targetTriple
      .getOrElse(
        Discover.targetTriple(clang, workdir)
      )
      .split('-')
      .headOption
      .getOrElse("") match {
      case "x86_64" => false
      case "i386"   => true
      case "i686"   => true
      case o =>
        println(
          s"Unexpected architecture in target triple: ${o}, defaulting to 64-bit"
        )
        false
    }

  private[scalanative] def targetsWindows: Boolean = {
    compilerConfig.targetTriple.fold(Platform.isWindows) { customTriple =>
      customTriple.contains("win32") ||
      customTriple.contains("windows")
    }
  }

  /** Map of properties resolved at linktime */
  def linktimeProperties: Map[String, Any]
}

object Config {

  /** Default empty config object where all of the fields are left blank. */
  def empty: Config =
    Impl(
      nativelib = Paths.get(""),
      mainClass = "",
      classPath = Seq.empty,
      workdir = Paths.get(""),
      logger = Logger.default,
      compilerConfig = NativeConfig.empty
    )

  private final case class Impl(
      nativelib: Path,
      mainClass: String,
      classPath: Seq[Path],
      workdir: Path,
      logger: Logger,
      compilerConfig: NativeConfig
  ) extends Config {
    def withNativelib(value: Path): Config =
      copy(nativelib = value)

    def withMainClass(value: String): Config =
      copy(mainClass = value)

    def withClassPath(value: Seq[Path]): Config =
      copy(classPath = value)

    def withWorkdir(value: Path): Config =
      copy(workdir = value)

    def withLogger(value: Logger): Config =
      copy(logger = value)

    override def withCompilerConfig(value: NativeConfig): Config =
      copy(compilerConfig = value)

    override def withCompilerConfig(fn: NativeConfig => NativeConfig): Config =
      copy(compilerConfig = fn(compilerConfig))

    override def linktimeProperties: Map[String, Any] = {
      val base = compilerConfig.linktimeProperties
      val isWindows = compilerConfig.linktimeProperties
        .get("scala.scalanative.meta.linktimeinfo.isWindows")
        .getOrElse(targetsWindows)
        .asInstanceOf[Boolean]
      val is32 = compilerConfig.linktimeProperties
        .get("scala.scalanative.meta.linktimeinfo.is32")
        .getOrElse(this.is32)
        .asInstanceOf[Boolean]
      Map(
        "scala.scalanative.meta.linktimeinfo.isWindows" -> isWindows,
        "scala.scalanative.meta.linktimeinfo.is32" -> is32,
        "scala.scalanative.meta.linktimeinfo.sizeOfPtr" -> Val.Size(
          if (is32) 4 else 8
        )
      ) ++ compilerConfig.linktimeProperties
    }
  }
}
