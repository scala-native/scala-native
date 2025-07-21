package scala.scalanative
package build

import java.nio.file.{Path, Paths}

/** An object describing how to configure the Scala Native toolchain. */
sealed trait NativeConfig {
  import NativeConfig._

  /** The path to the `clang` executable. */
  def clang: Path

  /** The path to the `clang++` executable. */
  def clangPP: Path

  /** The options passed to LLVM's linker. */
  def linkingOptions: Seq[String]

  /** The compilation options passed to LLVM. */
  def compileOptions: Seq[String]

  /** The compiler C version -std=XXX */
  def compileStdC: Option[String]

  /** The compiler C++ version -std=XXX */
  def compileStdCpp: Option[String]

  /** Optional target triple that defines current OS, ABI and CPU architecture.
   */
  def targetTriple: Option[String]

  /** The garbage collector to use. */
  def gc: GC

  /** The LTO mode to use used during a release build. */
  def lto: LTO

  /** Compilation mode. */
  def mode: Mode

  /** Build target for current compilation */
  def buildTarget: BuildTarget

  /** Shall linker check that NIR is well-formed after every phase? */
  def check: Boolean

  /** Shall linker NIR check treat warnings as errors? */
  def checkFatalWarnings: Boolean

  /** Should build fail if it detects usage of unsupported feature on given
   *  platform
   */
  def checkFeatures: Boolean

  /** Shall linker dump intermediate NIR after every phase? */
  def dump: Boolean

  /** Should sanitizer implemention be used? */
  def sanitizer: Option[Sanitizer]

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** Shall we optimize the resulting NIR code? */
  def optimize: Boolean

  /** Shall we use the incremental compilation? */
  def useIncrementalCompilation: Boolean

  /** Shall be compiled with multithreading support. If equal to `None` the
   *  toolchain would detect if program uses system threads - when not thrads
   *  are not used, the program would be linked without multihreading support.
   */
  def multithreading: Option[Boolean]

  /*  Was multhithreadinng explicitly select, if not default to true */
  private[scalanative] def multithreadingSupport: Boolean =
    multithreading.getOrElse(true)

  /** Map of user defined properties resolved at linktime */
  def linktimeProperties: NativeConfig.LinktimeProperites

  /** Shall the resource files be embedded in the resulting binary file? Allows
   *  the use of getClass().getResourceAsStream() on the included files. Will
   *  not embed files with certain extensions, including ".c", ".h", ".scala"
   *  and ".class".
   */
  def embedResources: Boolean

  /** A glob pattern that matches list of files to embed into the executable. */
  def resourceIncludePatterns: Seq[String]

  /** A glob pattern that matches list of files to exclude from embedding into
   *  the executable.
   */
  def resourceExcludePatterns: Seq[String]

  /** Base name for executable or library, typically the project name. */
  def baseName: String

  /** Configuration when doing optimization */
  def optimizerConfig: OptimizerConfig

  /** Configuration for semantics of generated program */
  def semanticsConfig: SemanticsConfig

  /** Configuration for LLVM metadata generation controlling source level
   *  debugging support
   */
  def sourceLevelDebuggingConfig: SourceLevelDebuggingConfig

  /** Create a new [[NativeConfig]] with given [[SourceLevelDebuggingConfig]] */
  def withSourceLevelDebuggingConfig(
      config: SourceLevelDebuggingConfig
  ): NativeConfig = withSourceLevelDebuggingConfig(_ => config)

  /** Update [[NativeConfig]] with given [[SourceLevelDebuggingConfig]] */
  def withSourceLevelDebuggingConfig(
      mapping: Mapping[SourceLevelDebuggingConfig]
  ): NativeConfig

  /** List of service providers which shall be allowed in the final binary */
  def serviceProviders: Map[ServiceName, Iterable[ServiceProviderName]]

  private[scalanative] lazy val configuredOrDetectedTriple =
    TargetTriple.parse(targetTriple.getOrElse(Discover.targetTriple(this)))

  /** Are we targeting a 32-bit platform?
   *
   *  @return
   *    true if 32 bit, false if 64 bit, unknown, or 16 bit
   */
  def is32BitPlatform = {
    import TargetTriple._
    val arch = configuredOrDetectedTriple.arch
    if (isArch32Bit(arch)) true
    else if (isArch64Bit(arch)) false
    else {
      println(
        s"Unexpected architecture in target triple: ${arch}, defaulting to 64-bit"
      )
      false
    }
  }

