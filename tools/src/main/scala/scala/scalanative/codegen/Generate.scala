package scala.scalanative
package codegen

import scala.collection.mutable

import scala.scalanative.build.Logger
import scala.scalanative.linker.{
  Class, Field, ReachabilityAnalysis, ScopeInfo, Unavailable
}

// scalafmt: { maxColumn = 120}
private[codegen] object Generate {
  private implicit val pos: nir.SourcePosition = nir.SourcePosition.NoPosition
  private implicit val scopeId: nir.ScopeId = nir.ScopeId.TopLevel
  import Impl._

  def apply(entry: Option[nir.Global.Top], defns: Seq[nir.Defn])(implicit
      meta: Metadata
  ): Seq[nir.Defn] =
    (new Impl(entry, defns)).generate()

  implicit def reachabilityAnalysis(implicit meta: Metadata): ReachabilityAnalysis.Result = meta.analysis

  private class Impl(entry: Option[nir.Global.Top], defns: Seq[nir.Defn])(implicit
      meta: Metadata
  ) {
    val buf = mutable.UnrolledBuffer.empty[nir.Defn]

    def generate(): Seq[nir.Defn] = {
      genDefnsExcludingGenerated()
      genInjects()
      entry.fold(genLibraryInit())(genMain(_))
      genClassMetadata()
      genTraitMetadata()
      genModuleAccessors()
      genModuleArray()
      genModuleArraySize()
      genScanableTypesIds()
      genWeakRefUtils()
      genArrayIds()

      buf.toSeq
    }

    def genDefnsExcludingGenerated(): Unit = {
      buf ++= defns
    }

    def genInjects(): Unit = {
      buf += InitDecl
      buf ++= Lower.injects
    }

    def genClassMetadata(): Unit = {
      meta.classes.foreach { cls =>
        val rtti = meta.rtti(cls)
        val pos = cls.position
        buf += nir.Defn.Var(nir.Attrs.None, rtti.name, rtti.struct, rtti.value)(pos)
      }
    }

    def genTraitMetadata(): Unit = {
      meta.traits.foreach { trt =>
        val rtti = meta.rtti(trt)
        val pos = trt.position
        buf += nir.Defn.Var(nir.Attrs.None, rtti.name, rtti.struct, rtti.value)(pos)
      }
    }

    /* Generate set of instructions using common exception handling, generate method
     * would return 0 if would execute successfully exception and 1 in otherwise */
    private def withExceptionHandler(
        body: (() => nir.Next.Unwind) => Seq[nir.Inst]
    )(implicit fresh: nir.Fresh): Seq[nir.Inst] = {
      val exc = nir.Val.Local(fresh(), nir.Rt.Throwable)
      val handler, thread, ueh, uehHandler = fresh()

      def unwind(): nir.Next.Unwind = {
        val exc = nir.Val.Local(fresh(), nir.Rt.Throwable)
        nir.Next.Unwind(exc, nir.Next.Label(handler, Seq(exc)))
      }
      body(unwind) ++ Seq(
        nir.Inst.Ret(nir.Val.Int(0)),
        nir.Inst.Label(handler, Seq(exc)),
        nir.Inst.Let(
          thread,
          nir.Op.Call(JavaThreadCurrentThreadSig, nir.Val.Global(JavaThreadCurrentThread, nir.Type.Ptr), Seq()),
          nir.Next.None
        ),
        nir.Inst.Let(
          ueh,
          nir.Op.Call(
            JavaThreadGetUEHSig,
            nir.Val.Global(JavaThreadGetUEH, nir.Type.Ptr),
            Seq(nir.Val.Local(thread, JavaThreadRef))
          ),
          nir.Next.None
        ),
        nir.Inst.Let(
          fresh(),
          nir.Op.Call(
            RuntimeExecuteUEHSig,
            nir.Val.Global(RuntimeExecuteUEH, nir.Type.Ptr),
            Seq(nir.Val.Null, nir.Val.Local(ueh, JavaThreadUEHRef), nir.Val.Local(thread, JavaThreadRef), exc)
          ),
          nir.Next.None
        ),
        nir.Inst.Ret(nir.Val.Int(1))
      )
    }

    /* Generate class initializers to handle class instantiated using reflection */
    private def genClassInitializersCalls(
        unwind: () => nir.Next
    )(implicit fresh: nir.Fresh): Seq[nir.Inst] = {
      defns.collect {
        case defn @ nir.Defn.Define(_, name: nir.Global.Member, _, _, _) if name.sig.isClinit =>
          nir.Inst.Let(
            nir.Op.Call(
              nir.Type.Function(Seq.empty, nir.Type.Unit),
              nir.Val.Global(name, nir.Type.Ref(name.owner)),
              Seq.empty
            ),
            unwind()
          )(implicitly, defn.pos, implicitly)
      }
    }

    private def genGcInit(unwindProvider: () => nir.Next)(implicit fresh: nir.Fresh) = {
      def unwind: nir.Next = unwindProvider()

      Seq(
        // Init GC
        nir.Inst.Let(nir.Op.Call(InitSig, Init, Seq.empty), unwind)
      )
    }

    /* Injects definition of library initializers that needs to be called, when using Scala Native as shared library.
     * Injects basic handling of exceptions, prints stack trace and returns non-zero value on exception or 0 otherwise */
    def genLibraryInit(): Unit = {
      implicit val fresh: nir.Fresh = nir.Fresh()

      buf += nir.Defn.Define(
        nir.Attrs.None.withIsExtern(true),
        LibraryInitName,
        LibraryInitSig,
        withExceptionHandler { unwindProvider =>
          Seq(nir.Inst.Label(fresh(), Nil)) ++
            genGcInit(unwindProvider) ++
            genClassInitializersCalls(unwindProvider)
        }
      )
    }

    def genMain(entry: nir.Global.Top): Unit = {
      validateMainEntry(entry)

      implicit val fresh = nir.Fresh()
      buf += nir.Defn.Define(
        nir.Attrs.None,
        MainName,
        MainSig,
        withExceptionHandler { unwindProvider =>
          val entryMainTy = nir.Type.Function(Seq(ObjectArray), nir.Type.Unit)
          val entryMainMethod =
            nir.Val.Global(entry.member(nir.Rt.ScalaMainSig), nir.Type.Ptr)

          val argc = nir.Val.Local(fresh(), nir.Type.Int)
          val argv = nir.Val.Local(fresh(), nir.Type.Ptr)
          val rt = nir.Val.Local(fresh(), Runtime)
          val arr = nir.Val.Local(fresh(), ObjectArray)

          def unwind = unwindProvider()
          Seq(nir.Inst.Label(fresh(), Seq(argc, argv))) ++
            genGcInit(unwindProvider) ++
            genClassInitializersCalls(unwindProvider) ++
            Seq(
              nir.Inst.Let(rt.id, nir.Op.Module(Runtime.name), unwind),
              nir.Inst.Let(
                arr.id,
                nir.Op.Call(RuntimeInitSig, RuntimeInit, Seq(rt, argc, argv)),
                unwind
              ),
              nir.Inst.Let(
                nir.Op.Call(entryMainTy, entryMainMethod, Seq(arr)),
                unwind
              ),
              nir.Inst.Let(nir.Op.Call(RuntimeOnShutdownSig, RuntimeOnShutdown, Seq(rt)), unwind)
            )
        }
      )
    }

    def genModuleAccessors(): Unit = {
      val LoadModuleSig = nir.Type.Function(
        Seq(nir.Type.Ptr, nir.Type.Ptr, nir.Type.Size, nir.Type.Ptr),
        nir.Type.Ptr
      )
      val LoadModuleDecl = nir.Defn.Declare(
        nir.Attrs.None.withIsExtern(true),
        extern("__scalanative_loadModule"),
        LoadModuleSig
      )
      val LoadModule = nir.Val.Global(LoadModuleDecl.name, nir.Type.Ptr)
      val useSynchronizedAccessors = meta.platform.isMultithreadingEnabled
      if (useSynchronizedAccessors) {
        buf += LoadModuleDecl
      }

      meta.classes.foreach { cls =>
        if (cls.isModule && cls.allocated) {
          val name = cls.name
          val clsTy = cls.ty

          implicit val fresh = nir.Fresh()
          implicit val pos = cls.position

          val entry = fresh()
          val existing = fresh()
          val initialize = fresh()

          val slot = nir.Val.Local(fresh(), nir.Type.Ptr)
          val self = nir.Val.Local(fresh(), clsTy)
          val cond = nir.Val.Local(fresh(), nir.Type.Bool)
          val alloc = nir.Val.Local(fresh(), clsTy)

          if (cls.isConstantModule) {
            val moduleTyName = name.member(nir.Sig.Generated("type"))
            val moduleTyVal = nir.Val.Global(moduleTyName, nir.Type.Ptr)
            val instanceName = name.member(nir.Sig.Generated("instance"))
            val instanceVal = nir.Val.StructValue(moduleTyVal :: meta.lockWordVals)
            // Needs to be defined as var, const does not allow to modify lock-word field
            val instanceDefn = nir.Defn.Var(
              nir.Attrs.None,
              instanceName,
              meta.layouts.ObjectHeader.layout,
              instanceVal
            )

            buf += instanceDefn
          } else {
            val initSig = nir.Type.Function(Seq(clsTy), nir.Type.Unit)
            val init = nir.Val.Global(name.member(nir.Sig.Ctor(Seq.empty)), nir.Type.Ptr)

            val loadName = name.member(nir.Sig.Generated("load"))
            val loadSig = nir.Type.Function(Seq.empty, clsTy)

            val selectSlot = nir.Op.Elem(
              nir.Type.Ptr,
              nir.Val.Global(moduleArrayName, nir.Type.Ptr),
              Seq(nir.Val.Int(meta.moduleArray.index(cls)))
            )

            /*  singlethreaded module load
             *  Uses simplified algorithm with lower overhead
             *  val instance = module[moduleId]
             *  if (instance != null) instance
             *  else {
             *    val instance = alloc
             *    module[moduleId] = instance
             *    moduleCtor(instance)
             *    instance
             *  }
             */
            def loadSinglethreadImpl: Seq[nir.Inst] = {
              Seq(
                nir.Inst.Label(entry, Seq.empty),
                nir.Inst.Let(slot.id, selectSlot, nir.Next.None),
                nir.Inst.Let(self.id, nir.Op.Load(clsTy, slot), nir.Next.None),
                nir.Inst.Let(
                  cond.id,
                  nir.Op.Comp(nir.Comp.Ine, nir.Rt.Object, self, nir.Val.Null),
                  nir.Next.None
                ),
                nir.Inst.If(cond, nir.Next(existing), nir.Next(initialize)),
                nir.Inst.Label(existing, Seq.empty),
                nir.Inst.Ret(self),
                nir.Inst.Label(initialize, Seq.empty),
                nir.Inst.Let(
                  alloc.id,
                  nir.Op.Classalloc(name, zone = None),
                  nir.Next.None
                ),
                nir.Inst.Let(nir.Op.Store(clsTy, slot, alloc), nir.Next.None),
                nir.Inst.Let(nir.Op.Call(initSig, init, Seq(alloc)), nir.Next.None),
                nir.Inst.Ret(alloc)
              )
            }

            /*  // Multithreading-safe module load
             *  val slot = modules.at(moduleId)
             *  return __scalanative_loadModule(slot, rtti, size, ctor)
             *
             *  Underlying C function implements the main logic of module initialization and synchronization.
             *  Safety of safe multithreaded initialization comes with the increased complexity and overhead.
             *  For single-threaded usage we use the old implementation
             */
            def loadMultithreadingSafeImpl: Seq[nir.Inst] = {
              val size = meta.layout(cls).size
              val rtti = meta.rtti(cls).const

              Seq(
                nir.Inst.Label(entry, Seq.empty),
                nir.Inst.Let(slot.id, selectSlot, nir.Next.None),
                nir.Inst.Let(
                  self.id,
                  nir.Op.Call(
                    LoadModuleSig,
                    LoadModule,
                    Seq(slot, rtti, nir.Val.Size(size), init)
                  ),
                  nir.Next.None
                ),
                nir.Inst.Ret(self)
              )
            }

            // Generate definition of module load function such as "module$G4load"
            // The callers will be generated while lowering "Op.Module", see "codegen/Lower.scala".
            val loadDefn = nir.Defn.Define(
              nir.Attrs.None.withInlineHint(
                if (useSynchronizedAccessors) nir.Attr.MayInline
                else nir.Attr.NoInline
              ),
              loadName,
              loadSig,
              if (useSynchronizedAccessors) loadMultithreadingSafeImpl
              else loadSinglethreadImpl
            )

            buf += loadDefn
          }
        }
      }
    }

    def genModuleArray(): Unit =
      buf +=
        nir.Defn.Var(
          nir.Attrs.None,
          moduleArrayName,
          meta.moduleArray.value.ty,
          meta.moduleArray.value
        )

    def genModuleArraySize(): Unit =
      buf +=
        nir.Defn.Const(
          nir.Attrs.None,
          moduleArraySizeName,
          nir.Type.Int,
          nir.Val.Int(meta.moduleArray.size)
        )

    private def tpe2arrayId(tpe: String): Int = {
      val clazz =
        reachabilityAnalysis
          .infos(nir.Global.Top(s"scala.scalanative.runtime.${tpe}Array"))
          .asInstanceOf[Class]

      meta.ids(clazz)
    }

    def genScanableTypesIds(): Unit = {
      // Ids of array types that can contain pointers
      for ((symbol, tpeName) <- Seq(
            (objectArrayIdName, "Object"),
            (blobArrayIdName, "Blob")
          )) {
        buf += nir.Defn.Const(
          nir.Attrs.None,
          symbol,
          nir.Type.Int,
          nir.Val.Int(tpe2arrayId(tpeName))
        )
      }
      // Boxed pointer can conain erased reference to objects
      val boxedPtrClass = reachabilityAnalysis.infos(nir.Rt.BoxedPtr.name).asInstanceOf[Class]
      buf += nir.Defn.Const(
        nir.Attrs.None,
        boxedPtrIdName,
        nir.Type.Int,
        nir.Val.Int(meta.ids(boxedPtrClass))
      )
    }

    def genWeakRefUtils(): Unit = {
      def addToBuf(name: nir.Global.Member, value: Int) =
        buf +=
          nir.Defn.Const(
            nir.Attrs.None,
            name,
            nir.Type.Int,
            nir.Val.Int(value)
          )

      val WeakReferenceClass = nir.Global.Top("java.lang.ref.WeakReference")
      val WeakReferenceGCReferent = WeakReferenceClass.member(
        nir.Sig.Field("_gc_modified_referent")
      )
      def weakRefClsInfo = reachabilityAnalysis.infos
        .get(WeakReferenceClass)
        .collect { case cls: Class if cls.allocated => cls }
      def weakRefReferentField =
        reachabilityAnalysis.infos
          .get(WeakReferenceGCReferent)
          .collect { case field: Field => field }

      val (weakRefIdsMin, weakRefIdsMax, modifiedFieldOffset) =
        weakRefClsInfo
          .zip(weakRefReferentField)
          .headOption
          .fold((-1, -1, -1)) {
            case (weakRef, weakRefReferantField) =>
              // if WeakReferences are being compiled and therefore supported
              val layout = meta.layout(weakRef)
              val gcModifiedFieldReferentIdx = layout
                .index(weakRefReferantField)
                .ensuring(
                  _ > 0,
                  "Runtime implementation error, no \"_gc_modified_referent\" field in java.lang.ref.WeakReference"
                )
              val gcModifiedFieldReferentOffset = layout.layout
                .tys(gcModifiedFieldReferentIdx)
                .offset

              (
                meta.ranges(weakRef).start,
                meta.ranges(weakRef).end,
                gcModifiedFieldReferentOffset.toInt
              )
          }
      addToBuf(weakRefIdsMaxName, weakRefIdsMax)
      addToBuf(weakRefIdsMinName, weakRefIdsMin)
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
        "Object",
        "Blob"
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

      buf += nir.Defn.Const(nir.Attrs.None, arrayIdsMinName, nir.Type.Int, nir.Val.Int(min))
      buf += nir.Defn.Const(nir.Attrs.None, arrayIdsMaxName, nir.Type.Int, nir.Val.Int(max))
    }

    private def validateMainEntry(entry: nir.Global.Top): Unit = {
      def fail(reason: String): Nothing =
        util.unsupported(s"Entry ${entry.id} $reason")

      val info = reachabilityAnalysis.infos.getOrElse(entry, fail("not linked"))
      info match {
        case cls: Class =>
          cls.resolve(nir.Rt.ScalaMainSig).getOrElse {
            fail(s"does not contain ${nir.Rt.ScalaMainSig}")
          }
        case _: Unavailable => fail("unavailable")
        case _              => util.unreachable
      }
    }
  }

  private object Impl {
    val rttiModule = nir.Global.Top("scala.scalanative.runtime.rtti$")

    val ObjectArray = nir.Type.Ref(nir.Global.Top("scala.scalanative.runtime.ObjectArray"))

    val Runtime = nir.Rt.Runtime
    val RuntimeInitSig = nir.Type.Function(Seq(Runtime, nir.Type.Int, nir.Type.Ptr), ObjectArray)
    val RuntimeInitName = Runtime.name.member(
      nir.Sig.Method("init", Seq(nir.Type.Int, nir.Type.Ptr, nir.Type.Array(nir.Rt.String)))
    )
    val RuntimeInit = nir.Val.Global(RuntimeInitName, nir.Type.Ptr)
    val RuntimeOnShutdownSig = nir.Type.Function(Seq(Runtime), nir.Type.Unit)
    val RuntimeOnShutdownName = Runtime.name
      .member(nir.Sig.Method("onShutdown", Seq(nir.Type.Unit)))
    val RuntimeOnShutdown = nir.Val.Global(RuntimeOnShutdownName, nir.Type.Ptr)

    val LibraryInitName = extern("ScalaNativeInit")
    val LibraryInitSig = nir.Type.Function(Seq.empty, nir.Type.Int)

    val MainName = extern("main")
    val MainSig = nir.Type.Function(Seq(nir.Type.Int, nir.Type.Ptr), nir.Type.Int)

    val JavaThread = nir.Global.Top("java.lang.Thread")
    val JavaThreadRef = nir.Type.Ref(JavaThread)

    val JavaThreadUEH = nir.Global.Top("java.lang.Thread$UncaughtExceptionHandler")
    val JavaThreadUEHRef = nir.Type.Ref(JavaThreadUEH)

    val JavaThreadCurrentThreadSig = nir.Type.Function(Seq(), JavaThreadRef)
    val JavaThreadCurrentThread = JavaThread.member(
      nir.Sig.Method("currentThread", Seq(JavaThreadRef), scope = nir.Sig.Scope.PublicStatic)
    )

    val JavaThreadGetUEHSig = nir.Type.Function(Seq(JavaThreadRef), JavaThreadUEHRef)
    val JavaThreadGetUEH = JavaThread.member(
      nir.Sig.Method("getUncaughtExceptionHandler", Seq(JavaThreadUEHRef))
    )

    val RuntimeExecuteUEHSig =
      nir.Type.Function(Seq(Runtime, JavaThreadUEHRef, JavaThreadRef, nir.Rt.Throwable), nir.Type.Unit)
    val RuntimeExecuteUEH = Runtime.name.member(
      nir.Sig.Method(
        "executeUncaughtExceptionHandler",
        Seq(JavaThreadUEHRef, JavaThreadRef, nir.Rt.Throwable, nir.Type.Unit)
      )
    )

    val InitSig = nir.Type.Function(Seq.empty, nir.Type.Unit)
    val InitDecl = nir.Defn.Declare(nir.Attrs.None, extern("scalanative_GC_init"), InitSig)
    val Init = nir.Val.Global(InitDecl.name, nir.Type.Ptr)

    val moduleArrayName = extern("__modules")
    val moduleArraySizeName = extern("__modules_size")
    val objectArrayIdName = extern("__object_array_id")
    val blobArrayIdName = extern("__blob_array_id")
    val boxedPtrIdName = extern("__boxed_ptr_id")
    val weakRefIdsMaxName = extern("__weak_ref_ids_max")
    val weakRefIdsMinName = extern("__weak_ref_ids_min")
    val weakRefFieldOffsetName = extern("__weak_ref_field_offset")
    val registryOffsetName = extern("__weak_ref_registry_module_offset")
    val registryFieldOffsetName = extern("__weak_ref_registry_field_offset")
    val arrayIdsMinName = extern("__array_ids_min")
    val arrayIdsMaxName = extern("__array_ids_max")

    private def extern(id: String): nir.Global.Member =
      nir.Global.Member(nir.Global.Top("__"), nir.Sig.Extern(id))
  }

  def depends(implicit platform: PlatformInfo): Seq[nir.Global] = {
    Seq(
      ObjectArray.name,
      Runtime.name,
      RuntimeInit.name,
      RuntimeOnShutdown.name,
      RuntimeExecuteUEH,
      JavaThread,
      JavaThreadCurrentThread,
      JavaThreadGetUEH,
      JavaThreadUEH
    )
  }
}
