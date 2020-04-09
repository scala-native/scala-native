package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirGlobalAddons extends NirDefinitions {
  val global: Global

  object nirPrimitives extends NirPrimitives {
    val global: NirGlobalAddons.this.global.type = NirGlobalAddons.this.global
    val nirAddons: ThisNirGlobalAddons =
      NirGlobalAddons.this.asInstanceOf[ThisNirGlobalAddons]
  }
}