  // update methods - order as properties above

  /** Create a new config with given path to clang. */
  def withClang(value: Path): NativeConfig

  /** Create a new config with given path to clang++. */
  def withClangPP(value: Path): NativeConfig

  /** Create a new config with given linking options. */
  final def withLinkingOptions(value: Seq[String]): NativeConfig =
    withLinkingOptions(_ => value)

  /** Create a new config with updated linking options. */
  def withLinkingOptions(update: Mapping[Seq[String]]): NativeConfig

  /** Create a new config with given compilation options. */
  final def withCompileOptions(value: Seq[String]): NativeConfig =
    withCompileOptions(_ => value)

  /** Create a new config with updated compilation options. */
  def withCompileOptions(update: Mapping[Seq[String]]): NativeConfig

  /** Create a new config with custom C std. */
  def withCompileStdC(value: String): NativeConfig

  /** Create a new config with custom C++ std. */
  def withCompileStdCpp(value: String): NativeConfig

  /** Create a new config given a target triple. */
  def withTargetTriple(value: Option[String]): NativeConfig

  /** Create a new config given a target triple. Delegates to
   *  [[#withTargetTriple(value:Option[String])* withTargetTriple(Option[String])]].
   *
   *  @param value
   *    target triple as a String
   *  @return
   *    a new NativeConfig with a new target triple
   */
  def withTargetTriple(value: String): NativeConfig

  /** Create a new config with given garbage collector. */
  def withGC(value: GC): NativeConfig

  /** Create a new config with the given lto mode. */
  def withLTO(value: LTO): NativeConfig

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): NativeConfig

  /** Create a new config with given build target */
  def withBuildTarget(target: BuildTarget): NativeConfig

  /** Create a new config with given check value. */
  def withCheck(value: Boolean): NativeConfig

  /** Create a new config with given checkFatalWarnings value. */
  def withCheckFatalWarnings(value: Boolean): NativeConfig

  /** Create a new config with given checkFeatures value. */
  def withCheckFeatures(value: Boolean): NativeConfig

  /** Create a new config with given dump value. */
  def withDump(value: Boolean): NativeConfig

  /** Create a new config with given sanitizer enabled. */
  def withSanitizer(value: Sanitizer): NativeConfig = withSanitizer(Some(value))

  /** Create a new config with given sanitizer enabled. */
  def withSanitizer(value: Option[Sanitizer]): NativeConfig

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): NativeConfig

  /** Create a new config with given optimize value */
  def withOptimize(value: Boolean): NativeConfig

  /** Create a new config with given incrementalCompilation value */
  def withIncrementalCompilation(value: Boolean): NativeConfig

  /** Create a new config with support for multithreading */
  def withMultithreading(enabled: Boolean): NativeConfig

  /** Create a new config with support for multithreading */
  def withMultithreading(defined: Option[Boolean]): NativeConfig

  /** Create a new config with given linktime properites */
  final def withLinktimeProperties(
      value: NativeConfig.LinktimeProperites
  ): NativeConfig = withLinktimeProperties(_ => value)

  /** Create a new config with updated linktime properites */
  def withLinktimeProperties(
      update: Mapping[NativeConfig.LinktimeProperites]
  ): NativeConfig

  /** Create a new [[NativeConfig]] enabling embedded resources in the
   *  executable with a value of `true` where `false` is default.
   */
  def withEmbedResources(value: Boolean): NativeConfig

  /** Create a new [[NativeConfig]] with updated resource include patterns. */
  def withResourceIncludePatterns(value: Seq[String]): NativeConfig

  /** Create a new [[NativeConfig]] with updated resource exclude patterns. */
  def withResourceExcludePatterns(value: Seq[String]): NativeConfig

  /** Create a new [[NativeConfig]] with an updated list of service providers
   *  allowed in the final binary
   */
  def withServiceProviders(
      value: Map[ServiceName, Iterable[ServiceProviderName]]
  ): NativeConfig

  /** Create a new config with given base artifact name.
   *
   *  Warning: must be unique across project modules.
   */
  def withBaseName(value: String): NativeConfig

  /** Create an optimization configuration */
  final def withOptimizerConfig(value: OptimizerConfig): NativeConfig =
    withOptimizerConfig(_ => value)

  /** Modify a optimization configuration */
  def withOptimizerConfig(update: Mapping[OptimizerConfig]): NativeConfig

  /** Create a semantics configuration */
  final def withSemanticsConfig(value: SemanticsConfig): NativeConfig =
    withSemanticsConfig(_ => value)

  /** Modify a semantics configuration */
  def withSemanticsConfig(update: Mapping[SemanticsConfig]): NativeConfig
}

