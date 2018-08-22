package scala.scalanative
package optimizer

trait PassCompanion {

  /** Instantiate the given pass. */
  def apply(config: build.Config, linked: linker.Result): Pass
}
