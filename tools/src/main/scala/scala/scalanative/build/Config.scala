package scala.scalanative
package build

import java.nio.file.{Path, Paths}

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  /** Directory to emit intermediate compilation results. */
  def workdir: Path

  /** Path to the nativelib jar. */
  @deprecated("Not needed: discovery is internal", "0.4.0")
  def nativelib: Path

  /** Entry point for linking. */
  def mainClass: String

  /** Optional main class for linking, introduced for binary compatiblity. */
  def selectedMainClass: Option[String]

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

  private[scalanative] lazy val targetsWindows: Boolean = {
    compilerConfig.targetTriple.fold(Platform.isWindows) { customTriple =>
      customTriple.contains("win32") ||
      customTriple.contains("windows")
    }
  }

  private[scalanative] lazy val targetsMac = Platform.isMac ||
    compilerConfig.targetTriple.exists { customTriple =>
      Seq("mac", "apple", "darwin").exists(customTriple.contains(_))
    }

  private[scalanative] lazy val targetsMsys: Boolean = {
    compilerConfig.targetTriple.fold(Platform.isMsys) { customTriple =>
      customTriple.contains("windows-msys")
    }
  }
  private[scalanative] lazy val targetsCygwin: Boolean = {
    compilerConfig.targetTriple.fold(Platform.isCygwin) { customTriple =>
      customTriple.contains("windows-cygnus")
    }
  }

  private[scalanative] lazy val targetsLinux: Boolean = Platform.isLinux ||
    compilerConfig.targetTriple.exists { customTriple =>
      Seq("linux").exists(customTriple.contains(_))
    }
}

object Config {

  /** Default empty config object where all of the fields are left blank. */
  def empty: Config =
    Impl(
      nativelib = Paths.get(""),
      selectedMainClass = None,
      classPath = Seq.empty,
      workdir = Paths.get(""),
      logger = Logger.default,
      compilerConfig = NativeConfig.empty
    )

  private final case class Impl(
      nativelib: Path,
      selectedMainClass: Option[String],
      classPath: Seq[Path],
      workdir: Path,
      logger: Logger,
      compilerConfig: NativeConfig
  ) extends Config {
    override def mainClass: String = selectedMainClass.getOrElse {
      throw new RuntimeException("Main class was not selected")
    }

    def withNativelib(value: Path): Config =
      copy(nativelib = value)

    def withMainClass(value: String): Config =
      copy(selectedMainClass = Option(value).filter(_.nonEmpty))

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
  }
}
