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

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** The LTO mode to use used during a release build. */
  def LTO: LTO

  /** Shall linker check that NIR is well-formed after every phase? */
  def check: Boolean

  /** Shall linker dump intermediate NIR after every phase? */
  def dump: Boolean

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

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): NativeConfig

  /** Create a new config with the given lto mode. */
  def withLTO(value: LTO): NativeConfig

  /** Create a new config with given check value. */
  def withCheck(value: Boolean): NativeConfig

  /** Create a new config with given dump value. */
  def withDump(value: Boolean): NativeConfig
}

object NativeConfig {

  /** Default empty config object where all of the fields are left blank. */
  def empty: NativeConfig =
    Impl(
      clang = Paths.get(""),
      clangPP = Paths.get(""),
      linkingOptions = Discover.linkingOptions(),
      compileOptions = Discover.compileOptions(),
      gc = Discover.GC(),
      LTO = Discover.LTO(),
      mode = Discover.mode(),
      check = false,
      dump = false,
      linkStubs = false
    )

  private final case class Impl(clang: Path,
                                clangPP: Path,
                                linkingOptions: Seq[String],
                                compileOptions: Seq[String],
                                gc: GC,
                                mode: Mode,
                                LTO: LTO,
                                linkStubs: Boolean,
                                check: Boolean,
                                dump: Boolean)
      extends NativeConfig {

    def withClang(value: Path): NativeConfig =
      copy(clang = value)

    def withClangPP(value: Path): NativeConfig =
      copy(clangPP = value)

    def withLinkingOptions(value: Seq[String]): NativeConfig =
      copy(linkingOptions = value)

    def withCompileOptions(value: Seq[String]): NativeConfig =
      copy(compileOptions = value)

    def withGC(value: GC): NativeConfig =
      copy(gc = value)

    def withMode(value: Mode): NativeConfig =
      copy(mode = value)

    def withLinkStubs(value: Boolean): NativeConfig =
      copy(linkStubs = value)

    def withLTO(value: LTO): NativeConfig =
      copy(LTO = value)

    def withCheck(value: Boolean): NativeConfig =
      copy(check = value)

    def withDump(value: Boolean): NativeConfig =
      copy(dump = value)

    override def toString: String =
      s"""NativeConfig(
         | - clang:           $clang
         | - clangPP:         $clangPP
         | - linkingOptions:  $linkingOptions
         | - compileOptions:  $compileOptions
         | - GC:              $gc
         | - mode:            $mode
         | - LTO:             $LTO
         | - linkStubs:       $linkStubs
         | - check:           $check
         | - dump:            $dump
         |)""".stripMargin
  }

}
