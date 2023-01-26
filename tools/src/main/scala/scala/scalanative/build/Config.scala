package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  private val testSuffix = "-test"

  /** Base Directory for native work products. */
  def basedir: Path

  /** Indicates whether this is a test config or not. */
  def testConfig: Boolean

  /** Directory to emit intermediate compilation results. Calculated based on
   *  [[basedir]] / native or native-test if a test project. The build creates
   *  directories if they do not exist.
   */
  def workdir: Path

  /** Base name for executable or library, typically the project name from the
   *  build tool [[defaultBasename]] or can be overridden by the user with
   *  [[NativeConfig#basename]].
   */
  def basename: String

  /** This is the name of the executable or library. Calculated based on a
   *  prefix for libraries `lib` for UNIX like OSes, [[basename]], `-test` if
   *  [[withTestConfig]] is `true`, and the executable or library suffix
   *  depending on platform and library type.
   */
  def artifactName: String

  /** Path to the output file, executable or library. Calculated based on
   *  [[basedir]] `/` [[artifactName]].
   */
  def artifactPath: Path

  /** Entry point for linking. */
  def mainClass: Option[String]

  /** Sequence of all NIR locations. */
  def classPath: Seq[Path]

  /** The logger used by the toolchain. */
  def logger: Logger

  def compilerConfig: NativeConfig

  /** Create a new config with given base directory. */
  def withBasedir(value: Path): Config

  /** Create a new config with given basename (module name) - required. */
  def withDefaultBasename(value: String): Config

  /** Create a new config with test (true) or normal config (false). */
  def withTestConfig(value: Boolean): Config

  /** Create new config with a fully qualified (with package) main class name as
   *  an [[Option]]. Only applicable if [[NativeConfig#buildTarget]] is a
   *  [[BuildTarget#Application]].
   *
   *  @param value
   *    fully qualified main class name as an [[Option]], default
   *    [[Option#none]]
   *  @return
   *    this config object
   */
  def withMainClass(value: Option[String]): Config

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

  protected def nameSuffix = if (testConfig) testSuffix else ""

  private[scalanative] lazy val targetsWindows: Boolean = {
    compilerConfig.targetTriple.fold(Platform.isWindows) { customTriple =>
      customTriple.contains("win32") ||
      customTriple.contains("windows")
    }
  }

  private[scalanative] lazy val targetsMac: Boolean = Platform.isMac ||
    compilerConfig.targetTriple.exists { customTriple =>
      Seq("mac", "apple", "darwin").exists(customTriple.contains(_))
    }
}

object Config {

  /** Default empty config object where all of the fields are left blank. */
  def empty: Config =
    Impl(
      nativelib = Paths.get(""),
      mainClass = None,
      classPath = Seq.empty,
      basedir = Paths.get(""),
      defaultBasename = "",
      testConfig = false,
      logger = Logger.default,
      compilerConfig = NativeConfig.empty
    )

  private final case class Impl(
      nativelib: Path,
      mainClass: Option[String],
      classPath: Seq[Path],
      basedir: Path,
      defaultBasename: String,
      testConfig: Boolean,
      logger: Logger,
      compilerConfig: NativeConfig
  ) extends Config {
    def withNativelib(value: Path): Config =
      copy(nativelib = value)

    def withMainClass(value: Option[String]): Config =
      copy(mainClass = value)

    def withClassPath(value: Seq[Path]): Config =
      copy(classPath = value)

    def withBasedir(value: Path): Config =
      copy(basedir = value)

    def withDefaultBasename(value: String): Config =
      copy(defaultBasename = value)

    def withTestConfig(value: Boolean): Config =
      copy(testConfig = value)

    def withLogger(value: Logger): Config =
      copy(logger = value)

    override def withCompilerConfig(value: NativeConfig): Config =
      copy(compilerConfig = value)

    override def withCompilerConfig(fn: NativeConfig => NativeConfig): Config =
      copy(compilerConfig = fn(compilerConfig))

    override lazy val workdir: Path =
      basedir.resolve(s"native$nameSuffix")

    override lazy val basename: String = compilerConfig.basename match {
      case bn if bn.nonEmpty => bn
      case _                 => defaultBasename
    }

    override lazy val artifactName: String = {
      val ext = compilerConfig.buildTarget match {
        case BuildTarget.Application =>
          if (targetsWindows) ".exe" else ""
        case BuildTarget.LibraryDynamic =>
          if (targetsWindows) ".dll"
          else if (targetsMac) ".dylib"
          else ".so"
        case BuildTarget.LibraryStatic =>
          if (targetsWindows) ".lib"
          else ".a"
      }
      val namePrefix = compilerConfig.buildTarget match {
        case BuildTarget.Application => ""
        case _: BuildTarget.Library  => if (targetsWindows) "" else "lib"
      }
      s"$namePrefix${basename}$nameSuffix$ext"
    }

    override lazy val artifactPath: Path = {
      basedir.resolve(artifactName)
    }

    override def toString: String = {
      val classPathFormat =
        classPath.mkString("List(", "\n".padTo(22, ' '), ")")
      s"""Config(
        | - basedir:         $basedir
        | - testConfig:      $testConfig
        | - workdir:         $workdir
        | - defaultBasename: $defaultBasename
        | - artifactPath:    $artifactPath
        | - classPath:       $classPathFormat
        | - compilerConfig:  $compilerConfig
        |)""".stripMargin
    }

  }
}
