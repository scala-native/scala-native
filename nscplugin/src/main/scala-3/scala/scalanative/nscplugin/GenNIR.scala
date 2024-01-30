package scala.scalanative.nscplugin

import dotty.tools._
import dotc._
import plugins._
import core._
import Contexts._

import java.net.URI
import java.nio.file.Path

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

      /** Treat all final fields like if they would be marked with safePublish
       */
      forceStrictFinalFields: Boolean = false,

      /** List of paths usd for relativization of source file positions
       */
      positionRelativizationPaths: Seq[Path] = Nil
  )
}
