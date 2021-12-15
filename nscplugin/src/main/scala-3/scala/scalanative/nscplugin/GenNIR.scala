package scala.scalanative.nscplugin

import dotty.tools._
import dotc._
import plugins._
import core._
import Contexts._

object GenNIR extends PluginPhase {
  val phaseName = "scalanative-genNIR"

  override val runsAfter = Set(transform.MoveStatics.name)
  override val runsBefore = Set(backend.jvm.GenBCode.name)

  override def run(using Context): Unit = {
    NirCodeGen().run()
  }
}
