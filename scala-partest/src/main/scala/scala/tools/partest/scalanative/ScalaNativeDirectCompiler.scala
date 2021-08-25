package scala.tools.partest.scalanative

import scala.scalanative.nscplugin.NirPlugin
import scala.tools.nsc.Settings
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.reporters.Reporter
import scala.tools.partest.nest.{DirectCompiler, PartestGlobal}

trait ScalaNativeDirectCompiler extends DirectCompiler {
  override def newGlobal(
      settings: Settings,
      reporter: Reporter
  ): PartestGlobal = {
    new PartestGlobal(settings, reporter) {
      override protected def loadRoughPluginsList(): List[Plugin] = {
        super.loadRoughPluginsList() :+
          Plugin.instantiate(classOf[NirPlugin], this)
      }
    }
  }
}
