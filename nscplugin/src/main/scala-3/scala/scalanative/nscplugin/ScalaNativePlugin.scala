package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.*
import dotty.tools.dotc.report
import dotty.tools.dotc.core.Contexts.ContextBase
import java.net.URI
import java.net.URISyntaxException
import dotty.tools.dotc.core.Contexts.Context
import java.nio.file.Paths
import scala.annotation.nowarn

class ScalaNativePlugin extends StandardPlugin:
  val name: String = "scalanative"
  val description: String = "Scala Native compiler plugin"

  override val optionsHelp: Option[String] =
    Some(s"""
      |  -P:$name:genStaticForwardersForNonTopLevelObjects
      |     Generate static forwarders for non-top-level objects.
      |     This option should be used by codebases that implement JDK classes.
      |     When used together with -Xno-forwarders, this option has no effect.
      |  -P:$name:forceStrictFinalFields
      |     Treat all final fields as if they we're marked with @safePublish.
      |     This option should be used by codebased that rely heavily on Java Final Fields semantics
      |     It should not be required by most of normal Scala code.
      |  -P:$name:positionRelativizationPaths
      |     Change the source file positions in generated outputs based on list of provided paths.
      |     It would strip the prefix of the source file if it matches given path.
      |     Non-absolute paths would be ignored.
      |     Multiple paths should be separated by a single semicolon ';' character.
      |     If none of the patches matches path would be relative to -sourcepath if defined or -sourceroot otherwise.
      """.stripMargin)

  @nowarn("cat=deprecation")
  override def init(options: List[String]): List[PluginPhase] = {
    val genNirSettings = options
      .foldLeft(GenNIR.Settings()) {
        case (config, "genStaticForwardersForNonTopLevelObjects") =>
          config.copy(genStaticForwardersForNonTopLevelObjects = true)
        case (config, "forceStrictFinalFields") =>
          config.copy(forceStrictFinalFields = true)
        case (config, s"positionRelativizationPaths:${paths}") =>
          config.copy(positionRelativizationPaths =
            (config.positionRelativizationPaths ++ paths
              .split(';')
              .map(Paths.get(_))
              .filter(_.isAbsolute())).distinct.sortBy(-_.getNameCount())
          )
        case (config, s"mapSourceURI:${mapping}") =>
          given Context = ContextBase().initialCtx
          report.warning("'mapSourceURI' is deprecated, it's ignored.")
          config
        case (config, _) => config
      }
    List(PrepNativeInterop(), PostInlineNativeInterop(), GenNIR(genNirSettings))
  }
