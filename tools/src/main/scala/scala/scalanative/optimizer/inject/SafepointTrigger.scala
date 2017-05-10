package scala.scalanative.optimizer.inject

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.{Inject, InjectCompanion}
import scala.scalanative.tools.Config

class SafepointTrigger(top: Top) extends Inject {
  import SafepointTrigger._

  override def apply(buf: mutable.Buffer[Defn]) =
    buf += safepointTriggerDefn
}

object SafepointTrigger extends InjectCompanion {
  val safepointTriggerName = Global.Top("scalanative_safepoint_trigger")
  val safepointTriggerTy   = Type.Array(Type.Byte, 4096)
  val safepointTriggerInit = Val.Zero(safepointTriggerTy)
  val safepointTriggerDefn =
    Defn.Var(Attrs(align = Some(4096)),
             safepointTriggerName,
             safepointTriggerTy,
             safepointTriggerInit)

  override def apply(config: Config, top: Top) = new SafepointTrigger(top)
}
