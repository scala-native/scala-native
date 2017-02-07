package scala.scalanative
package optimizer

import tools.Config
import analysis.ClassHierarchy.Top
import nir.{Global, Defn}

trait AnyPassCompanion {

  /** Is it a factory for an injection pass? */
  def isInjectionPass: Boolean

  /** Instantiate the given pass. */
  def apply(config: Config, top: Top): AnyPass

  /** A sequence of extra dependencies that should be
   *  loaded by linker if given pass is enabled.
   */
  def depends: Seq[Global] = Seq()

  /** A sequence of extra definitions that should be
   *  injected into final assembly if given pass is enabled.
   */
  def injects: Seq[Defn] = Seq()
}

trait InjectCompanion extends AnyPassCompanion {

  override def isInjectionPass = true
}

trait PassCompanion extends AnyPassCompanion {

  override def isInjectionPass = false
}
