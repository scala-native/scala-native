package scala.scalanative
package optimizer

import build.Config
import sema.Top
import nir.{Global, Defn}

trait PassCompanion {

  /** Instantiate the given pass. */
  def apply(config: Config, top: Top): Pass
}
