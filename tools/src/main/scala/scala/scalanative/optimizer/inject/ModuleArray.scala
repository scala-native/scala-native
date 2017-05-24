package scala.scalanative.optimizer.inject

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.{Inject, InjectCompanion}
import scala.scalanative.tools.Config

class ModuleArray(top: Top) extends Inject {
  override def apply(buf: mutable.Buffer[Defn]) = {
    buf +=
      Defn.Var(Attrs.None,
               ModuleArray.moduleArrayName,
               top.moduleArray.value.ty,
               top.moduleArray.value)

    buf +=
      Defn.Var(Attrs.None,
               ModuleArray.moduleArraySizeName,
               Type.Int,
               Val.Int(top.moduleArray.size))
  }
}

object ModuleArray extends InjectCompanion {
  val moduleArrayName     = Global.Top("__modules")
  val moduleArraySizeName = Global.Top("__modules_size")

  override def apply(config: Config, top: Top) = new ModuleArray(top)
}
