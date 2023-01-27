package scala.scalanative
package build

import java.nio.file.{Path, Paths}
import scala.scalanative.nir.Val

/** An object describing how to configure the Scala Native toolchain. */
sealed trait NativeConfig {

  /** The garbage collector to use. */
  def gc: GC

  /** Compilation mode. */
  def mode: Mode

  /** Build target for current compilation */
  def buildTarget: BuildTarget

  /** The path to the `clang` executable. */
  def clang: Path

  /** The path to the `clang++` executable. */
  def clangPP: Path

  /** The options passed to LLVM's linker. */
  def linkingOptions: Seq[String]

  /** The compilation options passed to LLVM. */
  def compileOptions: Seq[String]

  /** Optional target triple that defines current OS, ABI and CPU architecture.
   */
  def targetTriple: Option[String]

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** The LTO mode to use used during a release build. */
  def lto: LTO

  /** Shall linker check that NIR is well-formed after every phase? */
  def check: Boolean

  /** Shall linker NIR check treat warnings as errors? */
  def checkFatalWarnings: Boolean

  /** Shall linker dump intermediate NIR after every phase? */
  def dump: Boolean

  /** Should address sanitizer be used? */
  def asan: Boolean

  /** Shall we optimize the resulting NIR code? */
  def optimize: Boolean

  /** Shall we use the incremental compilation? */
  def useIncrementalCompilation: Boolean

  /** Shall be compiled with multithreading support */
  def multithreadingSupport: Boolean

  /** Map of user defined properties resolved at linktime */
  def linktimeProperties: NativeConfig.LinktimeProperites

  /** Configuration when doing optimization */
  def optimizerConfig: OptimizerConfig

  private lazy val detectedTriple = Discover.targetTriple(clang)

  /** Are we targeting a 32-bit platform?
   *
   *  This should perhaps list known 32-bit architectures and search for others
   *  containing "32" and assume everything else is 64-bit. Printing the
   *  architecture for a name that is not found seems excessive perhaps?
   */
  def is32BitPlatform = {
    targetTriple
      .getOrElse(detectedTriple)
      .split('-')
      .headOption
      .getOrElse("") match {
      case "x86_64"  => false
      case "aarch64" => false
      case "arm64"   => false
      case "i386"    => true
      case "i686"    => true
      case o =>
        println(
          s"Unexpected architecture in target triple: ${o}, defaulting to 64-bit"
        )
        false
    }
  }

  /** Shall the resource files be embedded in the resulting binary file? Allows
   *  the use of getClass().getResourceAsStream() on the included files. Will
   *  not embed files with certain extensions, including ".c", ".h", ".scala"
   *  and ".class".
   */
  def embedResources: Boolean

  /** Base name for executable or library, typically the project name. */
  def basename: String

  /** Create a new config with given garbage collector. */
  def withGC(value: GC): NativeConfig

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): NativeConfig

  /** Create a new config with given path to clang. */
  def withClang(value: Path): NativeConfig

  /** Create a new config with given path to clang++. */
  def withClangPP(value: Path): NativeConfig

  /** Create a new config with given linking options. */
  def withLinkingOptions(value: Seq[String]): NativeConfig

  /** Create a new config with given compilation options. */
  def withCompileOptions(value: Seq[String]): NativeConfig

  /** Create a new config with given build target */
  def withBuildTarget(target: BuildTarget): NativeConfig

  /** Create a new config given a target triple. */
  def withTargetTriple(value: Option[String]): NativeConfig

  /** Create a new config given a target triple. */
  def withTargetTriple(value: String): NativeConfig

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): NativeConfig

  /** Create a new config with the given lto mode. */
  def withLTO(value: LTO): NativeConfig

  /** Create a new config with given check value. */
  def withCheck(value: Boolean): NativeConfig

  /** Create a new config with given checkFatalWarnings value. */
  def withCheckFatalWarnings(value: Boolean): NativeConfig

  /** Create a new config with given dump value. */
  def withDump(value: Boolean): NativeConfig

  /** Create a new config with given asan value. */
  def withASAN(value: Boolean): NativeConfig

  /** Create a new config with given optimize value */
  def withOptimize(value: Boolean): NativeConfig

  /** Create a new config with given incrementalCompilation value */
  def withIncrementalCompilation(value: Boolean): NativeConfig

  /** Create a new config with given linktime properites */
  def withLinktimeProperties(
      value: NativeConfig.LinktimeProperites
  ): NativeConfig

  def withEmbedResources(value: Boolean): NativeConfig

  /** Create a new config with support for multithreading */
  def withMultithreadingSupport(enabled: Boolean): NativeConfig

  /** Create a new config with given base artifact name. */
  def withBasename(value: String): NativeConfig

  /** Create a optimization configuration */
  def withOptimizerConfig(value: OptimizerConfig): NativeConfig

}

object NativeConfig {
  type LinktimeProperites = Map[String, Any]

  /** Default empty config object where all of the fields are left blank. */
  def empty: NativeConfig =
    Impl(
      clang = Paths.get(""),
      clangPP = Paths.get(""),
      linkingOptions = Seq.empty,
      compileOptions = Seq.empty,
      targetTriple = None,
      gc = GC.default,
      lto = LTO.default,
      mode = Mode.default,
      buildTarget = BuildTarget.default,
      check = false,
      checkFatalWarnings = false,
      dump = false,
      asan = false,
      linkStubs = false,
      optimize = true,
      useIncrementalCompilation = true,
      multithreadingSupport = false,
      linktimeProperties = Map.empty,
      embedResources = false,
      basename = "",
      optimizerConfig = OptimizerConfig.empty
    )

