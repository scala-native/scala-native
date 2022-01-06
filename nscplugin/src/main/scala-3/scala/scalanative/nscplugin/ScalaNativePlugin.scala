package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins._

class ScalaNativePlugin extends StandardPlugin:
  val name: String = "scalanative"
  val description: String = "Scala Native compiler plugin"

  def init(options: List[String]): List[PluginPhase] = {
    val genNirSettings = options
      .foldLeft(GenNIR.Settings()) {
        case (config, "genStaticForwardersForNonTopLevelObjects") =>
          config.copy(genStaticForwardersForNonTopLevelObjects = true)
        case (config, _) => config
      }
    List(PrepNativeInterop(), GenNIR(genNirSettings))
  }
