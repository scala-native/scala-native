package scala.scalanative
package tools

import scalanative.io.VirtualDirectory
import nir.Global

sealed trait Config {

  /** Entry point for linking. */
  def entry: Global

  /** Sequence of all NIR locations. */
  def paths: Seq[LinkerPath]

  /** Directory to emit intermediate compilation results. */
  def targetDirectory: VirtualDirectory

  /** Target triple. */
  def target: String

  /** Compilation mode. */
  def mode: Mode

  /** Create new config with given entry point. */
  def withEntry(value: Global): Config

  /** Create a new config with given nir paths. */
  def withPaths(value: Seq[LinkerPath]): Config

  /** Create a new config with given directory. */
  def withTargetDirectory(value: VirtualDirectory): Config

  /** Create a new config with given target triple. */
  def withTarget(value: String): Config

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): Config
}

object Config {

  /** Default empty config object. */
  val empty: Config =
    Impl(entry = Global.None,
         paths = Seq.empty,
         targetDirectory = VirtualDirectory.empty,
         target = "",
         mode = Mode.Debug)

  private final case class Impl(entry: Global,
                                paths: Seq[LinkerPath],
                                targetDirectory: VirtualDirectory,
                                target: String,
                                mode: Mode)
      extends Config {
    def withEntry(value: Global): Config =
      copy(entry = value)

    def withPaths(value: Seq[LinkerPath]): Config =
      copy(paths = value)

    def withTargetDirectory(value: VirtualDirectory): Config =
      copy(targetDirectory = value)

    def withTarget(value: String): Config =
      copy(target = value)

    def withMode(value: Mode): Config =
      copy(mode = value)
  }
}
