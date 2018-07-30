package scala.scalanative
package lower

import scala.collection.mutable
import scala.scalanative.nir._

object Generate {
  import Impl._

  def apply(entry: Global)(implicit top: sema.Top, meta: Metadata): Seq[Defn] =
    (new Impl(entry)).generate()

  private class Impl(entry: Global)(implicit top: sema.Top, meta: Metadata) {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    val tables = new TraitDispatchTables(top)

    def generate(): Seq[Defn] = {
      genClassHasTrait()
      genTraitHasTrait()
      genMain()
      genStackBottom()
      genModuleArray()
      genModuleArraySize()
      genObjectArrayId()
      genTraitDispatchTables()
      buf
    }

    def genClassHasTrait(): Unit = {
      implicit val fresh   = Fresh()
      val classid, traitid = Val.Local(fresh(), Type.Int)
      val boolptr          = Val.Local(fresh(), Type.Ptr)
      val result           = Val.Local(fresh(), Type.Bool)

      buf += Defn.Define(
        Attrs(isExtern = true, inline = Attr.AlwaysInline),
        ClassHasTraitName,
        ClassHasTraitSig,
        Seq(
          Inst.Label(fresh(), Seq(classid, traitid)),
          Inst.Let(boolptr.name,
                   Op.Elem(tables.classHasTraitTy,
                           tables.classHasTraitVal,
                           Seq(Val.Int(0), classid, traitid))),
          Inst.Let(result.name, Op.Load(Type.Bool, boolptr)),
          Inst.Ret(result)
        )
      )
    }

    def genTraitHasTrait(): Unit = {
      implicit val fresh  = Fresh()
      val leftid, rightid = Val.Local(fresh(), Type.Int)
      val boolptr         = Val.Local(fresh(), Type.Ptr)
      val result          = Val.Local(fresh(), Type.Bool)

      buf += Defn.Define(
        Attrs(isExtern = true, inline = Attr.AlwaysInline),
        TraitHasTraitName,
        TraitHasTraitSig,
        Seq(
          Inst.Label(fresh(), Seq(leftid, rightid)),
          Inst.Let(boolptr.name,
                   Op.Elem(tables.traitHasTraitTy,
                           tables.traitHasTraitVal,
                           Seq(Val.Int(0), leftid, rightid))),
          Inst.Let(result.name, Op.Load(Type.Bool, boolptr)),
          Inst.Ret(result)
        )
      )
    }

    def genMain(): Unit = {
      implicit val fresh = Fresh()
      val entryMainTy =
        Type.Function(Seq(Type.Module(entry.top), ObjectArray), Type.Void)
      val entryMainName =
        Global.Member(entry, "main_scala.scalanative.runtime.ObjectArray_unit")
      val entryMain = Val.Global(entryMainName, Type.Ptr)

      val stackBottom = Val.Local(fresh(), Type.Ptr)

      val argc   = Val.Local(fresh(), Type.Int)
      val argv   = Val.Local(fresh(), Type.Ptr)
      val module = Val.Local(fresh(), Type.Module(entry.top))
      val rt     = Val.Local(fresh(), Rt)
      val arr    = Val.Local(fresh(), ObjectArray)
      val exc    = Val.Local(fresh(), nir.Rt.Object)
      val unwind = Next.Unwind(fresh())

      buf += Defn.Define(
        Attrs.None,
        MainName,
        MainSig,
        Seq(
          Inst.Label(fresh(), Seq(argc, argv)),
          Inst.Let(stackBottom.name, Op.Stackalloc(Type.Ptr, Val.Long(0))),
          Inst.Let(
            Op.Store(Type.Ptr,
                     Val.Global(stackBottomName, Type.Ptr),
                     stackBottom)),
          Inst.Let(Op.Call(InitSig, Init, Seq(), unwind)),
          Inst.Let(rt.name, Op.Module(Rt.name, unwind)),
          Inst.Let(arr.name,
                   Op.Call(RtInitSig, RtInit, Seq(rt, argc, argv), unwind)),
          Inst.Let(module.name, Op.Module(entry.top, unwind)),
          Inst.Let(Op.Call(entryMainTy, entryMain, Seq(module, arr), unwind)),
          Inst.Let(Op.Call(RtLoopSig, RtLoop, Seq(module), unwind)),
          Inst.Ret(Val.Int(0)),
          Inst.Label(unwind.name, Seq(exc)),
          Inst.Let(
            Op.Call(PrintStackTraceSig, PrintStackTrace, Seq(exc), Next.None)),
          Inst.Ret(Val.Int(1))
        )
      )
    }

    def genStackBottom(): Unit =
      buf += Defn.Var(Attrs.None, stackBottomName, Type.Ptr, Val.Null)

    def genModuleArray(): Unit =
      buf +=
        Defn.Var(Attrs.None,
                 moduleArrayName,
                 meta.moduleArray.value.ty,
                 meta.moduleArray.value)

    def genModuleArraySize(): Unit =
      buf +=
        Defn.Var(Attrs.None,
                 moduleArraySizeName,
                 Type.Int,
                 Val.Int(meta.moduleArray.size))

    def genObjectArrayId() = {
      val objectArray =
        top.nodes(Global.Top("scala.scalanative.runtime.ObjectArray"))

      buf += Defn.Var(Attrs.None,
                      objectArrayIdName,
                      Type.Int,
                      Val.Int(objectArray.id))
    }

    def genTraitDispatchTables() = {
      buf += tables.dispatchDefn
      buf += tables.classHasTraitDefn
      buf += tables.traitHasTraitDefn
    }
  }

  private object Impl {
    val ClassHasTraitName =
      Global.Member(Global.Top("__extern"), "extern.__check_class_has_trait")
    val ClassHasTraitSig = Type.Function(Seq(Type.Int, Type.Int), Type.Bool)

    val TraitHasTraitName =
      Global.Member(Global.Top("__extern"), "extern.__check_trait_has_trait")
    val TraitHasTraitSig = Type.Function(Seq(Type.Int, Type.Int), Type.Bool)

    val ObjectArray =
      Type.Class(Global.Top("scala.scalanative.runtime.ObjectArray"))

    val Rt =
      Type.Module(Global.Top("scala.scalanative.runtime.package$"))
    val RtInitSig =
      Type.Function(Seq(Rt, Type.Int, Type.Ptr), ObjectArray)
    val RtInit =
      Val.Global(
        Rt.name member "init_i32_ptr_scala.scalanative.runtime.ObjectArray",
        Type.Ptr)
    val RtLoopSig =
      Type.Function(Seq(Rt), Type.Unit)
    val RtLoop =
      Val.Global(Rt.name member "loop_unit", Type.Ptr)

    val MainName = Global.Top("main")
    val MainSig  = Type.Function(Seq(Type.Int, Type.Ptr), Type.Int)

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

    val stackBottomName = Global.Top("__stack_bottom")

    val moduleArrayName     = Global.Top("__modules")
    val moduleArraySizeName = Global.Top("__modules_size")

    val objectArrayIdName = Global.Top("__object_array_id")
  }

  val depends =
    Seq(ObjectArray.name,
        Rt.name,
        RtInit.name,
        RtLoop.name,
        PrintStackTraceName)

  val injects =
    Seq(InitDecl)
}
