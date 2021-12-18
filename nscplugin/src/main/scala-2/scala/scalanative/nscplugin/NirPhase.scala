package scala.scalanative.nscplugin

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.PluginComponent

abstract class NirPhase[G <: Global with Singleton](val global: G)
    extends PluginComponent
    with NirCompat[G] {

  /** Not for use in the constructor body: only initialized afterwards. */
  val nirAddons: NirGlobalAddons {
    val global: NirPhase.this.global.type
  }

}
