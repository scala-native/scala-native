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

  /** Should a main method be injected? */
  def injectMain: Boolean

  /** Create new config with given entry point. */
  def withEntry(value: Global): Config

  /** Create a new config with given nir paths. */
  def withPaths(value: Seq[LinkerPath]): Config

  /** Create a new config with given directory. */
  def withTargetDirectory(value: VirtualDirectory): Config

  /** Create a new config with given inject main flag. */
  def withInjectMain(value: Boolean): Config
}

object Config {

  /** Default empty config object. */
  val empty: Config =
    Impl(entry = Global.None,
         paths = Seq.empty,
         targetDirectory = VirtualDirectory.empty,
         injectMain = true)

  private final case class Impl(entry: Global,
                                paths: Seq[LinkerPath],
                                targetDirectory: VirtualDirectory,
                                injectMain: Boolean)
      extends Config {
    def withEntry(value: Global): Config =
      copy(entry = value)

    def withPaths(value: Seq[LinkerPath]): Config =
      copy(paths = value)

    def withTargetDirectory(value: VirtualDirectory): Config =
      copy(targetDirectory = value)

    def withInjectMain(value: Boolean): Config =
      copy(injectMain = value)
  }
}
