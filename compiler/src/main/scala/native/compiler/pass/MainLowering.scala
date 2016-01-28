package native
package compiler
package pass

import native.nir.{ Global => G, _ }

/** Introduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
trait MainLowering extends Pass { self: Lowering =>
  override def onPostCompilationUnit(defns: Seq[Defn]) = {
    val argc = fresh()
    val argcTy = Type.I32
    val argv = fresh()
    val argvTy = Type.Ptr(Type.Ptr(Type.I8))
    val moduleAccessorName =
      G.Tagged(entryModule, G.Atom("accessor"))
    val moduleMainName =
      G.Nested(entryModule, G.Tagged(G.Tagged(G.Atom("main"),
        G.Tagged(G.Atom("string"), G.Atom("arr"))), G.Atom("unit")))
    val moduleMainTy = Type.Function(Seq(Type.Ptr(Type.I8), Type.Ptr(Type.I8)), Type.Void)
    val moduleMain = Val.Global(moduleMainName, Type.Ptr(moduleMainTy))
    val moduleAccessorTy = Type.Function(Seq(), Type.Ptr(Type.I8))
    val moduleAccessor = Val.Global(moduleAccessorName, Type.Ptr(moduleAccessorTy))
    val m = fresh()
    val body =
      Block(fresh(), Seq(Param(argc, argcTy), Param(argv, argvTy)),
        Seq(Instr(m, Op.Call(moduleAccessorTy, moduleAccessor, Seq())),
            Instr(Op.Call(moduleMainTy, moduleMain, Seq(Val.Local(m, Type.Ptr(Type.I8)),
                                                        Val.Zero(Type.Ptr(Type.I8))))),
            Instr(Op.Ret(Val.I32(0)))))
    val sig =
      Type.Function(Seq(argcTy, argvTy), Type.I32)
    val main =
      Defn.Define(Seq(), G.Atom("main"), sig, Seq(body))

    super.onPostCompilationUnit(defns :+ main)
  }
}
