package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import nir._

/** Introduces `main` function that sets up
 *  the runtime and calls the given entry point.
 */
class MainInjection(entry: Global)(implicit fresh: Fresh) extends Pass {
  import MainInjection._

  override def preAssembly = {
    case defns =>
      val entryMainTy =
        Type.Function(Seq(Type.Module(entry.top), ObjectArray), Type.Void)
      val entryMainName =
        Global.Member(entry, "main_class.ssnr.ObjectArray_unit")
      val entryMain = Val.Global(entryMainName, Type.Ptr)

      val start, succ, fail = fresh()

      val argc   = Val.Local(fresh(), Type.I32)
      val argv   = Val.Local(fresh(), Type.Ptr)
      val module = Val.Local(fresh(), Type.Module(entry.top))
      val rt     = Val.Local(fresh(), Rt)
      val arr    = Val.Local(fresh(), ObjectArray)
      val exc    = Val.Local(fresh(), nir.Rt.Object)

      defns :+ Defn.Define(
        Attrs.None,
        MainName,
        MainSig,
        Seq(
          Inst.Label(start, Seq(argc, argv)),
          Inst.Try(Next.Succ(succ), Next.Fail(fail)),
          Inst.Label(succ, Seq()),
          Inst.Let(Op.Call(InitSig, Init, Seq())),
          Inst.Let(rt.name, Op.Module(Rt.name)),
          Inst.Let(arr.name, Op.Call(RtInitSig, RtInit, Seq(rt, argc, argv))),
          Inst.Let(module.name, Op.Module(entry.top)),
          Inst.Let(Op.Call(entryMainTy, entryMain, Seq(module, arr))),
          Inst.Ret(Val.I32(0)),
          Inst.Label(fail, Seq(exc)),
          Inst.Let(Op.Call(PrintStackTraceSig, PrintStackTrace, Seq(exc))),
          Inst.Ret(Val.I32(1))
        )
      )
  }
}

object MainInjection extends PassCompanion {
  val ObjectArray =
    Type.Class(Global.Top("scala.scalanative.runtime.ObjectArray"))

  val Rt =
    Type.Module(Global.Top("scala.scalanative.runtime.package$"))
  val RtInitSig =
    Type.Function(Seq(Rt, Type.I32, Type.Ptr), ObjectArray)
  val RtInit =
    Val.Global(Rt.name member "init_i32_ptr_class.ssnr.ObjectArray", Type.Ptr)

  val MainName = Global.Top("main")
  val MainSig  = Type.Function(Seq(Type.I32, Type.Ptr), Type.I32)

  val ThrowableName = Global.Top("java.lang.Throwable")
  val Throwable     = Type.Class(ThrowableName)

  val PrintStackTraceSig =
    Type.Function(Seq(Throwable), Type.Unit)
  val PrintStackTraceName =
    Global.Member(ThrowableName, "printStackTrace_unit")
  val PrintStackTrace =
    Val.Global(PrintStackTraceName, Type.Ptr)

  val InitSig  = Type.Function(Seq(), Type.Unit)
  val Init     = Val.Global(Global.Top("scalanative_init"), Type.Ptr)
  val InitDecl = Defn.Declare(Attrs.None, Init.name, InitSig)

  override val depends =
    Seq(ObjectArray.name, Rt.name, RtInit.name, PrintStackTraceName)

  override val injects =
    Seq(InitDecl)

  override def apply(config: tools.Config, top: Top) =
    if (config.injectMain) new MainInjection(config.entry)(top.fresh)
    else EmptyPass
}
