package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
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
  override def preDefn = {
    case Defn.Module(attrs, name @ ClassRef(cls), parent, ifaces) =>
      val clsName = name tag "module"
      val clsDefn = Defn.Class(attrs, name tag "module", parent, ifaces)
      val clsTy   = Type.Class(clsName)
      val clsNull = Val.Zero(clsTy)

      val valueName = name tag "value"
      val valueDefn = Defn.Var(Attrs.None, valueName, clsTy, clsNull)
      val value     = Val.Global(valueName, Type.Ptr)

      val entry      = fresh()
      val existing   = fresh()
      val initialize = fresh()

      val self  = Val.Local(fresh(), clsTy)
      val cond  = Val.Local(fresh(), Type.Bool)
      val alloc = Val.Local(fresh(), clsTy)

      val initCall =
        if (isStaticModule(name)) Seq()
        else {
          val initSig = Type.Function(Seq(Arg(Type.Class(name))), Type.Void)
          val init    = Val.Global(name member "init", Type.Ptr)

          Seq(Inst(Op.Call(initSig, init, Seq(alloc))))
        }

      val loadName = name tag "load"
      val loadSig  = Type.Function(Seq(), clsTy)
      val loadDefn = Defn.Define(
          Attrs.None,
          loadName,
          loadSig,
          Seq(Block(entry,
                    Seq(),
                    Seq(
                        Inst(self.name, Op.Load(clsTy, value)),
                        Inst(cond.name,
                             Op.Comp(Comp.Ine, Rt.Object, self, clsNull))
                    ),
                    Cf.If(cond, Next(existing), Next(initialize))),
              Block(existing, Seq(), Seq(), Cf.Ret(self)),
              Block(initialize,
                    Seq(),
                    Seq(
                        Seq(Inst(alloc.name, Op.Classalloc(clsName))),
                        Seq(Inst(Op.Store(clsTy, value, alloc))),
                        initCall
                    ).flatten,
                    Cf.Ret(alloc))))

      Seq(clsDefn, valueDefn, loadDefn)
  }

  override def preInst = {
    case Inst(n, Op.Module(name)) =>
      val loadSig = Type.Function(Seq(), Type.Class(name tag "module"))
      val load    = Val.Global(name tag "load", Type.Ptr)

      Seq(
          Inst(n, Op.Call(loadSig, load, Seq()))
      )
  }

  override def preType = {
    case Type.Module(n) => Type.Class(n)
  }

  def isStaticModule(name: Global): Boolean =
    top.nodes(name).isInstanceOf[Class] &&
      (!top.nodes.contains(name member "init"))
}

object ModuleLowering extends PassCompanion {
  def apply(ctx: Ctx) = new ModuleLowering()(ctx.top, ctx.fresh)
}
