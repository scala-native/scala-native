package scala.scalanative
package tools

import scalanative.io.VirtualDirectory
import nir.Global

import java.io.File

sealed trait Config {

  /** Entry point for linking. */
  def entry: Global

  /** Sequence of all NIR locations. */
  def paths: Seq[LinkerPath]

  /** Directory to emit intermediate compilation results. */
  def targetDirectory: VirtualDirectory

  /** Should a main method be injected? */
  def injectMain: Boolean

  /** Is virtual dispatch profiling enabled? */
  def profileDispatch: Boolean

  /** Where to put virtual dispatch info? */
  def profileDispatchInfo: Option[File]

  /** Maximum number of candidates to consider a call-site for inline caching */
  def inlineCachingMaxCandidates: Int

  /** Create new config with given entry point. */
  def withEntry(value: Global): Config

  /** Create a new config with given nir paths. */
  def withPaths(value: Seq[LinkerPath]): Config

  /** Create a new config with given directory. */
  def withTargetDirectory(value: VirtualDirectory): Config

  /** Create a new config with given inject main flag. */
  def withInjectMain(value: Boolean): Config

  /** Create a new config with virtual dispatch profiling enabled or disabled */
  def withProfileDispatch(value: Boolean): Config

  /** Create a new config where dispatch info is stored to the specified file */
  def withProfileDispatchInfo(value: Option[File]): Config

  /** Create a new config with a max number of candidates for inline caching */
  def withInlineCachingMaxCandidates(value: Int): Config
}

object Config {

  /** Default empty config object. */
  val empty: Config =
    Impl(entry = Global.None,
         paths = Seq.empty,
         targetDirectory = VirtualDirectory.empty,
         injectMain = true,
         profileDispatch = false,
         profileDispatchInfo = None,
         inlineCachingMaxCandidates = 2)

  private final case class Impl(entry: Global,
                                paths: Seq[LinkerPath],
                                targetDirectory: VirtualDirectory,
                                injectMain: Boolean,
                                profileDispatch: Boolean,
                                profileDispatchInfo: Option[File],
                                inlineCachingMaxCandidates: Int)
      extends Config {
    def withEntry(value: Global): Config =
      copy(entry = value)

    def withPaths(value: Seq[LinkerPath]): Config =
      copy(paths = value)

    def withTargetDirectory(value: VirtualDirectory): Config =
      copy(targetDirectory = value)

    def withInjectMain(value: Boolean): Config =
      copy(injectMain = value)

    def withProfileDispatch(value: Boolean): Config =
      copy(profileDispatch = value)

    def withProfileDispatchInfo(value: Option[File]): Config =
      copy(profileDispatchInfo = value)

    def withInlineCachingMaxCandidates(value: Int): Config =
      copy(inlineCachingMaxCandidates = value)
  }
}
