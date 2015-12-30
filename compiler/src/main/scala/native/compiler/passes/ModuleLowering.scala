package native
package compiler
package passes

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
 *          %alloc = alloc[class $name]
 *          call $name::<>(%new)
 *          store[$name] $name!data, %alloc
 *          ret %alloc
 *        block %elsep()
 *          ret %prev
 *
 */
object ModuleLowering extends Pass {
  import Name.Local._

  override def onDefn(defn: Defn) = defn match {
    case Defn.Module(attrs, name, parent, ifaces, members) =>
      val cls = Defn.Class(attrs, name, parent, ifaces, members)
      val clsty = Type.Class(name)
      val ptrclsty = Type.Ptr(clsty)
      val ctorty = Type.Function(Seq(), Type.Unit)
      val ctor = Val.Name(Name.Nested(name, Name.Constructor(Seq())), Type.Ptr(ctorty))
      val data = Defn.Var(Seq(), Name.Tagged(name, "data"), clsty, Val.Null)
      val dataval = Val.Name(data.name, ptrclsty)
      val accessor =
        Defn.Define(
          Seq(),
          Name.Tagged(name, "accessor"),
          Type.Function(Seq(), Type.Class(name)),
          {
            val entry = Focus.entry(new Fresh)
            val prev = entry withOp Op.Load(clsty, dataval)
            val cond = prev withOp Op.Comp(Comp.Eq, Type.ObjectClass, prev.value, Val.Null)

            cond.branchIf(cond.value, Type.Nothing,
              { thenp =>
                val alloc = thenp withOp Op.Alloc(clsty)
                val call = alloc withOp Op.Call(ctorty, ctor, Seq(alloc.value))
                val store = call withOp Op.Store(clsty, dataval, alloc.value)
                store finish Op.Ret(alloc.value)
              },
              { elsep =>
                elsep finish Op.Ret(prev.value)
              }
            ).finish(Op.Undefined).blocks
          }
        )
      Seq(cls, data, accessor)
    case _ =>
      super.onDefn(defn)
  }
}
