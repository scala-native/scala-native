package scala.scalanative
package linker

import scalanative.nir._
import scalanative.util.{Stats, Scope}

object Link {

  /** Create a new linker given tools configuration. */
  def apply(config: build.Config, entries: Seq[Global]): Result =
    Scope { implicit in =>
      Stats.in(Stats.time("reach") {
        Reach(entries, ClassLoader(config))
      })
    }
}
