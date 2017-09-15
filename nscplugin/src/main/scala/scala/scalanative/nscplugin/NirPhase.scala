package scala.scalanative
package nscplugin

import scala.tools.nsc.plugins.PluginComponent

trait NirPhase extends PluginComponent
  with NirGenType
  with NirGenName {

  val nirAddons: NirGlobalAddons {
    val global: NirPhase.this.global.type
  }

}
