package native
package compiler
package pass

import native.nir._

/** Lowers modules into module classes with singleton
 *  instance stored in a global variable that is accessed
 *  through a dedicated accessor function.
 *
 *  For example a module with members:
 *
 *      module $name : $parent, ..$ifaces {
 *        ..$members
 *      }
 *
 *  Translates to:
 *
 *      class $name : $parent, ..$ifaces {
 *        ..$members
 *      }
 *
 *      var ${name}_data: class $name = null
 *
 *      def ${name}_accessor: () => class $name {
 *        %entry:
 *          %prev = load[class $name] ${name}_data
 *          %cond = ieq[object] prev, zero[object]
 *          if %cond then %thenp else %elsep
 *        %thenp:
 *          %alloc = alloc[class $name]
 *          call ${name}_init(%alloc)
 *          store[$name] ${name}_data, %alloc
 *          ret %alloc
 *        %elsep:
 *          ret %prev
 *      }
 *
 *  Eliminates:
 *  - Type.ModuleClass
 *  - Op.Module
 *  - Defn.Module
 */
class ModuleLowering(implicit fresh: Fresh) extends Pass {
  override def preDefn = {
    case Defn.Module(attrs, name, parent, ifaces, members) =>
      val cls      = Defn.Class(attrs, name, parent, ifaces, members)
      val clsty    = Type.Class(name)
      val ptrclsty = Type.Ptr(clsty)
      val ctorty   = Type.Function(Seq(clsty), Intr.unit)
      val ctor     = Val.Global(name + "init", Type.Ptr(ctorty))
      val data     = Defn.Var(Seq(), name + "data", clsty, Val.Zero(clsty))
      val dataval  = Val.Global(data.name, ptrclsty)
      val entry    = fresh()
      val thenp    = fresh()
      val elsep    = fresh()
      val prev     = Val.Local(fresh(), clsty)
      val cond     = Val.Local(fresh(), Type.Bool)
      val alloc    = Val.Local(fresh(), clsty)
      val accessor =
        Defn.Define(Seq(), name + "accessor", Type.Function(Seq(), Type.Class(name)), Seq(
          Block(entry, Seq(),
            Seq(
              Inst(prev.name, Op.Load(clsty, dataval)),
              Inst(cond.name, Op.Comp(Comp.Ieq, Intr.object_,
                                      prev, Val.Zero(Intr.object_)))
            ),
            Cf.If(cond, Next(thenp), Next(elsep))),
          Block(thenp, Seq(),
            Seq(
              Inst(alloc.name, Op.Alloc(clsty)),
              Inst(Op.Call(ctorty, ctor, Seq(alloc))),
              Inst(Op.Store(clsty, dataval, alloc))
            ),
            Cf.Ret(alloc)),
          Block(elsep, Seq(),
            Seq(),
            Cf.Ret(prev))))

      Seq(cls, data, accessor)
  }

  override def preInst = {
    case Inst(n, Op.Module(name)) =>
      val accessorTy = Type.Function(Seq(), Type.Class(name))
      val accessorVal = Val.Global(name + "accessor", Type.Ptr(accessorTy))
      Seq(Inst(n, Op.Call(accessorTy, accessorVal, Seq())))
  }

  override def preType = {
    case Type.ModuleClass(n) => Type.Class(n)
  }
}
