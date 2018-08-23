package scala.scalanative
package linker

import scalanative.nir._
import scalanative.util.{Stats, Scope}

object Link {

  /** Load all clases and methods reachable from the entry points. */
  def apply(config: build.Config, entries: Seq[Global]): Result =
    Scope { implicit in =>
      Reach(entries, ClassLoader.fromDisk(config))
    }
}
