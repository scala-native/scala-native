package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

/** An object describing how to configure the Scala Native toolchain. */
sealed trait Config {

  private val testSuffix = "-test"

  /** Base Directory for native work products. */
  def baseDir: Path

  /** Indicates whether this is a test config or not. */
  def testConfig: Boolean

  /** Directory to emit intermediate compilation results. Calculated based on
   *  [[#baseDir]] / `native` or `native-test` if a test project. The build
   *  creates directories if they do not exist.
   */
  def workDir: Path

  /** Name of the project module from the build system. Must be unique amongst
   *  modules in the larger project.
   *
   *  @return
   *    moduleName
   */
  def moduleName: String

  /** Base name for executable or library, typically the project/module name
   *  from the build tool [[#moduleName]] or can be overridden by the user with
   *  [[NativeConfig#baseName]]. This must be unique over all module names and
   *  other `baseName`s in the project. Delegated method to
   *  [[NativeConfig#baseName]]
   */
  def baseName: String

  /** This is the name of the executable or library. Calculated based on a
   *  prefix for libraries `lib` for UNIX like OSes, [[#baseName]], `-test` if
   *  [[#testConfig]] is `true`, and the executable or library suffix depending
   *  on platform and library type.
   */
  def artifactName: String

  /** Final Path to the output file, executable or library. Calculated based on
   *  [[#baseDir]] `/` [[#artifactName]].
   */
  def artifactPath: Path

  /** Build path to support multiple main applications.
   *
   *  For libraries it is the same as the [[#artifactPath]] and for applications
   *  it resolves to [[#workDir]] `/` [[#artifactName]] and after the build it
   *  is copied to [[#artifactPath]].
   */
  def buildPath: Path

  /** Entry point for linking. */
  def mainClass: Option[String]

  /** Sequence of all NIR locations. */
  def classPath: Seq[Path]

  /** The logger used by the toolchain. */
  def logger: Logger

  /** The [[NativeConfig]] that is used by the developer to control settings. */
  def compilerConfig: NativeConfig

  /** Create a new config with given base directory. */
  def withBaseDir(value: Path): Config

  /** Create a new config with test (true) or normal config (false). */
  def withTestConfig(value: Boolean): Config

  /** Create a new config with the module name - required. */
  def withModuleName(value: String): Config

  /** Create new config with a fully qualified (with package) main class name as
   *  an [[Option]]. Only applicable if [[NativeConfig#buildTarget]] is a
   *  [[BuildTarget#application]].
   *
   *  @param value
   *    fully qualified main class name as an [[Option]], default [[None]]
   *  @return
   *    this config object
   */
  def withMainClass(value: Option[String]): Config

  /** Create a new config with given nir paths. */
  def withClassPath(value: Seq[Path]): Config

  /** Create a new config with the given logger. */
  def withLogger(value: Logger): Config

  /** Create a [[Config]] with a new [[NativeConfig]]. */
  def withCompilerConfig(value: NativeConfig): Config

  /** Create a [[Config]] with a function which takes and returns a
   *  [[NativeConfig]].
   */
  def withCompilerConfig(fn: NativeConfig => NativeConfig): Config

  // delegated methods

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

  // helpers

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

/** Factory to create [[#empty]] [[Config]] objects */
object Config {

  /** Default empty config object where all of the fields are left blank or the
   *  default value.
   */
  def empty: Config =
    Impl(
      baseDir = Paths.get(""),
      testConfig = false,
      moduleName = "",
      mainClass = None,
      classPath = Seq.empty,
      logger = Logger.default,
      compilerConfig = NativeConfig.empty
    )

  private final case class Impl(
      baseDir: Path,
      testConfig: Boolean,
      moduleName: String,
      mainClass: Option[String],
      classPath: Seq[Path],
      logger: Logger,
      compilerConfig: NativeConfig
  ) extends Config {

    def withBaseDir(value: Path): Config =
      copy(baseDir = value)

    def withTestConfig(value: Boolean): Config =
      copy(testConfig = value)

    def withModuleName(value: String): Config =
      copy(moduleName = value)

    def withMainClass(value: Option[String]): Config =
      copy(mainClass = value)

    def withClassPath(value: Seq[Path]): Config =
      copy(classPath = value)

    def withLogger(value: Logger): Config =
      copy(logger = value)

    override def withCompilerConfig(value: NativeConfig): Config =
      copy(compilerConfig = value)

    override def withCompilerConfig(fn: NativeConfig => NativeConfig): Config =
      copy(compilerConfig = fn(compilerConfig))

    override lazy val workDir: Path =
      baseDir.resolve(s"native$nameSuffix")

    override lazy val baseName: String =
      compilerConfig.baseName match {
        case bn if bn.nonEmpty => bn
        case _                 => moduleName
      }

    override lazy val artifactName: String =
      artifactName(baseName)

    override lazy val artifactPath: Path =
      baseDir.resolve(artifactName)

    override lazy val buildPath: Path =
      compilerConfig.buildTarget match {
        case BuildTarget.Application =>
          workDir.resolve(artifactName(mainClass.get))
        case _: BuildTarget.Library =>
          baseDir.resolve(artifactName)
      }

    private def artifactName(name: String) = {
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
      s"$namePrefix${name}$nameSuffix$ext"
    }

    override def toString: String = {
      val classPathFormat =
        classPath.mkString("List(", "\n".padTo(22, ' '), ")")
      s"""Config(
        | - baseDir:        $baseDir
        | - testConfig:     $testConfig
        | - workDir:        $workDir
        | - moduleName:     $moduleName
        | - baseName:       $baseName
        | - artifactName:   $artifactName
        | - artifactPath:   $artifactPath
        | - buildPath:      $buildPath
        | - mainClass:      $mainClass
        | - classPath:      $classPathFormat
        | - compilerConfig: $compilerConfig
        |)""".stripMargin
    }

  }
}
