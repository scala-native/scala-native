package scala.scalanative
package codegen

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.linker.Class

object Generate {
  import Impl._

  def apply(entry: Global, defns: Seq[Defn])(
      implicit meta: Metadata): Seq[Defn] =
    (new Impl(entry, defns)).generate()

  implicit def linked(implicit meta: Metadata): linker.Result =
    meta.linked

  private class Impl(entry: Global, defns: Seq[Defn])(implicit meta: Metadata) {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    def generate(): Seq[Defn] = {
      genDefnsExcludingCheckHasTrait()
      genInjects()
      genMain()
      genStructMetadata()
      genClassMetadata()
      genClassHasTrait()
      genTraitMetadata()
      genTraitHasTrait()
      genTraitDispatchTables()
      genModuleAccessors()
      genModuleArray()
      genModuleArraySize()
      genObjectArrayId()
      genArrayIds()
      genStackBottom()
      buf
    }

    def genDefnsExcludingCheckHasTrait(): Unit = {
      defns.foreach { defn =>
        if (defn.name.id != "extern.__check_class_has_trait"
            && defn.name.id != "extern.__check_trait_has_trait") {
          buf += defn
        }
      }
    }

    def genInjects(): Unit = {
      buf += InitDecl
      buf ++= Lower.injects
    }

    def genStructMetadata(): Unit = {
      meta.structs.foreach { struct =>
        val rtti = meta.rtti(struct)

        buf += Defn.Const(Attrs.None, rtti.name, rtti.struct, rtti.value)
      }
    }

    def genClassMetadata(): Unit = {
      meta.classes.foreach { cls =>
        val struct = meta.layout(cls).struct
        val rtti   = meta.rtti(cls)

        buf += Defn.Struct(Attrs.None, struct.name, struct.tys)
        buf += Defn.Const(Attrs.None, rtti.name, rtti.struct, rtti.value)
      }
    }

    def genClassHasTrait(): Unit = {
      implicit val fresh   = Fresh()
      val classid, traitid = Val.Local(fresh(), Type.Int)
      val boolptr          = Val.Local(fresh(), Type.Ptr)
      val result           = Val.Local(fresh(), Type.Bool)

      buf += Defn.Define(
        Attrs(inline = Attr.AlwaysInline),
        ClassHasTraitName,
        ClassHasTraitSig,
        Seq(
          Inst.Label(fresh(), Seq(classid, traitid)),
          Inst.Let(boolptr.name,
                   Op.Elem(meta.hasTraitTables.classHasTraitTy,
                           meta.hasTraitTables.classHasTraitVal,
                           Seq(Val.Int(0), classid, traitid)),
                   Next.None),
          Inst.Let(result.name, Op.Load(Type.Bool, boolptr), Next.None),
          Inst.Ret(result)
        )
      )
    }

    def genTraitMetadata(): Unit = {
      meta.traits.foreach { trt =>
        val rtti = meta.rtti(trt)

        buf += Defn.Const(Attrs.None, rtti.name, rtti.struct, rtti.value)
      }
    }

    def genTraitHasTrait(): Unit = {
      implicit val fresh  = Fresh()
      val leftid, rightid = Val.Local(fresh(), Type.Int)
      val boolptr         = Val.Local(fresh(), Type.Ptr)
      val result          = Val.Local(fresh(), Type.Bool)

      buf += Defn.Define(
        Attrs(inline = Attr.AlwaysInline),
        TraitHasTraitName,
        TraitHasTraitSig,
        Seq(
          Inst.Label(fresh(), Seq(leftid, rightid)),
          Inst.Let(boolptr.name,
                   Op.Elem(meta.hasTraitTables.traitHasTraitTy,
                           meta.hasTraitTables.traitHasTraitVal,
                           Seq(Val.Int(0), leftid, rightid)),
                   Next.None),
          Inst.Let(result.name, Op.Load(Type.Bool, boolptr), Next.None),
          Inst.Ret(result)
        )
      )
    }

