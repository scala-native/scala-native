package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import java.net.URI
import java.net.URISyntaxException

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
    import ScalaNativeOptions.URIMap

    var genStaticForwardersForNonTopLevelObjects: Boolean = false
    lazy val sourceURIMaps: List[URIMap] = _sourceURIMaps.reverse
    var _sourceURIMaps: List[URIMap] = Nil
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
    import ScalaNativeOptions.URIMap
    options.foreach {
      case "genStaticForwardersForNonTopLevelObjects" =>
        genStaticForwardersForNonTopLevelObjects = true
      case opt if opt.startsWith("mapSourceURI:") =>
        val uris = opt.stripPrefix("mapSourceURI:").split("->")
        if (uris.length != 1 && uris.length != 2) {
          error("mapSourceURI needs one or two URIs as argument.")
        } else {
          try {
            val from = new URI(uris.head)
            val to = uris.lift(1).map(str => new URI(str))
            _sourceURIMaps ::= URIMap(from, to)
          } catch {
            case e: URISyntaxException =>
              error(s"${e.getInput} is not a valid URI")
          }
        }

      case option =>
        error("Option not understood: " + option)
    }

    true // this plugin is always enabled
  }

  override val optionsHelp: Option[String] = Some(s"""
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

}
