package scala.scalanative.nscplugin

import dotty.tools._
import dotc._
import plugins._
import core._
import Contexts._

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
  case class Settings(genStaticForwardersForNonTopLevelObjects: Boolean = false)
}
