package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirGlobalAddons extends NirDefinitions {
  val global: Global

  import global._
  import definitions._
  import nirDefinitions._

  object nirPrimitives extends NirPrimitives {
    val global: NirGlobalAddons.this.global.type = NirGlobalAddons.this.global
    val nirAddons: ThisNirGlobalAddons =
      NirGlobalAddons.this.asInstanceOf[ThisNirGlobalAddons]
  }
}
