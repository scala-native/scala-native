package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Lowers modules into module classes with singleton
  * instance stored in a global variable that is accessed
  * through a dedicated accessor function.
  *
  * For example a dynamic module with members:
  *
  *     module $name : $parent, .. $ifaces {
  *       ..$members
  *     }
  *
  * Translates to:
  *
  *     class $name : $parent, .. $ifaces {
  *       .. $members
  *     }
  *
  *     var @$name.value: class $name = zero[class $name]
  *
  *     def $name.load: () => void {
  *       %entry:
  *         %self = load[class $name] @"module.$name"
  *         %cond = ieq[class j.l.Object] %instance, zero[class $name]
  *         if %cond then %existing else %initialize
  *       %existing:
  *         ret %self
  *       %initialize:
  *         %alloc = alloc[class $name]
  *         call $name::init(%alloc)
  *         store[class $name] @"module.$name", %alloc
  *         ret %alloc
  *     }
  *
  * Eliminates:
  * - Type.Module
  * - Op.Module
  * - Defn.Module
  */
class ModuleLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  override def preDefn = {
    case Defn.Module(attrs, name @ ClassRef(cls), parent, ifaces) =>
      val clsDefn = Defn.Class(attrs, name, parent, ifaces)

      val zero      = Val.Zero(cls.ty)
      val valueName = name + "value"
      val valueDefn = Defn.Var(Seq(), valueName, Type.ClassValue(name), zero)
      val value     = Val.Global(valueName, Type.Ptr)

      val entry      = fresh()
      val existing   = fresh()
      val initialize = fresh()

      val self  = Val.Local(fresh(), cls.ty)
      val cond  = Val.Local(fresh(), Type.Bool)
      val alloc = Val.Local(fresh(), cls.ty)

      val initCall =
        if (isStaticModule(name)) Seq()
        else {
          val initSig = Type.Function(Seq(Type.Class(name)), Type.Unit)
          val init    = Val.Global(name + "init", Type.Ptr)

          Seq(Inst(Op.Call(initSig, init, Seq(alloc))))
        }

      val loadName = name + "load"
      val loadSig  = Type.Function(Seq(), Type.Void)
      val loadDefn = Defn.Define(
        Seq(),
        loadName,
        loadSig,
        Seq(Block(entry,
                  Seq(),
                  Seq(
                    Inst(self.name, Op.Load(self.ty, value)),
                    Inst(cond.name, Op.Comp(Comp.Ieq, Rt.Object, self, zero))
                  ),
                  Cf.If(cond, Next(existing), Next(initialize))),
            Block(existing,
                  Seq(),
                  Seq(),
                  Cf.Ret(self)),
            Block(initialize,
                  Seq(),
                  Seq(
                    Seq(Inst(alloc.name, Op.Alloc(cls.ty))),
                    initCall,
                    Seq(Inst(Op.Store(cls.ty, value, alloc)))
                  ).flatten,
                  Cf.Ret(alloc))))

      Seq(clsDefn, valueDefn, loadDefn)
  }

  override def preInst = {
    case Inst(n, Op.Module(name)) =>
      val ensureInit =
        if (isStaticModule(name)) None
        else {
          val ensureInitTy = Type.Function(Seq(), Type.Void)
          val ensureInitRef =
            Val.Global(name + "ensure" + "init", Type.Ptr)

          Some(Inst(Op.Call(ensureInitTy, ensureInitRef, Seq())))
        }

      val instanceRef =
        Val.Global(name + "instance", Type.Ptr)

      ensureInit ++: Seq(
        Inst(n, Op.Conv(Conv.Bitcast, Type.Class(name), instanceRef))
      )
  }

  override def preType = {
    case Type.Module(n) => Type.Class(n)
  }

  def isStaticModule(name: Global): Boolean =
    chg.nodes(name).isInstanceOf[ClassHierarchy.Class] &&
    (!chg.nodes.contains(name + "init"))
}
