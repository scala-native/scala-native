package native
package compiler
package pass

import native.nir._

/** Introduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
trait MainLowering extends Pass { self: Lowering =>
  override def onPostCompilationUnit(defns: Seq[Defn]) = {
    val argc = fresh()
    val argcTy = Type.I32
    val argcVal = Val.Local(argc, argcTy)
    val argv = fresh()
    val argvTy = Type.Ptr(Type.Ptr(Type.I8))
    val argvVal = Val.Local(argv, argvTy)
    val moduleMainName = entryModule ++ Seq("main", "string", "arr", "unit")
    val moduleMainTy = Type.Function(Seq(Type.Ptr(Type.I8), Type.Ptr(Type.I8)), Type.Void)
    val moduleMain = Val.Global(moduleMainName, Type.Ptr(moduleMainTy))
    val m = fresh()
    val mVal = Val.Local(m, Type.Ptr(Type.I8))
    val arr = fresh()
    val arrVal = Val.Local(arr, Type.ArrayClass(Intrinsic.string))
    val body =
      Block(fresh(), Seq(Param(argc, argcTy), Param(argv, argvTy)),
        Seq(Instr(arr, Intrinsic.call(Intrinsic.init, argcVal, argvVal)),
            Instr(m, Op.Module(entryModule)),
            Instr(Op.Call(moduleMainTy, moduleMain, Seq(mVal, arrVal))),
            Instr(Op.Ret(Val.I32(0)))))
    val sig =
      Type.Function(Seq(argcTy, argvTy), Type.I32)
    val main =
      Defn.Define(Seq(), Global("main"), sig, Seq(body))

    super.onPostCompilationUnit(defns ++ onDefn(main))
  }
}
