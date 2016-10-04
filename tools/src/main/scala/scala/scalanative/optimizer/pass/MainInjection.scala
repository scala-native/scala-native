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
      val entryMainTy = Type.Function(
        Seq(Arg(Type.Module(entry.top)), Arg(ObjectArray)),
        Type.Void)
      val entryMainName =
        Global.Member(entry, "main_class.ssnr.ObjectArray_unit")
      val entryMain = Val.Global(entryMainName, Type.Ptr)

      val argc   = Val.Local(fresh(), Type.I32)
      val argv   = Val.Local(fresh(), Type.Ptr)
      val module = Val.Local(fresh(), Type.Module(entry.top))
      val rt     = Val.Local(fresh(), Rt)
      val arr    = Val.Local(fresh(), ObjectArray)

      val methodCallsDumpName = "method-calls.txt"

      defns :+ Defn.Define(
        Attrs.None,
        MainName,
        MainSig,
        Seq(
          Inst.Label(fresh(), Seq(argc, argv)),
          Inst.Let(Op.Call(InitSig, Init, Seq())),
          Inst.Let(rt.name, Op.Module(Rt.name)),
          Inst.Let(arr.name, Op.Call(RtInitSig, RtInit, Seq(rt, argc, argv))),
          Inst.Let(module.name, Op.Module(entry.top)),
          Inst.Let(Op.Call(entryMainTy, entryMain, Seq(module, arr))),
          Inst.Let(Op.Call(DumpLogSig, DumpLog, Seq(Val.String(methodCallsDumpName)))),
          Inst.Ret(Val.I32(0))))
  }
}

object MainInjection extends PassCompanion {
  val ObjectArray =
    Type.Class(Global.Top("scala.scalanative.runtime.ObjectArray"))

  val Rt =
    Type.Module(Global.Top("scala.scalanative.runtime.package$"))
  val RtInitSig =
    Type.Function(Seq(Arg(Rt), Arg(Type.I32), Arg(Type.Ptr)), ObjectArray)
  val RtInit =
    Val.Global(Rt.name member "init_i32_ptr_class.ssnr.ObjectArray", Type.Ptr)

  val MainName = Global.Top("main")
  val MainSig  = Type.Function(Seq(Arg(Type.I32), Arg(Type.Ptr)), Type.I32)

  val InitSig  = Type.Function(Seq(), Type.Unit)
  val Init     = Val.Global(Global.Top("scalanative_init"), Type.Ptr)
  val InitDecl = Defn.Declare(Attrs.None, Init.name, InitSig)

  val DumpLogSig  = Type.Function(Seq(Arg(nir.Rt.String)), Type.Void)
  val DumpLog     = Val.Global(Global.Top("method_call_dump_file"), Type.Ptr)
  val DumpLogDecl = Defn.Declare(Attrs.None, DumpLog.name, DumpLogSig)

  override val depends =
    Seq(ObjectArray.name, Rt.name, RtInit.name)

  override val injects =
    Seq(InitDecl, DumpLogDecl)

  override def apply(config: tools.Config, top: Top) =
    if (config.injectMain) new MainInjection(config.entry)(top.fresh)
    else EmptyPass
}
