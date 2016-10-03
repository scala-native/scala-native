package scala.scalanative
package compiler
package pass

import nir._

/** Introduces `main` function that sets up
 *  the runtime and calls the given entry point.
 */
class MainInjection(entry: Global)(implicit fresh: Fresh) extends Pass {
  import MainInjection._

  override def preAssembly = {
    case defns =>
      val mainTy =
        Type.Function(Seq(Type.Module(entry.top), ObjectArray), Type.Void)
      val main   = Val.Global(entry, Type.Ptr)
      val argc   = Val.Local(fresh(), Type.I32)
      val argv   = Val.Local(fresh(), Type.Ptr)
      val module = Val.Local(fresh(), Type.Module(entry.top))
      val rt     = Val.Local(fresh(), Rt)
      val arr    = Val.Local(fresh(), ObjectArray)

      defns :+ Defn.Define(
        Attrs.None,
        mainName,
        mainSig,
        Seq(
          Block(fresh(),
                Seq(argc, argv),
                Seq(Inst(rt.name, Op.Module(Rt.name)),
                    Inst(arr.name,
                         Op.Call(initSig, init, Seq(rt, argc, argv))),
                    Inst(module.name, Op.Module(entry.top)),
                    Inst(Op.Call(mainTy, main, Seq(module, arr)))),
                Cf.Ret(Val.I32(0)))))
  }
}

object MainInjection extends PassCompanion {
  def apply(ctx: Ctx) = new MainInjection(ctx.entry)(ctx.fresh)

  val ObjectArray =
    Type.Class(Global.Top("scala.scalanative.runtime.ObjectArray"))

  val Rt       = Type.Module(Global.Top("scala.scalanative.runtime.package$"))
  val initName = Rt.name member "init_i32_ptr_class.ssnr.ObjectArray"
  val initSig  = Type.Function(Seq(Rt, Type.I32, Type.Ptr), ObjectArray)
  val init     = Val.Global(initName, initSig)

  val mainName = Global.Top("main")
  val mainSig  = Type.Function(Seq(Type.I32, Type.Ptr), Type.I32)

  override val depends = Seq(ObjectArray.name, Rt.name, init.name)
}
