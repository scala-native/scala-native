package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import java.nio.file.{Path, Paths}

class ScalaNativePlugin(val global: Global) extends Plugin {
  val name = "scalanative"
  val description = "Compile to Scala Native IR (NIR)"
  val components: List[PluginComponent] = global match {
    case _: doc.ScaladocGlobal => List(prepNativeInterop)
    case _                     => List(prepNativeInterop, nirGen)
  }

  /** A trick to avoid early initializers while still enforcing that `global` is
   *  initialized early.
   */
  abstract class NirGlobalAddonsEarlyInit[G <: Global with Singleton](
      val global: G
  ) extends NirGlobalAddons

  object nirAddons extends NirGlobalAddonsEarlyInit[global.type](global)

  object scalaNativeOpts extends ScalaNativeOptions {
    var genStaticForwardersForNonTopLevelObjects: Boolean = false
    var forceStrictFinalFields: Boolean = false
    var positionRelativizationPaths: Seq[Path] = Nil
  }

  object prepNativeInterop extends PrepNativeInterop[global.type](global) {
    val nirAddons: ScalaNativePlugin.this.nirAddons.type =
      ScalaNativePlugin.this.nirAddons
    val scalaNativeOpts = ScalaNativePlugin.this.scalaNativeOpts
    override val runsAfter = List("typer")
    override val runsBefore = List("pickler")
  }

  object nirGen extends NirGenPhase[global.type](global) {
    val nirAddons: ScalaNativePlugin.this.nirAddons.type =
      ScalaNativePlugin.this.nirAddons
    val scalaNativeOpts = ScalaNativePlugin.this.scalaNativeOpts
    override val runsAfter = List("mixin")
    override val runsBefore = List("delambdafy", "cleanup", "terminal")
  }

  override def init(options: List[String], error: String => Unit): Boolean = {
    import scalaNativeOpts._
    options.foreach {
      case "genStaticForwardersForNonTopLevelObjects" =>
        genStaticForwardersForNonTopLevelObjects = true
      case "forceStrictFinalFields" =>
        forceStrictFinalFields = true

      case opt if opt.startsWith("positionRelativizationPaths:") =>
        positionRelativizationPaths = {
          positionRelativizationPaths ++ opt
            .stripPrefix("positionRelativizationPaths:")
            .split(';')
            .map(Paths.get(_))
            .filter(_.isAbsolute())
        }.distinct.sortBy(-_.getNameCount())

      case opt if opt.startsWith("mapSourceURI:") =>
        global.reporter.warning(
          global.NoPosition,
          "'mapSourceURI' is deprecated, it is ignored"
        )

      case option =>
        error("Option not understood: " + option)
    }

    true // this plugin is always enabled
  }

  override val optionsHelp: Option[String] = Some(s"""
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

}
