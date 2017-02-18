package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._

/** Lowers modules into module classes with singleton
 *  instance stored in a global variable that is accessed
 *  through a dedicated accessor function.
 *
 *  For example a dynamic module with members:
 *
 *      module $name : $parent, .. $ifaces
 *
 *      .. $members
 *
 *  Translates to:
 *
 *      class module.$name : $parent, .. $ifaces
 *
 *      .. $members
 *
 *      var value.$name: class module.$name = zero[class module.$name]
 *
 *      def load.$name: () => class module.$name {
 *        %entry:
 *          %self = load[class module.$name] @"module.$name"
 *          %cond = ieq[class j.l.Object] %instance, zero[class module.$name]
 *          if %cond then %existing else %initialize
 *        %existing:
 *          ret %self
 *        %initialize:
 *          %alloc = alloc[class module.$name]
 *          call $name::init(%alloc)
 *          store[class $name] @"module.$name", %alloc
 *          ret %alloc
 *      }
 */
class ModuleLowering(implicit top: Top, fresh: Fresh) extends Pass {
  override def onDefns(defns: Seq[Defn]) = {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    defns.foreach {
      case Defn.Module(attrs, clsName @ ClassRef(cls), parent, ifaces) =>
        val clsDefn = Defn.Class(attrs, clsName, parent, ifaces)
        val clsTy   = Type.Class(clsName)
        val clsNull = Val.Zero(clsTy)

        val valueName = clsName member "value"
        val valueDefn = Defn.Var(Attrs.None, valueName, clsTy, clsNull)
        val value     = Val.Global(valueName, Type.Ptr)

        val entry      = fresh()
        val existing   = fresh()
        val initialize = fresh()

        val self  = Val.Local(fresh(), clsTy)
        val cond  = Val.Local(fresh(), Type.Bool)
        val alloc = Val.Local(fresh(), clsTy)

        val initCall = if (isStaticModule(clsName)) {
          Inst.None
        } else {
          val initSig = Type.Function(Seq(Type.Class(clsName)), Type.Void)
          val init    = Val.Global(clsName member "init", Type.Ptr)

          Inst.Let(Op.Call(initSig, init, Seq(alloc), Next.None))
        }

        val loadName = clsName member "load"
        val loadSig  = Type.Function(Seq(), clsTy)
        val loadDefn = Defn.Define(
          Attrs.None,
          loadName,
          loadSig,
          Seq(
            Inst.Label(entry, Seq()),
            Inst.Let(self.name, Op.Load(clsTy, value)),
            Inst.Let(cond.name, Op.Comp(Comp.Ine, Rt.Object, self, clsNull)),
            Inst.If(cond, Next(existing), Next(initialize)),
            Inst.Label(existing, Seq()),
            Inst.Ret(self),
            Inst.Label(initialize, Seq()),
            Inst.Let(alloc.name, Op.Classalloc(clsName)),
            Inst.Let(Op.Store(clsTy, value, alloc)),
            initCall,
            Inst.Ret(alloc)
          )
        )

        buf += clsDefn
        buf += valueDefn
        buf += loadDefn

      case defn =>
        buf += super.onDefn(defn)
    }

    buf
  }

  override def onInst(inst: Inst): Inst = inst match {
    case Inst.Let(n, Op.Module(name, unwind)) =>
      val loadSig = Type.Function(Seq(), Type.Class(name))
      val load    = Val.Global(name member "load", Type.Ptr)

      Inst.Let(n, Op.Call(loadSig, load, Seq(), unwind))

    case _ =>
      super.onInst(inst)
  }

  override def onType(ty: Type): Type = ty match {
    case Type.Module(n) => Type.Class(n)
    case _              => super.onType(ty)
  }

  def isStaticModule(name: Global): Boolean =
    top.nodes(name).isInstanceOf[Class] &&
      (!top.nodes.contains(name member "init"))
}

object ModuleLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ModuleLowering()(top, top.fresh)
}
