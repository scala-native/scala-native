package scala.scalanative
package codegen

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.linker.{Class, ScopeInfo, Unavailable}
import scala.scalanative.build.Logger

object Generate {
  import Impl._

  def apply(entry: Option[Global.Top], defns: Seq[Defn])(implicit
      meta: Metadata
  ): Seq[Defn] =
    (new Impl(entry, defns)).generate()

  implicit def linked(implicit meta: Metadata): linker.Result =
    meta.linked
  private implicit val pos: Position = Position.NoPosition

  private class Impl(entry: Option[Global.Top], defns: Seq[Defn])(implicit
      meta: Metadata
  ) {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    def generate(): Seq[Defn] = {
      genDefnsExcludingGenerated()
      genInjects()
      entry.fold(genLibraryInit())(genMain(_))
      genClassMetadata()
      genClassHasTrait()
      genTraitMetadata()
      genTraitHasTrait()
      genTraitDispatchTables()
      genModuleAccessors()
      genModuleArray()
      genModuleArraySize()
      genObjectArrayId()
      genWeakRefUtils()
      genArrayIds()
      genStackBottom()

      buf.toSeq
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
        val rtti = meta.rtti(cls)
        val pos = cls.position
        buf += Defn.Var(Attrs.None, rtti.name, rtti.struct, rtti.value)(pos)
      }
    }

    def genClassHasTrait(): Unit = {
      implicit val fresh = Fresh()
      val classid, traitid = Val.Local(fresh(), Type.Int)
      val boolptr = Val.Local(fresh(), Type.Ptr)
      val result = Val.Local(fresh(), Type.Bool)

      buf += Defn.Define(
        Attrs(inlineHint = Attr.AlwaysInline),
        ClassHasTraitName,
        ClassHasTraitSig,
        Seq(
          Inst.Label(fresh(), Seq(classid, traitid)),
          Inst.Let(
            boolptr.name,
            Op.Elem(
              meta.hasTraitTables.classHasTraitTy,
              meta.hasTraitTables.classHasTraitVal,
              Seq(Val.Int(0), classid, traitid)
            ),
            Next.None
          ),
          Inst.Let(result.name, Op.Load(Type.Bool, boolptr), Next.None),
          Inst.Ret(result)
        )
      )
    }

    def genTraitMetadata(): Unit = {
      meta.traits.foreach { trt =>
        val rtti = meta.rtti(trt)
        val pos = trt.position
        buf += Defn.Var(Attrs.None, rtti.name, rtti.struct, rtti.value)(pos)
      }
    }

    def genTraitHasTrait(): Unit = {
      implicit val fresh = Fresh()
      val leftid, rightid = Val.Local(fresh(), Type.Int)
      val boolptr = Val.Local(fresh(), Type.Ptr)
      val result = Val.Local(fresh(), Type.Bool)

      buf += Defn.Define(
        Attrs(inlineHint = Attr.AlwaysInline),
        TraitHasTraitName,
        TraitHasTraitSig,
        Seq(
          Inst.Label(fresh(), Seq(leftid, rightid)),
          Inst.Let(
            boolptr.name,
            Op.Elem(
              meta.hasTraitTables.traitHasTraitTy,
              meta.hasTraitTables.traitHasTraitVal,
              Seq(Val.Int(0), leftid, rightid)
            ),
            Next.None
          ),
          Inst.Let(result.name, Op.Load(Type.Bool, boolptr), Next.None),
          Inst.Ret(result)
        )
      )
    }

