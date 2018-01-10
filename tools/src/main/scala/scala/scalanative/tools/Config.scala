package scala.scalanative
package tools

import java.nio.file.{Path, Paths}

import nir.Global

sealed trait Config {

  /** Entry point for linking. */
  def entry: Global

  /** Sequence of all NIR locations. */
  def paths: Seq[Path]

  /** Directory to emit intermediate compilation results. */
  def workdir: Path

  /** Target triple. */
  def target: String

  /** Compilation mode. */
  def mode: Mode

  /** Should stubs be linked? */
  def linkStubs: Boolean

  /** Create new config with given entry point. */
  def withEntry(value: Global): Config

  /** Create a new config with given nir paths. */
  def withPaths(value: Seq[Path]): Config

  /** Create a new config with given directory. */
  def withWorkdir(value: Path): Config

  /** Create a new config with given target triple. */
  def withTarget(value: String): Config

  /** Create a new config with given compilation mode. */
  def withMode(value: Mode): Config

  /** Create a new config with given behavior for stubs. */
  def withLinkStubs(value: Boolean): Config
}

object Config {

  /** Default empty config object. */
  val empty: Config =
    Impl(
      entry = Global.None,
      paths = Seq.empty,
      workdir = Paths.get(""),
      target = "",
      mode = Mode.Debug,
      linkStubs = false
    )

  private final case class Impl(entry: Global,
                                paths: Seq[Path],
                                workdir: Path,
                                target: String,
                                mode: Mode,
                                linkStubs: Boolean)
      extends Config {
    def withEntry(value: Global): Config =
      copy(entry = value)

    def withPaths(value: Seq[Path]): Config =
      copy(paths = value)

    def withWorkdir(value: Path): Config =
      copy(workdir = value)

    def withTarget(value: String): Config =
      copy(target = value)

    def withMode(value: Mode): Config =
      copy(mode = value)

    def withLinkStubs(value: Boolean): Config =
      copy(linkStubs = value)
  }
}
