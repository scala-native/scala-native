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
      |  -P:$name:mapSourceURI:FROM_URI[->TO_URI]
      |     Change the location the source URIs in the emitted IR point to
      |     - strips away the prefix FROM_URI (if it matches)
      |     - optionally prefixes the TO_URI, where stripping has been performed
      |     - any number of occurrences are allowed. Processing is done on a first match basis.
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
          val uris = mapping.split("->")
          if uris.length != 1 && uris.length != 2
          then
            report.error(
              s"mapSourceUri needs one or two URIs as argument, got '$mapping'"
            )
            config
          else {
            try
              val from = new URI(uris.head)
              val to = uris.lift(1).map(str => new URI(str))
              val sourceMaps = config.sourceURIMaps :+ GenNIR.URIMap(from, to)
              config.copy(sourceURIMaps = sourceMaps)
            catch
              case e: URISyntaxException =>
                report.error(s"${e.getInput} is not a valid URI")
                config
          }
        case (config, _) => config
      }
    List(PrepNativeInterop(), PostInlineNativeInterop(), GenNIR(genNirSettings))
  }
