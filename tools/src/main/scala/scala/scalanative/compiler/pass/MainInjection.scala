package scala.scalanative
package compiler
package pass

import nir._

/** Introduces `main` function that sets up
 *  the runtime and calls the given entry point.
 */
class MainInjection(entry: Global)(implicit fresh: Fresh) extends Pass {
  override def preAssembly = {
    case defns =>
      val mainTy = Type.Function(Seq(Type.Module(entry.top), Rt.ObjectArray),
                                 Type.Void)
      val main   = Val.Global(entry, Type.Ptr)
      val argc   = Val.Local(fresh(), Type.I32)
      val argv   = Val.Local(fresh(), Type.Ptr)
      val module = Val.Local(fresh(), Type.Module(entry.top))
      val rt     = Val.Local(fresh(), Rt.Rt)
      val arr    = Val.Local(fresh(), Rt.ObjectArray)

      Rt.defns ++ defns :+ Defn.Define(
          Attrs.None,
          Rt.mainName,
          Rt.mainSig,
          Seq(
              Block(
                  fresh(),
                  Seq(argc, argv),
                  Seq(Inst(rt.name, Op.Module(Rt.Rt.name)),
                      Inst(arr.name,
                           Op.Call(Rt.initSig, Rt.init, Seq(rt, argc, argv))),
                      Inst(module.name, Op.Module(entry.top)),
                      Inst(Op.Call(mainTy, main, Seq(module, arr)))),
                  Cf.Ret(Val.I32(0)))))
  }
}
