package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._

class NirPlugin(val global: Global) extends Plugin {
  val name        = "nir"
  val description = "Compile to Scala Native IR (NIR)"
  val components: List[PluginComponent] = global match {
    case _: doc.ScaladocGlobal => List(prepNativeInterop)
    case _                     => List(prepNativeInterop, nirGen)
  }

  /** A trick to avoid early initializers while still enforcing that `global`
   *  is initialized early.
   */
  abstract class NirGlobalAddonsEarlyInit[G <: Global with Singleton](
      val global: G)
      extends NirGlobalAddons

  object nirAddons extends NirGlobalAddonsEarlyInit[global.type](global)

  object prepNativeInterop extends PrepNativeInterop[global.type](global) {
    val nirAddons: NirPlugin.this.nirAddons.type = NirPlugin.this.nirAddons
    override val runsAfter                       = List("typer")
    override val runsBefore                      = List("pickler")
  }

  object nirGen extends NirGenPhase[global.type](global) {
    val nirAddons: NirPlugin.this.nirAddons.type = NirPlugin.this.nirAddons
    override val runsAfter                       = List("mixin")
    override val runsBefore                      = List("delambdafy", "cleanup", "terminal")
  }
}
