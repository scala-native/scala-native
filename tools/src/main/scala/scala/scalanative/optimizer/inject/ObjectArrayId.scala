package scala.scalanative.optimizer.inject

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.{Inject, InjectCompanion}
import scala.scalanative.tools.Config

class ObjectArrayId(top: Top) extends Inject {
  override def apply(buf: mutable.Buffer[Defn]) = {
    val objectArray =
      top.nodes(Global.Top("scala.scalanative.runtime.ObjectArray"))

    buf += Defn.Var(Attrs.None,
                    ObjectArrayId.objectArrayIdName,
                    Type.Int,
                    Val.Int(objectArray.id))
  }
}

object ObjectArrayId extends InjectCompanion {
  val objectArrayIdName = Global.Top("__object_array_id")

  override def apply(config: Config, top: Top) = new ObjectArrayId(top)
}