    def genMain(): Unit = {
      implicit val fresh = Fresh()
      val entryMainTy =
        Type.Function(Seq(Type.Module(entry.top), ObjectArray), Type.Void)
      val entryMainName =
        Global.Member(entry, "main_arr.java.lang.String_unit")
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
          Inst.Let(stackBottom.name,
                   Op.Stackalloc(Type.Ptr, Val.Long(0)),
                   unwind),
          Inst.Let(Op.Store(Type.Ptr,
                            Val.Global(stackBottomName, Type.Ptr),
                            stackBottom),
                   unwind),
          Inst.Let(Op.Call(InitSig, Init, Seq()), unwind),
          Inst.Let(rt.name, Op.Module(Rt.name), unwind),
          Inst.Let(arr.name,
                   Op.Call(RtInitSig, RtInit, Seq(rt, argc, argv)),
                   unwind),
          Inst.Let(module.name, Op.Module(entry.top), unwind),
          Inst.Let(Op.Call(entryMainTy, entryMain, Seq(module, arr)), unwind),
          Inst.Let(Op.Call(RtLoopSig, RtLoop, Seq(module)), unwind),
          Inst.Ret(Val.Int(0)),
          Inst.Label(unwind.name, Seq(exc)),
          Inst.Let(Op.Call(PrintStackTraceSig, PrintStackTrace, Seq(exc)),
                   Next.None),
          Inst.Ret(Val.Int(1))
        )
      )
    }

    def genStackBottom(): Unit =
      buf += Defn.Var(Attrs.None, stackBottomName, Type.Ptr, Val.Null)

    def genModuleAccessors(): Unit = {
      meta.classes.foreach { cls =>
        if (cls.isModule && cls.allocated) {
          val name  = cls.name
          val clsTy = cls.ty

          implicit val fresh = Fresh()

          val entry      = fresh()
          val existing   = fresh()
          val initialize = fresh()

          val slot  = Val.Local(fresh(), Type.Ptr)
          val self  = Val.Local(fresh(), clsTy)
          val cond  = Val.Local(fresh(), Type.Bool)
          val alloc = Val.Local(fresh(), clsTy)

          val initCall = if (cls.isStaticModule) {
            Inst.None
          } else {
            val initSig = Type.Function(Seq(clsTy), Type.Void)
            val init    = Val.Global(name member "init", Type.Ptr)

            Inst.Let(Op.Call(initSig, init, Seq(alloc)), Next.None)
          }

          val loadName = name member "load"
          val loadSig  = Type.Function(Seq(), clsTy)
          val loadDefn = Defn.Define(
            Attrs.None,
            loadName,
            loadSig,
            Seq(
              Inst.Label(entry, Seq()),
              Inst.Let(slot.name,
                       Op.Elem(Type.Ptr,
                               Val.Global(Global.Top("__modules"), Type.Ptr),
                               Seq(Val.Int(meta.moduleArray.index(cls)))),
                       Next.None),
              Inst.Let(self.name, Op.Load(clsTy, slot), Next.None),
              Inst.Let(cond.name,
                       Op.Comp(Comp.Ine, nir.Rt.Object, self, Val.Null),
                       Next.None),
              Inst.If(cond, Next(existing), Next(initialize)),
              Inst.Label(existing, Seq()),
              Inst.Ret(self),
              Inst.Label(initialize, Seq()),
              Inst.Let(alloc.name, Op.Classalloc(name), Next.None),
              Inst.Let(Op.Store(clsTy, slot, alloc), Next.None),
              initCall,
              Inst.Ret(alloc)
            )
          )

          buf += loadDefn
        }
      }
    }

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

    private def tpe2arrayId(tpe: String): Int = {
      val clazz =
        linked
          .infos(Global.Top(s"scala.scalanative.runtime.${tpe}Array"))
          .asInstanceOf[Class]

     meta.ids(clazz)
    }

    def genObjectArrayId(): Unit = {
      buf += Defn.Var(Attrs.None,
                      objectArrayIdName,
                      Type.Int,
                      Val.Int(tpe2arrayId("Object")))
    }

    def genArrayIds(): Unit = {
      val tpes = Seq("Unit", "Boolean", "Char", "Byte", "Short", "Int", "Long", "Float", "Double", "Object")
      val ids = tpes.map(tpe2arrayId).sorted

      buf += Defn.Var(Attrs.None,
                      arrayIdsName,
                      Type.Array(Type.Int),
                      Val.ArrayValue(Type.Int, ids.map(Val.Int)))

    }

    def genTraitDispatchTables(): Unit = {
      buf += meta.dispatchTable.dispatchDefn
      buf += meta.hasTraitTables.classHasTraitDefn
      buf += meta.hasTraitTables.traitHasTraitDefn
    }
  }

  private object Impl {
    val ClassHasTraitName = Global.Top("__check_class_has_trait")
    val ClassHasTraitSig  = Type.Function(Seq(Type.Int, Type.Int), Type.Bool)

    val TraitHasTraitName = Global.Top("__check_trait_has_trait")
    val TraitHasTraitSig  = Type.Function(Seq(Type.Int, Type.Int), Type.Bool)

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

    val arrayIdsName = Global.Top("__array_ids")
  }

  val depends =
    Seq(ObjectArray.name,
        Rt.name,
        RtInit.name,
        RtLoop.name,
        PrintStackTraceName)
}
