package scala.scalanative
package codegen

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.linker.Class

object Generate {
  import Impl._

  def apply(entry: Global.Top, defns: Seq[Defn])(
      implicit meta: Metadata): Seq[Defn] =
    (new Impl(entry, defns)).generate()

  implicit def linked(implicit meta: Metadata): linker.Result =
    meta.linked

  private class Impl(entry: Global.Top, defns: Seq[Defn])(
      implicit meta: Metadata) {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    def generate(): Seq[Defn] = {
      genDefnsExcludingGenerated()
      genInjects()
      genMain()
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

    def genDefnsExcludingGenerated(): Unit = {
      defns.foreach { defn =>
        if (defn.name != ClassHasTraitName
            && defn.name != TraitHasTraitName) {
          buf += defn
        }
      }
    }

    def genInjects(): Unit = {
      buf += InitDecl
      buf ++= Lower.injects
    }

    def genClassMetadata(): Unit = {
      meta.classes.foreach { cls =>
        val struct = meta.layout(cls).struct
        val rtti   = meta.rtti(cls)

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
        Type.Function(Seq(Type.Ref(entry.top), ObjectArray), Type.Unit)
      val entryMainName =
        Global.Member(entry,
                      Sig.Method("main", Seq(Type.Array(Rt.String), Type.Unit)))
      val entryMain = Val.Global(entryMainName, Type.Ptr)

      val stackBottom = Val.Local(fresh(), Type.Ptr)

      val argc    = Val.Local(fresh(), Type.Int)
      val argv    = Val.Local(fresh(), Type.Ptr)
      val module  = Val.Local(fresh(), Type.Ref(entry.top))
      val rt      = Val.Local(fresh(), Runtime)
      val arr     = Val.Local(fresh(), ObjectArray)
      val exc     = Val.Local(fresh(), nir.Rt.Object)
      val handler = fresh()
      def unwind = {
        val exc = Val.Local(fresh(), nir.Rt.Object)
        Next.Unwind(exc, Next.Label(handler, Seq(exc)))
      }

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
          Inst.Let(rt.name, Op.Module(Runtime.name), unwind),
          Inst.Let(arr.name,
                   Op.Call(RuntimeInitSig, RuntimeInit, Seq(rt, argc, argv)),
                   unwind),
          Inst.Let(module.name, Op.Module(entry.top), unwind),
          Inst.Let(Op.Call(entryMainTy, entryMain, Seq(module, arr)), unwind),
          Inst.Let(Op.Call(RuntimeLoopSig, RuntimeLoop, Seq(module)), unwind),
          Inst.Ret(Val.Int(0)),
          Inst.Label(handler, Seq(exc)),
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

          if (cls.isConstantModule) {
            val moduleTyName =
              name.member(Sig.Generated("type"))
            val moduleTyVal =
              Val.Global(moduleTyName, Type.Ptr)
            val instanceName =
              name.member(Sig.Generated("instance"))
            val instanceVal =
              Val.StructValue(Seq(moduleTyVal))
            val instanceDefn = Defn.Const(
              Attrs.None,
              instanceName,
              Type.StructValue(Seq(Type.Ptr)),
              instanceVal
            )

            buf += instanceDefn
          } else {
            val initSig = Type.Function(Seq(clsTy), Type.Unit)
            val init    = Val.Global(name.member(Sig.Ctor(Seq())), Type.Ptr)

            val loadName = name.member(Sig.Generated("load"))
            val loadSig  = Type.Function(Seq(), clsTy)
            val loadDefn = Defn.Define(
              Attrs(inline = Attr.NoInline),
              loadName,
              loadSig,
              Seq(
                Inst.Label(entry, Seq()),
                Inst.Let(slot.name,
                         Op.Elem(Type.Ptr,
                                 Val.Global(moduleArrayName, Type.Ptr),
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
                Inst.Let(Op.Call(initSig, init, Seq(alloc)), Next.None),
                Inst.Ret(alloc)
              )
            )

            buf += loadDefn
          }
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
      val tpes = Seq("BoxedUnit",
                     "Boolean",
                     "Char",
                     "Byte",
                     "Short",
                     "Int",
                     "Long",
                     "Float",
                     "Double",
                     "Object")
      val ids = tpes.map(tpe2arrayId).sorted

      // all the arrays have a common superclass, therefore their ids are consecutive
      val min = ids.head
      val max = ids.last
      if (ids != (min to max)) {
        throw new Exception(
          s"Ids for all known arrays ($tpes) are not consecutive!")
      }

      buf += Defn.Var(Attrs.None, arrayIdsMinName, Type.Int, Val.Int(min))

      buf += Defn.Var(Attrs.None, arrayIdsMaxName, Type.Int, Val.Int(max))

    }

    def genTraitDispatchTables(): Unit = {
      buf += meta.dispatchTable.dispatchDefn
      buf += meta.hasTraitTables.classHasTraitDefn
      buf += meta.hasTraitTables.traitHasTraitDefn
    }
  }

  private object Impl {
    val rttiModule = Global.Top("java.lang.rtti$")

    val ClassHasTraitName =
      Global.Member(rttiModule, Sig.Extern("__check_class_has_trait"))
    val ClassHasTraitSig = Type.Function(Seq(Type.Int, Type.Int), Type.Bool)

    val TraitHasTraitName =
      Global.Member(rttiModule, Sig.Extern("__check_trait_has_trait"))
    val TraitHasTraitSig = Type.Function(Seq(Type.Int, Type.Int), Type.Bool)

    val ObjectArray =
      Type.Ref(Global.Top("scala.scalanative.runtime.ObjectArray"))

    val Runtime =
      Rt.Runtime
    val RuntimeInitSig =
      Type.Function(Seq(Runtime, Type.Int, Type.Ptr), ObjectArray)
    val RuntimeInitName =
      Runtime.name.member(
        Sig.Method("init", Seq(Type.Int, Type.Ptr, Type.Array(Rt.String))))
    val RuntimeInit =
      Val.Global(RuntimeInitName, Type.Ptr)
    val RuntimeLoopSig =
      Type.Function(Seq(Runtime), Type.Unit)
    val RuntimeLoopName =
      Runtime.name.member(Sig.Method("loop", Seq(Type.Unit)))
    val RuntimeLoop =
      Val.Global(RuntimeLoopName, Type.Ptr)

    val MainName = extern("main")
    val MainSig  = Type.Function(Seq(Type.Int, Type.Ptr), Type.Int)

    val ThrowableName = Global.Top("java.lang.Throwable")
    val Throwable     = Type.Ref(ThrowableName)

    val PrintStackTraceSig =
      Type.Function(Seq(Throwable), Type.Unit)
    val PrintStackTraceName =
      ThrowableName.member(Sig.Method("printStackTrace", Seq(Type.Unit)))
    val PrintStackTrace =
      Val.Global(PrintStackTraceName, Type.Ptr)

    val InitSig  = Type.Function(Seq(), Type.Unit)
    val Init     = Val.Global(extern("scalanative_init"), Type.Ptr)
    val InitDecl = Defn.Declare(Attrs.None, Init.name, InitSig)

    val stackBottomName     = extern("__stack_bottom")
    val moduleArrayName     = extern("__modules")
    val moduleArraySizeName = extern("__modules_size")
    val objectArrayIdName   = extern("__object_array_id")
    val arrayIdsMinName     = extern("__array_ids_min")
    val arrayIdsMaxName     = extern("__array_ids_max")

    private def extern(id: String): Global =
      Global.Member(Global.Top("__"), Sig.Extern(id))
  }

  val depends =
    Seq(ObjectArray.name,
        Runtime.name,
        RuntimeInit.name,
        RuntimeLoop.name,
        PrintStackTraceName)
}
