package native
package compiler
package pass

import native.nir._

/** Introduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
class MainInjection(entry: Global)(implicit fresh: Fresh) extends Pass {
  override def preAssembly = { case defns =>
    val mainTy = Type.Function(Seq(Type.Module(entry), Intr.object_array), Type.Void)
    val main   = Val.Global(entry ++ Seq("main", "string", "arr", "unit"), Type.Ptr(mainTy))
    val argc   = Val.Local(fresh(), Type.I32)
    val argv   = Val.Local(fresh(), Type.Ptr(Type.Ptr(Type.I8)))
    val module = Val.Local(fresh(), Type.Module(entry))
    val arr    = Val.Local(fresh(), Intr.object_array)
    val sig    = Type.Function(Seq(argc.ty, argv.ty), Type.I32)

    defns :+
      Defn.Define(Seq(), Global("main"), sig, Seq(
        Block(fresh(), Seq(argc, argv),
          Seq(Inst(arr.name, Intr.call(Intr.init, argc, argv)),
              Inst(module.name, Op.Module(entry)),
              Inst(Op.Call(mainTy, main, Seq(module, arr)))),
          Cf.Ret(Val.I32(0)))))
  }
}
