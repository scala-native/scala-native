package scala.scalanative
package linker

import scalanative.nir._
import scalanative.util.Scope

object Link {

  /** Load all clases and methods reachable from the entry points. */
  def apply(config: build.Config, entries: Seq[Global])(implicit
      scope: Scope
  ): ReachabilityAnalysis =
    Reach(config, entries, ClassLoader.fromDisk(config))

  /** Run reachability analysis on already loaded methods. */
  def apply(
      config: build.Config,
      entries: Seq[Global],
      defns: Seq[Defn]
  ): ReachabilityAnalysis =
    Reach(config, entries, ClassLoader.fromMemory(defns))
}
