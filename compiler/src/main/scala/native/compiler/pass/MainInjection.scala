package native
package compiler
package pass

import native.nir._

/** Introduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
trait MainInjection extends Pass { self: EarlyLowering =>
  override def onPostCompilationUnit(defns: Seq[Defn]) = {
    val argc = Val.Local(fresh(), Type.I32)
    val argv = Val.Local(fresh(), Type.Ptr(Type.Ptr(Type.I8)))
    val moduleMainName = entryModule ++ Seq("main", "string", "arr", "unit")
    val moduleMainTy = Type.Function(Seq(Type.Ptr(Type.I8), Type.Ptr(Type.I8)), Type.Void)
    val moduleMain = Val.Global(moduleMainName, Type.Ptr(moduleMainTy))
    val m = fresh()
    val mVal = Val.Local(m, Type.Ptr(Type.I8))
    val arr = fresh()
    val arrVal = Val.Local(arr, Type.ArrayClass(Intr.string))
    val body =
      Block(fresh(), Seq(argc, argv),
        Seq(Inst(arr, Intr.call(Intr.init, argc, argv)),
            Inst(m, Op.Module(entryModule)),
            Inst(Op.Call(moduleMainTy, moduleMain, Seq(mVal, arrVal))),
            Inst(Op.Ret(Val.I32(0)))))
    val sig =
      Type.Function(Seq(argc.ty, argv.ty), Type.I32)
    val main =
      Defn.Define(Seq(), Global("main"), sig, Seq(body))

    super.onPostCompilationUnit(defns ++ onDefn(main))
  }
}
