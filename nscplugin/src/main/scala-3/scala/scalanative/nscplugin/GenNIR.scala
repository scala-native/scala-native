package scala.scalanative.nscplugin

import dotty.tools._
import dotc._
import plugins._
import core._
import Contexts._

import java.net.URI

class GenNIR(settings: GenNIR.Settings) extends PluginPhase {
  val phaseName = GenNIR.name

  override val runsAfter = Set(transform.MoveStatics.name)
  override val runsBefore = Set(backend.jvm.GenBCode.name)

  override def run(using Context): Unit = {
    NirCodeGen(settings).run()
  }
}

object GenNIR {
  val name = "scalanative-genNIR"
  case class Settings(
      /** Should static forwarders be emitted for non-top-level objects.
       *
       *  Scala/JVM does not do that and, we do not do it by default either, but
       *  this option can be used to opt in. This is necessary for
       *  implementations of JDK classes.
       */
      genStaticForwardersForNonTopLevelObjects: Boolean = false,
      /** Which source locations in source maps should be relativized (or where
       *  should they be mapped to)?
       */
      sourceURIMaps: List[URIMap] = Nil
  )
  case class URIMap(from: URI, to: Option[URI])

}