    /* Generate set of instructions using common exception handling, generate method
     * would return 0 if would execute successfully exception and 1 in otherwise */
    private def withExceptionHandler(
        body: (() => Next.Unwind) => Seq[Inst]
    )(implicit fresh: Fresh): Seq[Inst] = {
      val exc = Val.Local(fresh(), nir.Rt.Object)
      val handler = fresh()

      def unwind(): Next.Unwind = {
        val exc = Val.Local(fresh(), nir.Rt.Object)
        Next.Unwind(exc, Next.Label(handler, Seq(exc)))
      }

      body(unwind) ++ Seq(
        Inst.Ret(Val.Int(0)),
        Inst.Label(handler, Seq(exc)),
        Inst.Let(
          Op.Call(PrintStackTraceSig, PrintStackTrace, Seq(exc)),
          Next.None
        ),
        Inst.Ret(Val.Int(1))
      )
    }

    /* Generate class initializers to handle class instantiated using reflection */
    private def genClassInitializersCalls(
        unwind: () => Next
    )(implicit fresh: Fresh): Seq[Inst] = {
      defns.collect {
        case Defn.Define(_, name: Global.Member, _, _) if name.sig.isClinit =>
          Inst.Let(
            Op.Call(
              Type.Function(Seq.empty, Type.Unit),
              Val.Global(name, Type.Ref(name)),
              Seq.empty
            ),
            unwind()
          )
      }
    }

    private def genGcInit(unwindProvider: () => Next)(implicit fresh: Fresh) = {
      def unwind: Next = unwindProvider()
      val stackBottom = Val.Local(fresh(), Type.Ptr)
      val StackBottomVar = Val.Global(stackBottomName, Type.Ptr)

      Seq(
        // init __stack_bottom variable
        Inst.Let(
          stackBottom.name,
          Op.Stackalloc(Type.Ptr, Val.Long(0)),
          unwind
        ),
        Inst.Let(Op.Store(Type.Ptr, StackBottomVar, stackBottom), unwind),
        // Init GC
        Inst.Let(Op.Call(InitSig, Init, Seq.empty), unwind)
      )
    }

    /* Injects definition of library initializers that needs to be called, when using Scala Native as shared library.
     * Injects basic handling of exceptions, prints stack trace and returns non-zero value on exception or 0 otherwise */
    def genLibraryInit(): Unit = {
      implicit val fresh: Fresh = Fresh()

      buf += Defn.Define(
        Attrs(isExtern = true),
        LibraryInitName,
        LibraryInitSig,
        withExceptionHandler { unwindProvider =>
          Seq(Inst.Label(fresh(), Nil)) ++
            genGcInit(unwindProvider) ++
            genClassInitializersCalls(unwindProvider)
        }
      )
    }

    def genMain(entry: Global.Top): Unit = {
      validateMainEntry(entry)

      implicit val fresh = Fresh()
      buf += Defn.Define(
        Attrs.None,
        MainName,
        MainSig,
        withExceptionHandler { unwindProvider =>
          val entryMainTy = Type.Function(Seq(ObjectArray), Type.Unit)
          val entryMainMethod =
            Val.Global(entry.member(Rt.ScalaMainSig), Type.Ptr)

          val argc = Val.Local(fresh(), Type.Int)
          val argv = Val.Local(fresh(), Type.Ptr)
          val rt = Val.Local(fresh(), Runtime)
          val arr = Val.Local(fresh(), ObjectArray)

          def unwind = unwindProvider()

          Seq(Inst.Label(fresh(), Seq(argc, argv))) ++
            genGcInit(unwindProvider) ++
            genClassInitializersCalls(unwindProvider) ++
            Seq(
              Inst.Let(rt.name, Op.Module(Runtime.name), unwind),
              Inst.Let(
                arr.name,
                Op.Call(RuntimeInitSig, RuntimeInit, Seq(rt, argc, argv)),
                unwind
              ),
              Inst.Let(
                Op.Call(entryMainTy, entryMainMethod, Seq(arr)),
                unwind
              ),
              Inst.Let(Op.Call(RuntimeLoopSig, RuntimeLoop, Seq(rt)), unwind)
            )
        }
      )
    }

    def genStackBottom(): Unit =
      buf += Defn.Var(Attrs.None, stackBottomName, Type.Ptr, Val.Null)

