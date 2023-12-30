package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins._
import dotty.tools.dotc.report
import dotty.tools.dotc.core.Contexts.NoContext
import java.net.URI
import java.net.URISyntaxException
import dotty.tools.dotc.core.Contexts.Context

class ScalaNativePlugin extends StandardPlugin:
  val name: String = "scalanative"
  val description: String = "Scala Native compiler plugin"

  override val optionsHelp: Option[String] =
    Some(s"""
      |  -P:$name:genStaticForwardersForNonTopLevelObjects
      |     Generate static forwarders for non-top-level objects.
      |     This option should be used by codebases that implement JDK classes.
      |     When used together with -Xno-forwarders, this option has no effect.
      """.stripMargin)

  override def init(options: List[String]): List[PluginPhase] = {
    val genNirSettings = options
      .foldLeft(GenNIR.Settings()) {
        case (config, "genStaticForwardersForNonTopLevelObjects") =>
          config.copy(genStaticForwardersForNonTopLevelObjects = true)
        case (config, s"mapSourceURI:${mapping}") =>
          given Context = NoContext
          report.warning("'mapSourceURI' is deprecated, it's ignored.")
          config
        case (config, _) => config
      }
    List(PrepNativeInterop(), PostInlineNativeInterop(), GenNIR(genNirSettings))
  }
