package scala.scalanative.optimizer.inject

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.{Inject, InjectCompanion}
import scala.scalanative.tools.Config

class GCExternals(top: Top) extends Inject {
  override def apply(buffer: mutable.Buffer[Defn]) = {
    buffer ++= genModuleArray(buffer)
    buffer += genObjectArrayId()
  }

  def genModuleArray(defns: mutable.Buffer[Defn]): Seq[Defn] = {
    val modules = defns.filter(_.isInstanceOf[Defn.Module])

    val moduleArray = Val.Array(Type.Ptr, modules.map {
      case Defn.Module(_, clsName, _, _) =>
        Val.Global(clsName member "value", Type.Ptr)
    })
    val moduleArrayVar =
      Defn.Var(Attrs.None,
               GCExternals.moduleArrayName,
               Type.Array(Type.Ptr, modules.size),
               moduleArray)

    val moduleArraySizeVar =
      Defn.Var(Attrs.None,
               GCExternals.moduleArraySizeName,
               Type.Int,
               Val.Int(modules.size))

    Seq(moduleArrayVar, moduleArraySizeVar)
  }

  def genObjectArrayId(): Defn.Var = {
    val objectArray =
      top.nodes(Global.Top("scala.scalanative.runtime.ObjectArray"))

    Defn.Var(Attrs.None,
             GCExternals.objectArrayIdName,
             Type.Int,
             Val.Int(objectArray.id))
  }
}

object GCExternals extends InjectCompanion {
  val moduleArrayName     = Global.Top("__modules")
  val moduleArraySizeName = Global.Top("__modules_size")

  val objectArrayIdName = Global.Top("__object_array_id")

  override def apply(config: Config, top: Top) = new GCExternals(top)
}
