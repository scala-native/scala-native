package scala.scalanative
package tools

import java.nio.file.{Path, Paths}

import nir.Global

sealed trait Config {

  /** Path to the nativelib jar. */
  def nativeLib: Path

  /** Entry point for linking. */
  def entry: Global

  /** Sequence of all NIR locations. */
  def paths: Seq[Path]

  /** Directory to emit intermediate compilation results. */
  def workdir: Path

  /** The path to the `clang` executable. */
  def clang: Path

  /** The path to the `clang++` executable. */
  def clangpp: Path

  /** Target triple. */
  def target: String

  /** The options passed to LLVM's linker. */
  def linkingOptions: Seq[String]

  /** The compilation options passed to LLVM. */
  def compileOptions: Seq[String]

  /** The garbage collector to use. */
  def gc: GarbageCollector

  /** Compilation mode. */
  def mode: Mode

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** Create a new config with given path to nativelib. */
  def withNativeLib(value: Path): Config

  /** Create new config with given entry point. */
  def withEntry(value: Global): Config

  /** Create a new config with given nir paths. */
  def withPaths(value: Seq[Path]): Config

  /** Create a new config with given directory. */
  def withWorkdir(value: Path): Config

  /** Create a new config with given path to clang. */
  def withClang(value: Path): Config

  /** Create a new config with given path to clang++. */
  def withClangPP(value: Path): Config

  /** Create a new config with given target triple. */
  def withTarget(value: String): Config

  /** Create a new config with given linking options. */
  def withLinkingOptions(value: Seq[String]): Config

  /** Create a new config with given compilation options. */
  def withCompileOptions(value: Seq[String]): Config

  /** Create a new config with given garbage collector. */
  def withGC(value: GarbageCollector): Config

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): Config

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): Config
}

object Config {

  /** Default empty config object. */
  val empty: Config =
    Impl(
      nativeLib = Paths.get(""),
      entry = Global.None,
      paths = Seq.empty,
      workdir = Paths.get(""),
      clang = Paths.get(""),
      clangpp = Paths.get(""),
      target = "",
      linkingOptions = Seq.empty,
      compileOptions = Seq.empty,
      gc = GarbageCollector.default,
      mode = Mode.default,
      linkStubs = false
    )

  private final case class Impl(nativeLib: Path,
                                entry: Global,
                                paths: Seq[Path],
                                workdir: Path,
                                clang: Path,
                                clangpp: Path,
                                target: String,
                                linkingOptions: Seq[String],
                                compileOptions: Seq[String],
                                gc: GarbageCollector,
                                mode: Mode,
                                linkStubs: Boolean)
      extends Config {
    def withNativeLib(value: Path): Config =
      copy(nativeLib = value)

    def withEntry(value: Global): Config =
      copy(entry = value)

    def withPaths(value: Seq[Path]): Config =
      copy(paths = value)

    def withWorkdir(value: Path): Config =
      copy(workdir = value)

    def withClang(value: Path): Config =
      copy(clang = value)

    def withClangPP(value: Path): Config =
      copy(clangpp = value)

    def withTarget(value: String): Config =
      copy(target = value)

    def withLinkingOptions(value: Seq[String]): Config =
      copy(linkingOptions = value)

    def withCompileOptions(value: Seq[String]): Config =
      copy(compileOptions = value)

    def withGC(value: GarbageCollector): Config =
      copy(gc = value)

    def withMode(value: Mode): Config =
      copy(mode = value)

    def withLinkStubs(value: Boolean): Config =
      copy(linkStubs = value)
  }
}
