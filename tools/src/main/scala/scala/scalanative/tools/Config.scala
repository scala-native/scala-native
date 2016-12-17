package scala.scalanative
package tools

import java.io.File

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

  /** Inject instructions to profile at runtime */
  def enableProfiling: Boolean

  /** Where to write the collected profiling information. */
  def profilingLocation: File

  /** Create new config with given entry point. */
  def withEntry(value: Global): Config

  /** Create a new config with given nir paths. */
  def withPaths(value: Seq[LinkerPath]): Config

  /** Create a new config with given directory. */
  def withTargetDirectory(value: VirtualDirectory): Config

  /** Create a new config with given inject main flag. */
  def withInjectMain(value: Boolean): Config

  /** Create a new config with given profiling flag. */
  def withEnableProfiling(value: Boolean): Config

  /** Create a new config specifying where to put the profiling info. */
  def withProfilingLocation(value: File): Config
}

object Config {

  /** Default empty config object. */
  val empty: Config =
    Impl(entry = Global.None,
         paths = Seq.empty,
         targetDirectory = VirtualDirectory.empty,
         injectMain = true,
         enableProfiling = false,
         profilingLocation = new File("/dev/null"))

  private final case class Impl(entry: Global,
                                paths: Seq[LinkerPath],
                                targetDirectory: VirtualDirectory,
                                injectMain: Boolean,
                                enableProfiling: Boolean,
                                profilingLocation: File)
      extends Config {
    def withEntry(value: Global): Config =
      copy(entry = value)

    def withPaths(value: Seq[LinkerPath]): Config =
      copy(paths = value)

    def withTargetDirectory(value: VirtualDirectory): Config =
      copy(targetDirectory = value)

    def withInjectMain(value: Boolean): Config =
      copy(injectMain = value)

    def withEnableProfiling(value: Boolean): Config =
      copy(enableProfiling = value)

    def withProfilingLocation(value: File): Config =
      copy(profilingLocation = value)
  }
}