  private final case class Impl(
      clang: Path,
      clangPP: Path,
      linkingOptions: Seq[String],
      compileOptions: Seq[String],
      targetTriple: Option[String],
      gc: GC,
      mode: Mode,
      buildTarget: BuildTarget,
      lto: LTO,
      linkStubs: Boolean,
      check: Boolean,
      checkFatalWarnings: Boolean,
      dump: Boolean,
      asan: Boolean,
      optimize: Boolean,
      useIncrementalCompilation: Boolean,
      multithreadingSupport: Boolean,
      linktimeProperties: LinktimeProperites,
      embedResources: Boolean,
      basename: String,
      optimizerConfig: OptimizerConfig
  ) extends NativeConfig {

    def withClang(value: Path): NativeConfig =
      copy(clang = value)

    def withClangPP(value: Path): NativeConfig =
      copy(clangPP = value)

    def withLinkingOptions(value: Seq[String]): NativeConfig =
      copy(linkingOptions = value)

    def withCompileOptions(value: Seq[String]): NativeConfig =
      copy(compileOptions = value)

    def withTargetTriple(value: Option[String]): NativeConfig =
      copy(targetTriple = value)

    def withTargetTriple(value: String): NativeConfig = {
      withTargetTriple(Some(value))
    }

    def withBuildTarget(target: BuildTarget): NativeConfig =
      copy(buildTarget = target)

    def withGC(value: GC): NativeConfig =
      copy(gc = value)

    def withMode(value: Mode): NativeConfig =
      copy(mode = value)

    def withLinkStubs(value: Boolean): NativeConfig =
      copy(linkStubs = value)

    def withLTO(value: LTO): NativeConfig =
      copy(lto = value)

    def withCheck(value: Boolean): NativeConfig =
      copy(check = value)

    def withCheckFatalWarnings(value: Boolean): NativeConfig =
      copy(checkFatalWarnings = value)

    def withDump(value: Boolean): NativeConfig =
      copy(dump = value)

    def withASAN(value: Boolean): NativeConfig =
      copy(asan = value)

    def withOptimize(value: Boolean): NativeConfig =
      copy(optimize = value)

    override def withIncrementalCompilation(value: Boolean): NativeConfig =
      copy(useIncrementalCompilation = value)

    def withMultithreadingSupport(enabled: Boolean): NativeConfig =
      copy(multithreadingSupport = enabled)

    def withLinktimeProperties(v: LinktimeProperites): NativeConfig = {
      checkLinktimeProperties(v)
      copy(linktimeProperties = v)
    }

    def withEmbedResources(value: Boolean): NativeConfig = {
      copy(embedResources = value)
    }

    def withBasename(value: String): NativeConfig = {
      copy(basename = value)
    }

    override def withOptimizerConfig(value: OptimizerConfig): NativeConfig = {
      copy(optimizerConfig = value)
    }

    override def toString: String = {
      val listLinktimeProperties = {
        if (linktimeProperties.isEmpty) ""
        else {
          val maxKeyLength = linktimeProperties.keys.map(_.length).max
          val keyPadSize = maxKeyLength.min(20)
          "\n" + linktimeProperties.toSeq
            .sortBy(_._1)
            .map {
              case (key, value) =>
                s"   * ${key.padTo(keyPadSize, ' ')} : $value"
            }
            .mkString("\n")
        }
      }
      s"""NativeConfig(
        | - clang:                  $clang
        | - clangPP:                $clangPP
        | - linkingOptions:         $linkingOptions
        | - compileOptions:         $compileOptions
        | - targetTriple:           $targetTriple
        | - buildTarget             $buildTarget
        | - GC:                     $gc
        | - mode:                   $mode
        | - LTO:                    $lto
        | - linkStubs:              $linkStubs
        | - check:                  $check
        | - checkFatalWarnings:     $checkFatalWarnings
        | - dump:                   $dump
        | - asan:                   $asan
        | - optimize                $optimize
        | - linktimeProperties:     $listLinktimeProperties
        | - embedResources:         $embedResources
        | - incrementalCompilation: $useIncrementalCompilation
        | - multithreading          $multithreadingSupport
        | - optimizerConfig:        ${optimizerConfig.show(" " * 3)}
        | - basename:               $basename
        |)""".stripMargin
    }
  }

  def checkLinktimeProperties(properties: LinktimeProperites): Unit = {
    def isNumberOrString(value: Any) = {
      value match {
        case _: Boolean | _: Byte | _: Char | _: Short | _: Int | _: Long |
            _: Float | _: Double | _: String | _: Val =>
          true
        case _ => false
      }
    }

    val invalid = properties.collect {
      case (key, value) if !isNumberOrString(value) => key
    }
    if (invalid.nonEmpty) {
      throw new BuildException(
        s"""Link-time properties needs to be non-null primitives or non-empty string
           |Invalid link-time properties:
           |${invalid.mkString(" - ", "\n", "")}
        """.stripMargin
      )
    }
  }

}
