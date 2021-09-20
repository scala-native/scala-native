package scala.scalanative
package build

import java.nio.file.{Path, Paths}

/** An object describing how to configure the Scala Native toolchain. */
sealed trait NativeConfig {

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

  /** Shall we optimize the resulting NIR code? */
  def optimize: Boolean

  /** Map of properties resolved at linktime */
  def linktimeProperties: Map[String, Any]

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

  /** Create a new config with given optimize value */
  def withOptimize(value: Boolean): NativeConfig

  /** Create a new config with given linktime properites */
  def withLinktimeProperties(value: NativeConfig.LinktimeProperites): NativeConfig
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
      check = false,
      checkFatalWarnings = false,
      dump = false,
      linkStubs = false,
      optimize = false,
      customLinktimeProperties = Map.empty
    )

  private final case class Impl(
      clang: Path,
      clangPP: Path,
      linkingOptions: Seq[String],
      compileOptions: Seq[String],
      targetTriple: Option[String],
      gc: GC,
      mode: Mode,
      lto: LTO,
      linkStubs: Boolean,
      check: Boolean,
      checkFatalWarnings: Boolean,
      dump: Boolean,
      optimize: Boolean,
      customLinktimeProperties: LinktimeProperites
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

    def withOptimize(value: Boolean): NativeConfig =
      copy(optimize = value)

    def linktimeProperties: Map[String, Any] = {
      predefinedLinktimeProperties(this) ++ customLinktimeProperties
    }

    override def withLinktimeProperties(v: Map[String, Any]): NativeConfig = {
      checkLinktimeProperties(v)
      copy(customLinktimeProperties = v)
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
        | - clang:              $clang
        | - clangPP:            $clangPP
        | - linkingOptions:     $linkingOptions
        | - compileOptions:     $compileOptions
        | - targetTriple:       $targetTriple
        | - GC:                 $gc
        | - mode:               $mode
        | - LTO:                $lto
        | - linkStubs:          $linkStubs
        | - check:              $check
        | - checkFatalWarnings: $checkFatalWarnings
        | - dump:               $dump
        | - optimize            $optimize
        | - linktimeProperties: $listLinktimeProperties
        |)""".stripMargin
    }
  }

  def predefinedLinktimeProperties(config: NativeConfig): LinktimeProperites = {
    val linktimeInfo = "scala.scalanative.meta.linktimeinfo"
    Map(
      s"$linktimeInfo.isWindows" -> Platform.isWindows
    )
  }

  def checkLinktimeProperties(properties: LinktimeProperites): Unit = {
    def isNumberOrString(value: Any) = {
      def hasSupportedType = value match {
        case _: Boolean | _: Byte | _: Char | _: Short | _: Int | _: Long |
             _: Float | _: Double | _: String =>
          true
        case _ => false
      }

      value != null && hasSupportedType
    }

    val invalid = properties.collect {
      case (key, value) if !isNumberOrString(value) => key
    }
    if (invalid.nonEmpty) {
      System.err.println(
        s"Invalid link-time properties: \n ${invalid.mkString(" - ", "\n", "")}"
      )
      throw new BuildException(
        "Link-time properties needs to be non-null primitives or non-empty string"
      )
    }
  }

}
