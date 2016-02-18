package native
package compiler
package pass

import native.nir._

/** Introduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
class MainInjection(entryModule: Global)(implicit fresh: Fresh) extends Pass {
  override def preAssembly = { case defns =>
    val moduleMainName = entryModule ++ Seq("main", "string", "arr", "unit")
    val moduleMainTy = Type.Function(Seq(Type.Ptr(Type.I8), Type.Ptr(Type.I8)), Type.Void)
    val moduleMain = Val.Global(moduleMainName, Type.Ptr(moduleMainTy))
    val argc = Val.Local(fresh(), Type.I32)
    val argv = Val.Local(fresh(), Type.Ptr(Type.Ptr(Type.I8)))
    val m = Val.Local(fresh(), Type.Ptr(Type.I8))
    val arr = Val.Local(fresh(), Type.ArrayClass(Intr.string))
    val body =
      Block(fresh(), Seq(argc, argv),
        Seq(Inst(arr.name, Intr.call(Intr.init, argc, argv)),
            Inst(m.name,   Op.Module(entryModule)),
            Inst(Op.Call(moduleMainTy, moduleMain, Seq(m, arr))),
            Inst(Op.Ret(Val.I32(0)))))
    val sig =
      Type.Function(Seq(argc.ty, argv.ty), Type.I32)
    val main =
      Defn.Define(Seq(), Global("main"), sig, Seq(body))

    defns :+ main
  }
}
