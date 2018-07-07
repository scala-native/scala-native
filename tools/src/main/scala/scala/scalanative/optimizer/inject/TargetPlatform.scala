package scala.scalanative.optimizer.inject

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.{Inject, InjectCompanion}
import scala.scalanative.build.Config

import scala.scalanative.optimizer.pass.StringLowering

class TargetArchitecture(top: Top, config: Config) extends Inject {
  override def apply(buf: mutable.Buffer[Defn]) = {
    buf +=
      Defn.Var(Attrs.None,
               TargetArchitecture.targetArchitecture,
               Type.Int,
               Val.Int(config.targetArchitecture.id))
  }
}

object TargetArchitecture extends InjectCompanion {
  val targetArchitecture = Global.Top("__targetArchitecture")

  override def apply(config: Config, top: Top) =
    new TargetArchitecture(top, config)
}
