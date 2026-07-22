package scala.scalanative
package linker

import scala.scalanative.build.ScalaNativeTracer
import scalanative.util.Scope

object Link {

  /** Load all clases and methods reachable from the entry points. */
  def apply(
      config: build.Config,
      entries: Seq[nir.Global],
      tracer: ScalaNativeTracer
  )(implicit
      scope: Scope
  ): ReachabilityAnalysis =
    Reach(config, entries, ClassLoader.fromDisk(config), tracer)

  /** Run reachability analysis on already loaded methods. */
  def apply(
      config: build.Config,
      entries: Seq[nir.Global],
      defns: Seq[nir.Defn],
      tracer: ScalaNativeTracer
  ): ReachabilityAnalysis =
    Reach(config, entries, ClassLoader.fromMemory(defns), tracer)

}
