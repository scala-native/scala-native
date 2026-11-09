package scala.tools.partest.scalanative

import scala.tools.nsc.Settings
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.util.ClassPath
import scala.tools.partest.nest.{DirectCompiler, PartestGlobal}

import scala.scalanative.nscplugin.ScalaNativePlugin

trait ScalaNativeDirectCompiler extends DirectCompiler {
  override def newGlobal(
      settings: Settings,
      reporter: Reporter
  ): PartestGlobal = {
    // Explicit -classpath from PartestTask drops outDir needed by separate compilation
    settings.classpath.value =
      ClassPath.join(settings.classpath.value, runner.outDir.getPath)
    new PartestGlobal(settings, reporter) {
      override protected def loadRoughPluginsList(): List[Plugin] = {
        super.loadRoughPluginsList() :+
          Plugin.instantiate(classOf[ScalaNativePlugin], this)
      }
    }
  }
}
