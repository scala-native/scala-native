package scala.scalanative
package build

import java.nio.file.{Path, Paths}

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  /** Target triple that defines current OS, ABI and CPU architecture. */
  def targetTriple: String

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

  /** Create a new config with given target triple. */
  def withTargetTriple(value: String): Config

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
}

object Config {

  /** Default empty config object where all of the fields are left blank. */
  def empty: Config =
    Impl(
      nativelib = Paths.get(""),
      mainClass = "",
      classPath = Seq.empty,
      workdir = Paths.get(""),
      targetTriple = "",
      logger = Logger.default,
      compilerConfig = NativeConfig.empty
    )

  private final case class Impl(nativelib: Path,
                                mainClass: String,
                                classPath: Seq[Path],
                                workdir: Path,
                                targetTriple: String,
                                logger: Logger,
                                compilerConfig: NativeConfig)
      extends Config {
    def withNativelib(value: Path): Config =
      copy(nativelib = value)

    def withMainClass(value: String): Config =
      copy(mainClass = value)

    def withClassPath(value: Seq[Path]): Config =
      copy(classPath = value)

    def withWorkdir(value: Path): Config =
      copy(workdir = value)

    def withTargetTriple(value: String): Config =
      copy(targetTriple = value)

    def withLogger(value: Logger): Config =
      copy(logger = value)

    override def withCompilerConfig(value: NativeConfig): Config =
      copy(compilerConfig = value)

    override def withCompilerConfig(fn: NativeConfig => NativeConfig): Config =
      copy(compilerConfig = fn(compilerConfig))
  }
}
