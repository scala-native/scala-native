package scala.scalanative.optimizer.inject

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.{Inject, InjectCompanion}
import scala.scalanative.tools.Config

/**
 * Created by lukaskellenberger on 08.03.17.
 */
class GCexternals(top: Top) extends Inject {
  override def apply(buffer: mutable.Buffer[Defn]) = {
    buffer ++= genModuleArray(buffer)
    buffer += genObjectArrayId()
  }

  def genModuleArray(defns: mutable.Buffer[Defn]): Seq[Defn] = {
    val modules = defns.filter {
      case _: Defn.Module => true
      case _              => false
    }

    val moduleArrayName     = Global.Top("__MODULES__")
    val moduleArraySizeName = Global.Top("__MODULES_SIZE__")
    val moduleArray = Val.Array(Type.Ptr, modules.map {
      case Defn.Module(_, clsName, _, _) =>
        Val.Global(clsName member "value", Type.Ptr)
    })
    val moduleArrayVar =
      Defn.Var(Attrs.None,
               moduleArrayName,
               Type.Array(Type.Ptr, modules.size),
               moduleArray)

    val moduleArraySizeVar =
      Defn.Var(Attrs.None,
               moduleArraySizeName,
               Type.Int,
               Val.Int(modules.size))

    Seq(moduleArrayVar, moduleArraySizeVar)
  }

  def genObjectArrayId(): Defn.Var = {
    val objectArray =
      top.nodes(Global.Top("scala.scalanative.runtime.ObjectArray"))
    val objectArrayIdName = Global.Top("__OBJECT_ARRAY_ID__")

    Defn.Var(Attrs.None, objectArrayIdName, Type.Int, Val.Int(objectArray.id))
  }
}

object GCexternals extends InjectCompanion {

  override def apply(config: Config, top: Top) = new GCexternals(top)
}
