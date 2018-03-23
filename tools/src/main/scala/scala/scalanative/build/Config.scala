package scala.scalanative
package build

import java.nio.file.{Path, Paths}

import nir.Global

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  /** The garbage collector to use. */
  def gc: GC

  /** Compilation mode. */
  def mode: Mode

  /** The path to the `clang` executable. */
  def clang: Path

  /** The path to the `clang++` executable. */
  def clangpp: Path

  /** The options passed to LLVM's linker. */
  def linkingOptions: Seq[String]

  /** The compilation options passed to LLVM. */
  def compileOptions: Seq[String]

  /** Target triple that defines current OS, ABI and CPU architecture. */
  def targetTriple: String

  /** Directory to emit intermediate compilation results. */
  def workdir: Path

  /** Path to the nativelib jar. */
  def nativelib: Path

  /** Entry point for linking. */
  def entry: String

  /** Sequence of all NIR locations. */
  def classpath: Seq[Path]

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** The logger used by the toolchain. */
  def logger: Logger

  /** Create a new config with given garbage collector. */
  def withGC(value: GC): Config

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): Config

  /** Create a new config with given path to clang. */
  def withClang(value: Path): Config

  /** Create a new config with given path to clang++. */
  def withClangpp(value: Path): Config

  /** Create a new config with given linking options. */
  def withLinkingOptions(value: Seq[String]): Config

  /** Create a new config with given compilation options. */
  def withCompileOptions(value: Seq[String]): Config

  /** Create a new config with given target triple. */
  def withTargetTriple(value: String): Config

  /** Create a new config with given directory. */
  def withWorkdir(value: Path): Config

  /** Create a new config with given path to nativelib. */
  def withNativelib(value: Path): Config

  /** Create new config with given entry point. */
  def withEntry(value: String): Config

  /** Create a new config with given nir paths. */
  def withClasspath(value: Seq[Path]): Config

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): Config

  /** Create a new config with the given logger. */
  def withLogger(value: Logger): Config
}

object Config {

  /** Default empty config object where all of the fields are left blank. */
  val empty: Config =
    Impl(
      nativelib = Paths.get(""),
      entry = "",
      classpath = Seq.empty,
      workdir = Paths.get(""),
      clang = Paths.get(""),
      clangpp = Paths.get(""),
      targetTriple = "",
      linkingOptions = Seq.empty,
      compileOptions = Seq.empty,
      gc = GC.default,
      mode = Mode.default,
      linkStubs = false,
      logger = Logger.default
    )

  private final case class Impl(nativelib: Path,
                                entry: String,
                                classpath: Seq[Path],
                                workdir: Path,
                                clang: Path,
                                clangpp: Path,
                                targetTriple: String,
                                linkingOptions: Seq[String],
                                compileOptions: Seq[String],
                                gc: GC,
                                mode: Mode,
                                linkStubs: Boolean,
                                logger: Logger)
      extends Config {
    def withNativelib(value: Path): Config =
      copy(nativelib = value)

    def withEntry(value: String): Config =
      copy(entry = value)

    def withClasspath(value: Seq[Path]): Config =
      copy(classpath = value)

    def withWorkdir(value: Path): Config =
      copy(workdir = value)

    def withClang(value: Path): Config =
      copy(clang = value)

    def withClangpp(value: Path): Config =
      copy(clangpp = value)

    def withTargetTriple(value: String): Config =
      copy(targetTriple = value)

    def withLinkingOptions(value: Seq[String]): Config =
      copy(linkingOptions = value)

    def withCompileOptions(value: Seq[String]): Config =
      copy(compileOptions = value)

    def withGC(value: GC): Config =
      copy(gc = value)

    def withMode(value: Mode): Config =
      copy(mode = value)

    def withLinkStubs(value: Boolean): Config =
      copy(linkStubs = value)

    def withLogger(value: Logger): Config =
      copy(logger = value)
  }
}
