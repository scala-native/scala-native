package scala.scalanative
package compiler
package pass

import nir._

/** Nrtoduces `main` function that set ups
 *  the runtime and calls the given entry point.
 */
class MainInjection(entry: Global)(implicit fresh: Fresh) extends Pass {
  override def preAssembly = { case defns =>
    val mainTy = Type.Function(Seq(Type.Module(entry.owner), Rt.RefArray),
                               Type.Unit)
    val main   = Val.Global(entry, Type.Ptr(mainTy))
    val argc   = Val.Local(fresh(), Type.I32)
    val argv   = Val.Local(fresh(), Type.Ptr(Type.Ptr(Type.I8)))
    val module = Val.Local(fresh(), Type.Module(entry.owner))
    val rt     = Val.Local(fresh(), Rt.Rt)
    val arr    = Val.Local(fresh(), Rt.RefArray)

    Rt.decls ++ defns :+
      Defn.Define(Seq(), Rt.mainName, Rt.mainSig, Seq(
        Block(fresh(), Seq(argc, argv),
          Seq(Inst(rt.name, Op.Module(Rt.Rt.name)),
              Inst(arr.name, Op.Call(Rt.initSig, Rt.init, Seq(rt, argc, argv))),
              Inst(module.name, Op.Module(entry.owner)),
              Inst(Op.Call(mainTy, main, Seq(module, arr)))),
          Cf.Ret(Val.I32(0)))))
  }
}
