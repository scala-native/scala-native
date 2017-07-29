package scala.scalanative.nscplugin


import scala.tools.nsc.plugins._

trait NirPluginComponent { self: PluginComponent =>

  val nirAddons: NirGlobalAddons {
    val global: NirPluginComponent.this.global.type
  }

}
