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
 *      module $name($parent, ..$ifaces) =
 *        ..$members
 *
 *  Translates to:
 *
 *      class $name($parent, ..$ifaces) =
 *        ..$members
 *
 *      var $name!data: class $name = null
 *
 *      define $name!accessor: () => class $name =
 *        block %entry():
 *          %prev = load[class $name] $name!data
 *          %cond = eq[object] prev, null
 *          if %cond then %thenp() else %elsep()
 *        block %thenp():
 *          %alloc = obj-alloc[class $name]
 *          call $name::<>(%new)
 *          store[$name] $name!data, %alloc
 *          ret %alloc
 *        block %elsep()
 *          ret %prev
 *
 *  Eliminates:
 *  - Type.ModuleClass
 *  - Defn.Module
 */
trait ModuleLowering extends Pass {
  private val accessortag = Global.Atom("accessor")
  private val datatag = Global.Atom("data")

  override def onDefn(defn: Defn) = defn match {
    case Defn.Module(attrs, name, parent, ifaces, members) =>
      val cls = Defn.Class(attrs, name, parent, ifaces, members)
      val clsty = Type.Class(name)
      val ptrclsty = Type.Ptr(clsty)
      val ctorty = Type.Function(Seq(), Type.Unit)
      val ctor = Val.Global(Global.Nested(name, Global.Atom("init")), Type.Ptr(ctorty))
      val data = Defn.Var(Seq(), Global.Tagged(name, datatag), clsty, Val.Null)
      val dataval = Val.Global(data.name, ptrclsty)
      val accessor =
        Defn.Define(
          Seq(),
          Global.Tagged(name, accessortag),
          Type.Function(Seq(), Type.Class(name)),
          {
            val entry = Focus.entry(fresh)
            val prev = entry withOp Op.Load(clsty, dataval)
            val cond = prev withOp Op.Comp(Comp.Eq, Type.ObjectClass, prev.value, Val.Null)

            cond.branchIf(cond.value, Type.Nothing,
              { thenp =>
                val alloc = thenp withOp Op.ObjAlloc(clsty)
                val call = alloc withOp Op.Call(ctorty, ctor, Seq(alloc.value))
                val store = call withOp Op.Store(clsty, dataval, alloc.value)
                store finish Op.Ret(alloc.value)
              },
              { elsep =>
                elsep finish Op.Ret(prev.value)
              }
            ).finish(Op.Unreachable).blocks
          }
        )
      Seq(cls, data, accessor).flatMap(onDefn)
    case _ =>
      super.onDefn(defn)
  }

  override def onType(ty: Type) = super.onType(ty match {
    case Type.ModuleClass(n) => Type.Class(n)
    case _                   => ty
  })
}
