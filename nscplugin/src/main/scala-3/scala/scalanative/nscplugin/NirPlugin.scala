package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins._

class NirPlugin extends StandardPlugin:
  val name: String = "NirPlugin"
  val description: String = "Scala Native compiler plugin"

  def init(options: List[String]): List[PluginPhase] = {
    val genNirSettings = options
      .foldLeft(GenNIR.Settings()) {
        case (config, "GenStaticForwardersForNonTopLevelObjects") =>
          config.copy(genStaticForwardersForNonTopLevelObjects = true)
        case (config, _) => config
      }
    List(PrepNativeInterop, AdaptLazyVals, GenNIR(genNirSettings))
  }
