package scala.scalanative
package build

import java.nio.file.{Path, Paths}

import scalanative.build.Discover.NativeLib

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  /** The garbage collector to use. */
  def gc: GC

  /** Compilation mode. */
  def mode: Mode

  /** The path to the `clang` executable. */
  def clang: Path

  /** The path to the `clang++` executable. */
  def clangPP: Path

  /** The options passed to LLVM's linker. */
  def linkingOptions: Seq[String]

  /** The compilation options passed to LLVM. */
  def compileOptions: Seq[String]

  /** Target triple that defines current OS, ABI and CPU architecture. */
  def targetTriple: String

  /** Directory to emit intermediate compilation results. */
  def workdir: Path

  /** Path to the nativelib jar. */
  @deprecated("Use nativelibs: Seq[NativeLib]", "0.4.0")
  def nativelib: Path

  private[build] def internalNativelib: Path

  /** Sequence of NativeLib, name and path to jars. */
  def nativelibs: Seq[NativeLib]

  /** Entry point for linking. */
  def mainClass: String

  /** Sequence of all NIR locations. */
  def classPath: Seq[Path]

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** The logger used by the toolchain. */
  def logger: Logger

  /** The LTO mode to use used during a release build. */
  def LTO: String

  /** Shall linker check that NIR is well-formed after every phase? */
  def check: Boolean

  /** Shall linker dump intermediate NIR after every phase? */
  def dump: Boolean

  /** Project contains native. Project name and source directory. */
  def nativeCodeProject: Option[NativeLib]

  /** Create a new config with given garbage collector. */
  def withGC(value: GC): Config

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): Config

  /** Create a new config with given path to clang. */
  def withClang(value: Path): Config

  /** Create a new config with given path to clang++. */
  def withClangPP(value: Path): Config

  /** Create a new config with given linking options. */
  def withLinkingOptions(value: Seq[String]): Config

  /** Create a new config with given compilation options. */
  def withCompileOptions(value: Seq[String]): Config

  /** Create a new config with given target triple. */
  def withTargetTriple(value: String): Config

  /** Create a new config with given directory. */
  def withWorkdir(value: Path): Config

  /** Create a new config with given path to nativelib. */
  @deprecated("Use withNativelibs(value: Seq[NativeLib])", "0.4.0")
  def withNativelib(value: Path): Config

  /** Create a new config with a Map of name to paths for the nativelibs. */
  def withNativelibs(value: Seq[NativeLib]): Config

  /** Create new config with given mainClass point. */
  def withMainClass(value: String): Config

  /** Create a new config with given nir paths. */
  def withClassPath(value: Seq[Path]): Config

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): Config

  /** Create a new config with the given logger. */
  def withLogger(value: Logger): Config

  /** Create a new config with the given lto mode. */
  def withLTO(value: String): Config

  /** Create a new config with given check value. */
  def withCheck(value: Boolean): Config

  /** Create a new config with given dump value. */
  def withDump(value: Boolean): Config

  /** Create a new config with an optional native code project. */
  def withNativeCodeProject(value: Option[NativeLib]): Config
}

object Config {

  /** Default empty config object where all of the fields are left blank. */
  def empty: Config =
    Impl(
      nativelib = Paths.get(""),
      internalNativelib = Paths.get(""),
      nativelibs = Seq.empty,
      mainClass = "",
      classPath = Seq.empty,
      workdir = Paths.get(""),
      clang = Paths.get(""),
      clangPP = Paths.get(""),
      targetTriple = "",
      linkingOptions = Seq.empty,
      compileOptions = Seq.empty,
      gc = GC.default,
      mode = Mode.default,
      linkStubs = false,
      logger = Logger.default,
      LTO = "none",
      check = false,
      dump = false,
      nativeCodeProject = None
    )

  private final case class Impl(nativelib: Path,
                                internalNativelib: Path,
                                nativelibs: Seq[NativeLib],
                                mainClass: String,
                                classPath: Seq[Path],
                                workdir: Path,
                                clang: Path,
                                clangPP: Path,
                                targetTriple: String,
                                linkingOptions: Seq[String],
                                compileOptions: Seq[String],
                                gc: GC,
                                mode: Mode,
                                linkStubs: Boolean,
                                logger: Logger,
                                LTO: String,
                                check: Boolean,
                                dump: Boolean,
                                nativeCodeProject: Option[NativeLib])
      extends Config {
    def withNativelib(value: Path): Config =
      copy(nativelib = value, internalNativelib = value)

    def withNativelibs(value: Seq[NativeLib]): Config =
      copy(nativelibs = value)

    def withMainClass(value: String): Config =
      copy(mainClass = value)

    def withClassPath(value: Seq[Path]): Config =
      copy(classPath = value)

    def withWorkdir(value: Path): Config =
      copy(workdir = value)

    def withClang(value: Path): Config =
      copy(clang = value)

    def withClangPP(value: Path): Config =
      copy(clangPP = value)

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

    def withLTO(value: String): Config =
      copy(LTO = value)

    def withCheck(value: Boolean): Config =
      copy(check = value)

    def withDump(value: Boolean): Config =
      copy(dump = value)

    def withNativeCodeProject(value: Option[NativeLib]): Config =
      copy(nativeCodeProject = value)
  }
}