object NativeConfig {
  type Mapping[T] = T => T
  type LinktimeProperites = Map[String, Any]
  type ServiceName = String
  type ServiceProviderName = String

  /** Default empty config object where all of the fields are left blank. */
  def empty: NativeConfig =
    Impl(
      clang = Paths.get(""),
      clangPP = Paths.get(""),
      linkingOptions = Seq.empty,
      compileOptions = Seq.empty,
      compileStdC = None,
      compileStdCpp = None,
      targetTriple = None,
      gc = GC.default,
      lto = LTO.default,
      mode = Mode.default,
      buildTarget = BuildTarget.default,
      check = false,
      checkFatalWarnings = false,
      checkFeatures = true,
      dump = false,
      sanitizer = None,
      linkStubs = false,
      optimize = true,
      useIncrementalCompilation = true,
      multithreading = None, // detect
      linktimeProperties = Map.empty,
      embedResources = false,
      resourceIncludePatterns = Seq("**"),
      resourceExcludePatterns = Seq.empty,
      serviceProviders = Map.empty,
      baseName = "",
      optimizerConfig = OptimizerConfig.empty,
      sourceLevelDebuggingConfig = SourceLevelDebuggingConfig.disabled,
      semanticsConfig = SemanticsConfig.default
    )

  private final case class Impl(
      clang: Path,
      clangPP: Path,
      linkingOptions: Seq[String],
      compileOptions: Seq[String],
      compileStdC: Option[String],
      compileStdCpp: Option[String],
      targetTriple: Option[String],
      gc: GC,
      lto: LTO,
      mode: Mode,
      buildTarget: BuildTarget,
      check: Boolean,
      checkFatalWarnings: Boolean,
      checkFeatures: Boolean,
      dump: Boolean,
      sanitizer: Option[Sanitizer],
      linkStubs: Boolean,
      optimize: Boolean,
      useIncrementalCompilation: Boolean,
      multithreading: Option[Boolean],
      linktimeProperties: LinktimeProperites,
      embedResources: Boolean,
      resourceIncludePatterns: Seq[String],
      resourceExcludePatterns: Seq[String],
      serviceProviders: Map[ServiceName, Iterable[ServiceProviderName]],
      baseName: String,
      optimizerConfig: OptimizerConfig,
      sourceLevelDebuggingConfig: SourceLevelDebuggingConfig,
      semanticsConfig: SemanticsConfig
  ) extends NativeConfig {

    def withClang(value: Path): NativeConfig =
      copy(clang = value)

    def withClangPP(value: Path): NativeConfig =
      copy(clangPP = value)

    def withLinkingOptions(update: Mapping[Seq[String]]): NativeConfig =
      copy(linkingOptions = update(linkingOptions).map(_.trim()))

    def withCompileOptions(update: Mapping[Seq[String]]): NativeConfig =
      copy(compileOptions = update(compileOptions).map(_.trim()))

    def withCompileStdC(value: String): NativeConfig =
      copy(compileStdC = Some(value))

    def withCompileStdCpp(value: String): NativeConfig =
      copy(compileStdCpp = Some(value))

    def withTargetTriple(value: Option[String]): NativeConfig = {
      val propertyName = "target.triple"
      value match {
        case Some(triple) => System.setProperty(propertyName, triple)
        case None         => System.clearProperty(propertyName)
      }
      copy(targetTriple = value)
    }

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

    def withCheckFeatures(value: Boolean): NativeConfig =
      copy(checkFeatures = value)

    def withDump(value: Boolean): NativeConfig =
      copy(dump = value)

    def withSanitizer(value: Option[Sanitizer]): NativeConfig =
      copy(sanitizer = value)

    def withOptimize(value: Boolean): NativeConfig =
      copy(optimize = value)

    override def withIncrementalCompilation(value: Boolean): NativeConfig =
      copy(useIncrementalCompilation = value)

    def withMultithreading(enabled: Boolean): NativeConfig =
      copy(multithreading = Some(enabled))

    def withMultithreading(defined: Option[Boolean]): NativeConfig =
      copy(multithreading = defined)

    def withLinktimeProperties(
        update: Mapping[LinktimeProperites]
    ): NativeConfig = {
      val v = update(linktimeProperties)
      checkLinktimeProperties(v)
      copy(linktimeProperties = v)
    }

    def withEmbedResources(value: Boolean): NativeConfig = {
      copy(embedResources = value)
    }

    def withResourceIncludePatterns(value: Seq[String]): NativeConfig = {
      copy(resourceIncludePatterns = value)
    }

    def withResourceExcludePatterns(value: Seq[String]): NativeConfig = {
      copy(resourceExcludePatterns = value)
    }

    def withServiceProviders(
        value: Map[ServiceName, Iterable[ServiceProviderName]]
    ): NativeConfig = {
      copy(serviceProviders = value)
    }

    def withBaseName(value: String): NativeConfig = {
      copy(baseName = value)
    }

    override def withOptimizerConfig(
        update: Mapping[OptimizerConfig]
    ): NativeConfig = {
      copy(optimizerConfig = update(optimizerConfig))
    }

    override def withSourceLevelDebuggingConfig(
        update: Mapping[SourceLevelDebuggingConfig]
    ): NativeConfig =
      copy(sourceLevelDebuggingConfig = update(sourceLevelDebuggingConfig))

    override def withSemanticsConfig(
        update: Mapping[SemanticsConfig]
    ): NativeConfig = copy(semanticsConfig = update(semanticsConfig))

    override def toString: String = {
      def showSeq(it: Iterable[Any]) = it.mkString("[", ", ", "]")
      def showMap(map: Map[String, Any], indent: Int = 4): String =
        if (map.isEmpty) "[]"
        else {
          val maxKeyLength = map.keys.map(_.length).max
          val keyPadSize = maxKeyLength.min(20)
          val indentPad = " " * indent
          "\n" + map.toSeq
            .sortBy(_._1)
            .map {
              case (key, value) =>
                val valueString = value match {
                  case seq: Iterable[_] => showSeq(seq)
                  case v                => v.toString()
                }
                s"$indentPad- ${key.padTo(keyPadSize, ' ')}: $valueString"
            }
            .mkString("\n")
        }

      s"""NativeConfig(
        | - baseName:                $baseName
        | - clang:                   $clang
        | - clangPP:                 $clangPP
        | - linkingOptions:          ${showSeq(linkingOptions)}
        | - compileOptions:          ${showSeq(compileOptions)}
        | - compileStdC:             $compileStdC
        | - compileStdCpp:           $compileStdCpp
        | - targetTriple:            $targetTriple
        | - GC:                      $gc
        | - LTO:                     $lto
        | - mode:                    $mode
        | - buildTarget              $buildTarget
        | - check:                   $check
        | - checkFatalWarnings:      $checkFatalWarnings
        | - checkFeatures            $checkFeatures
        | - dump:                    $dump
        | - sanitizer:               ${sanitizer.map(_.name).getOrElse("none")}
        | - linkStubs:               $linkStubs
        | - optimize                 $optimize
        | - incrementalCompilation:  $useIncrementalCompilation
        | - multithreading           $multithreading
        | - linktimeProperties:      ${showMap(linktimeProperties)}
        | - embedResources:          $embedResources
        | - resourceIncludePatterns: ${showSeq(resourceIncludePatterns)}
        | - resourceExcludePatterns: ${showSeq(resourceExcludePatterns)}
        | - serviceProviders:        ${showMap(serviceProviders)}
        | - optimizerConfig:         ${optimizerConfig.show(" " * 4)}
        | - semanticsConfig:         ${semanticsConfig.show(" " * 4)}
        | - sourceLevelDebuggingConfig: ${sourceLevelDebuggingConfig.show(
          " " * 4
        )}
        |)""".stripMargin
    }
  }

  private[scalanative] def checkLinktimeProperties(
      properties: LinktimeProperites
  ): Unit = {
    def isNumberOrString(value: Any) = {
      value match {
        case _: Boolean | _: Byte | _: Char | _: Short | _: Int | _: Long |
            _: Float | _: Double | _: String | _: nir.Val =>
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