    def genModuleAccessors(): Unit = {
      meta.classes.foreach { cls =>
        if (cls.isModule && cls.allocated) {
          val name = cls.name
          val clsTy = cls.ty

          implicit val fresh = Fresh()
          implicit val pos = cls.position

          val entry = fresh()
          val existing = fresh()
          val initialize = fresh()

          val slot = Val.Local(fresh(), Type.Ptr)
          val self = Val.Local(fresh(), clsTy)
          val cond = Val.Local(fresh(), Type.Bool)
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
              meta.layouts.ObjectHeader.layout,
              instanceVal
            )

            buf += instanceDefn
          } else {
            val initSig = Type.Function(Seq(clsTy), Type.Unit)
            val init = Val.Global(name.member(Sig.Ctor(Seq.empty)), Type.Ptr)

            val loadName = name.member(Sig.Generated("load"))
            val loadSig = Type.Function(Seq.empty, clsTy)
            val loadDefn = Defn.Define(
              Attrs(inlineHint = Attr.NoInline),
              loadName,
              loadSig,
              Seq(
                Inst.Label(entry, Seq.empty),
                Inst.Let(
                  slot.name,
                  Op.Elem(
                    Type.Ptr,
                    Val.Global(moduleArrayName, Type.Ptr),
                    Seq(Val.Int(meta.moduleArray.index(cls)))
                  ),
                  Next.None
                ),
                Inst.Let(self.name, Op.Load(clsTy, slot), Next.None),
                Inst.Let(
                  cond.name,
                  Op.Comp(Comp.Ine, nir.Rt.Object, self, Val.Null),
                  Next.None
                ),
                Inst.If(cond, Next(existing), Next(initialize)),
                Inst.Label(existing, Seq.empty),
                Inst.Ret(self),
                Inst.Label(initialize, Seq.empty),
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
        Defn.Var(
          Attrs.None,
          moduleArrayName,
          meta.moduleArray.value.ty,
          meta.moduleArray.value
        )

    def genModuleArraySize(): Unit =
      buf +=
        Defn.Var(
          Attrs.None,
          moduleArraySizeName,
          Type.Int,
          Val.Int(meta.moduleArray.size)
        )

    private def tpe2arrayId(tpe: String): Int = {
      val clazz =
        linked
          .infos(Global.Top(s"scala.scalanative.runtime.${tpe}Array"))
          .asInstanceOf[Class]

      meta.ids(clazz)
    }

    def genObjectArrayId(): Unit = {
      buf += Defn.Var(
        Attrs.None,
        objectArrayIdName,
        Type.Int,
        Val.Int(tpe2arrayId("Object"))
      )
    }

    def genWeakRefUtils(): Unit = {
      def addToBuf(name: Global, value: Int) =
        buf +=
          Defn.Var(
            Attrs.None,
            name,
            Type.Int,
            Val.Int(value)
          )

      val (weakRefId, modifiedFieldOffset) = linked.infos
        .get(Global.Top("java.lang.ref.WeakReference"))
        .collect { case cls: Class if cls.allocated => cls }
        .fold((-1, -1)) { weakRef =>
          // if WeakReferences are being compiled and therefore supported
          val gcModifiedFieldIndexes: Seq[Int] =
            meta.layout(weakRef).entries.zipWithIndex.collect {
              case (field, index)
                  if field.name.mangle.contains("_gc_modified_") =>
                index
            }

          if (gcModifiedFieldIndexes.size != 1)
            throw new Exception(
              "Exactly one field should have the \"_gc_modified_\" modifier in java.lang.ref.WeakReference"
            )

          (meta.ids(weakRef), gcModifiedFieldIndexes.head)
        }
      addToBuf(weakRefIdName, weakRefId)
      addToBuf(weakRefFieldOffsetName, modifiedFieldOffset)
    }

    def genArrayIds(): Unit = {
      val tpes = Seq(
        "Boolean",
        "Char",
        "Byte",
        "Short",
        "Int",
        "Long",
        "Float",
        "Double",
        "Object"
      )
      val ids = tpes.map(tpe2arrayId).sorted

      // all the arrays have a common superclass, therefore their ids are consecutive
      val min = ids.head
      val max = ids.last
      if (ids != (min to max)) {
        throw new Exception(
          s"Ids for all known arrays ($tpes) are not consecutive!"
        )
      }

      buf += Defn.Var(Attrs.None, arrayIdsMinName, Type.Int, Val.Int(min))

      buf += Defn.Var(Attrs.None, arrayIdsMaxName, Type.Int, Val.Int(max))

    }

    def genTraitDispatchTables(): Unit = {
      buf += meta.dispatchTable.dispatchDefn
      buf += meta.hasTraitTables.classHasTraitDefn
      buf += meta.hasTraitTables.traitHasTraitDefn
    }

    private def validateMainEntry(entry: Global.Top): Unit = {
      def fail(reason: String): Nothing =
        util.unsupported(s"Entry ${entry.id} $reason")

      val info = linked.infos.getOrElse(entry, fail("not linked"))
      info match {
        case cls: Class =>
          cls.resolve(Rt.ScalaMainSig).getOrElse {
            fail(s"does not contain ${Rt.ScalaMainSig}")
          }
        case _: Unavailable => fail("unavailable")
        case _              => util.unreachable
      }
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
        Sig.Method("init", Seq(Type.Int, Type.Ptr, Type.Array(Rt.String)))
      )
    val RuntimeInit =
      Val.Global(RuntimeInitName, Type.Ptr)
    val RuntimeLoopSig =
      Type.Function(Seq(Runtime), Type.Unit)
    val RuntimeLoopName =
      Runtime.name.member(Sig.Method("loop", Seq(Type.Unit)))
    val RuntimeLoop =
      Val.Global(RuntimeLoopName, Type.Ptr)

    val LibraryInitName = extern("ScalaNativeInit")
    val LibraryInitSig = Type.Function(Seq.empty, Type.Int)

    val MainName = extern("main")
    val MainSig = Type.Function(Seq(Type.Int, Type.Ptr), Type.Int)

    val ThrowableName = Global.Top("java.lang.Throwable")
    val Throwable = Type.Ref(ThrowableName)

    val PrintStackTraceSig =
      Type.Function(Seq(Throwable), Type.Unit)
    val PrintStackTraceName =
      ThrowableName.member(Sig.Method("printStackTrace", Seq(Type.Unit)))
    val PrintStackTrace =
      Val.Global(PrintStackTraceName, Type.Ptr)

    val InitSig = Type.Function(Seq.empty, Type.Unit)
    val Init = Val.Global(extern("scalanative_init"), Type.Ptr)
    val InitDecl = Defn.Declare(Attrs.None, Init.name, InitSig)

    val stackBottomName = extern("__stack_bottom")
    val moduleArrayName = extern("__modules")
    val moduleArraySizeName = extern("__modules_size")
    val objectArrayIdName = extern("__object_array_id")
    val weakRefIdName = extern("__weak_ref_id")
    val weakRefFieldOffsetName = extern("__weak_ref_field_offset")
    val registryOffsetName = extern("__weak_ref_registry_module_offset")
    val registryFieldOffsetName = extern("__weak_ref_registry_field_offset")
    val arrayIdsMinName = extern("__array_ids_min")
    val arrayIdsMaxName = extern("__array_ids_max")

    private def extern(id: String): Global =
      Global.Member(Global.Top("__"), Sig.Extern(id))
  }

  val depends =
    Seq(
      ObjectArray.name,
      Runtime.name,
      RuntimeInit.name,
      RuntimeLoop.name,
      PrintStackTraceName
    )
}
