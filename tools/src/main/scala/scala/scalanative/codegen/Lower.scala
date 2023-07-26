package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.util.{ScopedVar, unsupported}
import scalanative.nir._
import scalanative.linker._
import scalanative.interflow.UseDef.eliminateDeadCode

object Lower {

  def apply(
      defns: Seq[Defn]
  )(implicit meta: Metadata, logger: build.Logger): Seq[Defn] =
    (new Impl).onDefns(defns)

  private final class Impl(implicit meta: Metadata, logger: build.Logger)
      extends Transform {
    import meta._
    import meta.config
    import meta.layouts.{Rtti, ClassRtti, ArrayHeader}

    implicit val linked: Result = meta.linked

    val Object = linked.infos(Rt.Object.name).asInstanceOf[Class]

    private val zero = Val.Int(0)
    private val one = Val.Int(1)
    val RttiClassIdPath = Seq(zero, Val.Int(Rtti.ClassIdIdx))
    val RttiTraitIdPath = Seq(zero, Val.Int(Rtti.TraitIdIdx))
    val ClassRttiDynmapPath = Seq(zero, Val.Int(ClassRtti.DynmapIdx))
    val ClassRttiVtablePath = Seq(zero, Val.Int(ClassRtti.VtableIdx))
    val ArrayHeaderLengthPath = Seq(zero, Val.Int(ArrayHeader.LengthIdx))

    // Type of the bare runtime type information struct.
    private val classRttiType =
      rtti(linked.infos(Global.Top("java.lang.Object"))).struct

    // Names of the fields of the java.lang.String in the memory layout order.
    private val stringFieldNames = {
      val node = ClassRef.unapply(Rt.StringName).get
      val names = layout(node).entries.map(_.name)
      assert(names.length == 4, "java.lang.String is expected to have 4 fields")
      names
    }

    private val fresh = new util.ScopedVar[Fresh]
    private val unwindHandler = new util.ScopedVar[Option[Local]]
    private val currentDefn = new util.ScopedVar[Defn.Define]
    private val nullGuardedVals = mutable.Set.empty[Val]
    private def currentDefnRetType = {
      val Type.Function(_, ret) = currentDefn.get.ty: @unchecked
      ret
    }

    private val unreachableSlowPath = mutable.Map.empty[Option[Local], Local]
    private val nullPointerSlowPath = mutable.Map.empty[Option[Local], Local]
    private val divisionByZeroSlowPath = mutable.Map.empty[Option[Local], Local]
    private val classCastSlowPath = mutable.Map.empty[Option[Local], Local]
    private val outOfBoundsSlowPath = mutable.Map.empty[Option[Local], Local]
    private val noSuchMethodSlowPath = mutable.Map.empty[Option[Local], Local]

    private def unwind: Next =
      unwindHandler.get.fold[Next](Next.None) { handler =>
        val exc = Val.Local(fresh(), Rt.Object)
        Next.Unwind(exc, Next.Label(handler, Seq(exc)))
      }

    override def onDefns(defns: Seq[Defn]): Seq[Defn] = {
      val buf = mutable.UnrolledBuffer.empty[Defn]

      defns.foreach {
        case _: Defn.Class | _: Defn.Module | _: Defn.Trait =>
          ()
        case Defn.Declare(attrs, MethodRef(_: Class | _: Trait, _), _)
            if !attrs.isExtern =>
          ()
        case Defn.Var(attrs, FieldRef(_: Class, _), _, _) if !attrs.isExtern =>
          ()
        case defn =>
          buf += onDefn(defn)
      }

      buf.toSeq
    }

    override def onDefn(defn: Defn): Defn = defn match {
      case defn: Defn.Define =>
        val Type.Function(_, ty) = defn.ty: @unchecked
        ScopedVar.scoped(
          fresh := Fresh(defn.insts),
          currentDefn := defn
        ) {
          try super.onDefn(defn)
          finally nullGuardedVals.clear()
        }
      case _ =>
        super.onDefn(defn)
    }

    override def onType(ty: Type): Type = ty

    def genNext(buf: Buffer, next: Next)(implicit pos: Position): Next = {
      next match {
        case Next.Unwind(exc, next) => Next.Unwind(exc, genNext(buf, next))
        case Next.Case(value, next) =>
          Next.Case(genVal(buf, value), genNext(buf, next))
        case Next.Label(name, args) =>
          Next.Label(name, args.map(genVal(buf, _)))
        case n => n
      }
    }

    private def optionallyBoxedUnit(v: nir.Val)(implicit
        pos: nir.Position
    ): nir.Val = {
      require(
        v.ty == Type.Unit,
        s"Definition is expected to return Unit type, found ${v.ty}"
      )
      if (currentDefnRetType == Type.Unit) Val.Unit
      else unit
    }

    override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
      val buf = new nir.Buffer()(fresh)
      val handlers = new nir.Buffer()(fresh)

      buf += insts.head

      def newUnwindHandler(next: Next)(implicit pos: Position): Option[Local] =
        next match {
          case Next.None =>
            None
          case Next.Unwind(exc, next) =>
            val handler = fresh()
            handlers.label(handler, Seq(exc))
            handlers.jump(next)
            Some(handler)
          case _ =>
            util.unreachable
        }

      insts.foreach {
        case inst @ Inst.Let(n, Op.Var(ty), unwind) =>
          buf.let(n, Op.Stackalloc(ty, one), unwind)(inst.pos)
        case _ =>
          ()
      }

      val Inst.Label(firstLabel, _) = insts.head: @unchecked
      val labelPositions = insts
        .collect { case Inst.Label(id, _) => id }
        .zipWithIndex
        .toMap
      var currentBlockPosition = labelPositions(firstLabel)

      genThisValueNullGuardIfUsed(
        currentDefn.get,
        buf,
        () => newUnwindHandler(Next.None)(insts.head.pos)
      )

      insts.tail.foreach {
        case inst @ Inst.Let(n, op, unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)(inst.pos)
          ) {
            genLet(buf, n, op)(inst.pos)
          }

        case inst @ Inst.Throw(v, unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)(inst.pos)
          ) {
            genThrow(buf, v)(inst.pos)
          }

        case inst @ Inst.Unreachable(unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)(inst.pos)
          ) {
            genUnreachable(buf)(inst.pos)
          }

        case inst @ Inst.Ret(v) =>
          implicit val pos: Position = inst.pos
          currentDefn.get.name match {
            case Global.Member(ClassRef(cls), sig)
                if sig.isCtor && cls.hasFinalFields =>
              // Release memory fence after initialization of constructor with final fields
              buf.fence(MemoryOrder.Release)
            case _ => ()
          }
          genGCSafepoint(buf)
          val retVal =
            if (v.ty == Type.Unit) optionallyBoxedUnit(v)
            else genVal(buf, v)
          buf += Inst.Ret(retVal)

        case inst @ Inst.Jump(next) =>
          implicit val pos: Position = inst.pos
          // Generate safepoint before backward jumps, eg. in loops
          next match {
            case Next.Label(target, _)
                if labelPositions(target) < currentBlockPosition =>
              genGCSafepoint(buf)
            case _ => ()
          }
          buf += Inst.Jump(genNext(buf, next))

        case inst @ Inst.Label(name, _) =>
          currentBlockPosition = labelPositions(name)
          buf += inst

        case inst =>
          buf += inst
      }

      implicit val pos: Position = Position.NoPosition
      genNullPointerSlowPath(buf)
      genDivisionByZeroSlowPath(buf)
      genClassCastSlowPath(buf)
      genUnreachableSlowPath(buf)
      genOutOfBoundsSlowPath(buf)
      genNoSuchMethodSlowPath(buf)

      nullPointerSlowPath.clear()
      divisionByZeroSlowPath.clear()
      classCastSlowPath.clear()
      unreachableSlowPath.clear()
      outOfBoundsSlowPath.clear()
      noSuchMethodSlowPath.clear()

      buf ++= handlers

      eliminateDeadCode(buf.toSeq.map(onInst))
    }

    override def onInst(inst: Inst): Inst = {
      implicit def pos: nir.Position = inst.pos
      inst match {
        case Inst.Ret(v) if v.ty == Type.Unit =>
          Inst.Ret(optionallyBoxedUnit(v))
        case _ => super.onInst(inst)
      }
    }

    override def onVal(value: Val): Val = value match {
      case Val.ClassOf(_) =>
        util.unsupported("Lowering ClassOf needs nir.Buffer")
      case Val.Global(ScopeRef(node), _) => rtti(node).const
      case Val.String(v)                 => genStringVal(v)
      case Val.Unit                      => unit
      case _                             => super.onVal(value)
    }

    def genVal(buf: Buffer, value: Val)(implicit pos: Position): Val =
      value match {
        case Val.ClassOf(ScopeRef(node)) => rtti(node).const
        case Val.Const(v)                => Val.Const(genVal(buf, v))
        case Val.StructValue(values) =>
          Val.StructValue(values.map(genVal(buf, _)))
        case Val.ArrayValue(ty, values) =>
          Val.ArrayValue(onType(ty), values.map(genVal(buf, _)))
        case _ => onVal(value)
      }

    def genNullPointerSlowPath(buf: Buffer)(implicit pos: Position): Unit = {
      nullPointerSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(
              throwNullPointerTy,
              throwNullPointerVal,
              Seq(Val.Null),
              unwind
            )
            buf.unreachable(Next.None)
          }
      }
    }

    def genDivisionByZeroSlowPath(buf: Buffer)(implicit pos: Position): Unit = {
      divisionByZeroSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(
              throwDivisionByZeroTy,
              throwDivisionByZeroVal,
              Seq(Val.Null),
              unwind
            )
            buf.unreachable(Next.None)
          }
      }
    }

    def genClassCastSlowPath(buf: Buffer)(implicit pos: Position): Unit = {
      classCastSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val obj = Val.Local(fresh(), Type.Ptr)
            val toty = Val.Local(fresh(), Type.Ptr)

            buf.label(slowPath, Seq(obj, toty))
            val fromty = buf.let(Op.Load(Type.Ptr, obj), unwind)
            buf.call(
              throwClassCastTy,
              throwClassCastVal,
              Seq(Val.Null, fromty, toty),
              unwind
            )
            buf.unreachable(Next.None)
          }
      }
    }

    def genUnreachableSlowPath(buf: Buffer)(implicit pos: Position): Unit = {
      unreachableSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(throwUndefinedTy, throwUndefinedVal, Seq(Val.Null), unwind)
            buf.unreachable(Next.None)
          }
      }
    }

    def genOutOfBoundsSlowPath(buf: Buffer)(implicit pos: Position): Unit = {
      outOfBoundsSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val idx = Val.Local(fresh(), Type.Int)

            buf.label(slowPath, Seq(idx))
            buf.call(
              throwOutOfBoundsTy,
              throwOutOfBoundsVal,
              Seq(Val.Null, idx),
              unwind
            )
            buf.unreachable(Next.None)
          }
      }
    }

    def genNoSuchMethodSlowPath(buf: Buffer)(implicit pos: Position): Unit = {
      noSuchMethodSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val sig = Val.Local(fresh(), Type.Ptr)

            buf.label(slowPath, Seq(sig))
            buf.call(
              throwNoSuchMethodTy,
              throwNoSuchMethodVal,
              Seq(Val.Null, sig),
              unwind
            )
            buf.unreachable(Next.None)
          }
      }
    }

    def genLet(buf: Buffer, n: Local, op: Op)(implicit pos: Position): Unit =
      op.resty match {
        case Type.Unit =>
          genOp(buf, fresh(), op)
          buf.let(n, Op.Copy(unit), unwind)
        case Type.Nothing =>
          genOp(buf, fresh(), op)
          genUnreachable(buf)
          buf.label(fresh(), Seq(Val.Local(n, op.resty)))
        case _ =>
          genOp(buf, n, op)
      }

    def genThrow(buf: Buffer, exc: Val)(implicit pos: Position) = {
      genGuardNotNull(buf, exc)
      genOp(buf, fresh(), Op.Call(throwSig, throw_, Seq(exc)))
      buf.unreachable(Next.None)
    }

    def genUnreachable(buf: Buffer)(implicit pos: Position) = {
      val failL = unreachableSlowPath.getOrElseUpdate(unwindHandler, fresh())

      buf.jump(Next(failL))
    }

    def genOp(buf: Buffer, n: Local, op: Op)(implicit pos: Position): Unit = {
      op match {
        case op: Op.Field =>
          genFieldOp(buf, n, op)
        case op: Op.Fieldload =>
          genFieldloadOp(buf, n, op)
        case op: Op.Fieldstore =>
          genFieldstoreOp(buf, n, op)
        case op: Op.Load =>
          genLoadOp(buf, n, op)
        case op: Op.Store =>
          genStoreOp(buf, n, op)
        case op: Op.Method =>
          genMethodOp(buf, n, op)
        case op: Op.Dynmethod =>
          genDynmethodOp(buf, n, op)
        case op: Op.Is =>
          genIsOp(buf, n, op)
        case op: Op.As =>
          genAsOp(buf, n, op)
        case op: Op.SizeOf      => genSizeOfOp(buf, n, op)
        case op: Op.AlignmentOf => genAlignmentOfOp(buf, n, op)
        case op: Op.Classalloc =>
          genClassallocOp(buf, n, op)
        case op: Op.Conv =>
          genConvOp(buf, n, op)
        case op: Op.Call =>
          genCallOp(buf, n, op)
        case op: Op.Comp =>
          genCompOp(buf, n, op)
        case op: Op.Bin =>
          genBinOp(buf, n, op)
        case op: Op.Box =>
          genBoxOp(buf, n, op)
        case op: Op.Unbox =>
          genUnboxOp(buf, n, op)
        case op: Op.Module =>
          genModuleOp(buf, n, op)
        case Op.Var(_) => () // Already emmited
        case Op.Varload(Val.Local(slot, Type.Var(ty))) =>
          buf.let(n, Op.Load(ty, Val.Local(slot, Type.Ptr)), unwind)
        case Op.Varstore(Val.Local(slot, Type.Var(ty)), value) =>
          buf.let(
            n,
            Op.Store(ty, Val.Local(slot, Type.Ptr), genVal(buf, value)),
            unwind
          )
        case op: Op.Arrayalloc =>
          genArrayallocOp(buf, n, op)
        case op: Op.Arrayload =>
          genArrayloadOp(buf, n, op)
        case op: Op.Arraystore =>
          genArraystoreOp(buf, n, op)
        case op: Op.Arraylength =>
          genArraylengthOp(buf, n, op)
        case op: Op.Stackalloc =>
          genStackallocOp(buf, n, op)
        case _ =>
          buf.let(n, op, unwind)
      }
    }

    def genGuardNotNull(buf: Buffer, obj: Val)(implicit pos: Position): Unit =
      obj.ty match {
        case ty: Type.RefKind if !ty.isNullable =>
          ()

        case _ if nullGuardedVals.add(obj) =>
          import buf._
          val v = genVal(buf, obj)

          val notNullL = fresh()
          val isNullL =
            nullPointerSlowPath.getOrElseUpdate(unwindHandler, fresh())

          val isNull = comp(Comp.Ine, v.ty, v, Val.Null, unwind)
          branch(isNull, Next(notNullL), Next(isNullL))
          label(notNullL)

        case _ => ()
      }

    def genGuardInBounds(buf: Buffer, idx: Val, len: Val)(implicit
        pos: Position
    ): Unit = {
      import buf._

      val inBoundsL = fresh()
      val outOfBoundsL =
        outOfBoundsSlowPath.getOrElseUpdate(unwindHandler, fresh())

      val gt0 = comp(Comp.Sge, Type.Int, idx, zero, unwind)
      val ltLen = comp(Comp.Slt, Type.Int, idx, len, unwind)
      val inBounds = bin(Bin.And, Type.Bool, gt0, ltLen, unwind)
      branch(inBounds, Next(inBoundsL), Next.Label(outOfBoundsL, Seq(idx)))
      label(inBoundsL)
    }

    def genFieldElemOp(buf: Buffer, obj: Val, name: Global)(implicit
        pos: Position
    ) = {
      import buf._
      val v = genVal(buf, obj)
      val FieldRef(cls: Class, fld) = name: @unchecked

      val layout = meta.layout(cls)
      val ty = layout.struct
      val index = layout.index(fld)

      genGuardNotNull(buf, v)
      elem(ty, v, Seq(zero, Val.Int(index)), unwind)
    }

    def genFieldloadOp(buf: Buffer, n: Local, op: Op.Fieldload)(implicit
        pos: Position
    ) = {
      val Op.Fieldload(ty, obj, name) = op
      val field = name match {
        case FieldRef(_, field) => field
        case _ =>
          throw new LinkingException(s"Metadata for field '$name' not found")
      }

      val isVolatile = field.attrs.isVolatile
      val syncAttrs = SyncAttrs(
        memoryOrder =
          if (isVolatile) MemoryOrder.SeqCst
          else if (field.attrs.isFinal) MemoryOrder.Monotonic
          else MemoryOrder.Unordered,
        isVolatile = isVolatile
      )

      val elem = genFieldElemOp(buf, genVal(buf, obj), name)
      genLoadOp(buf, n, Op.Load(ty, elem, Some(syncAttrs)))
    }

    def genFieldstoreOp(buf: Buffer, n: Local, op: Op.Fieldstore)(implicit
        pos: Position
    ) = {
      val Op.Fieldstore(ty, obj, name, value) = op
      val field = name match {
        case FieldRef(_, field) => field
        case _ =>
          throw new LinkingException(s"Metadata for field '$name' not found")
      }

      val isVolatile = field.attrs.isVolatile
      val syncAttrs = SyncAttrs(
        memoryOrder =
          if (isVolatile) MemoryOrder.SeqCst
          else if (field.attrs.isFinal) MemoryOrder.Monotonic
          else MemoryOrder.Unordered,
        isVolatile = isVolatile
      )
      val elem = genFieldElemOp(buf, genVal(buf, obj), name)
      genStoreOp(buf, n, Op.Store(ty, elem, value, Some(syncAttrs)))
    }

    def genFieldOp(buf: Buffer, n: Local, op: Op)(implicit
        pos: Position
    ) = {
      val Op.Field(obj, name) = op: @unchecked
      val elem = genFieldElemOp(buf, obj, name)
      buf.let(n, Op.Copy(elem), unwind)
    }

    def genLoadOp(buf: Buffer, n: Local, op: Op.Load)(implicit
        pos: Position
    ): Unit = {
      op match {
        // Convert synchronized load(bool) into load(byte)
        // LLVM is not providing synchronization on booleans
        case Op.Load(Type.Bool, ptr, syncAttrs @ Some(_)) =>
          val valueAsByte = fresh()
          val asPtr =
            if (platform.useOpaquePointers) ptr
            else {
              val asPtr = fresh()
              genConvOp(buf, asPtr, Op.Conv(Conv.Bitcast, Type.Ptr, ptr))
              Val.Local(asPtr, Type.Ptr)
            }
          genLoadOp(
            buf,
            valueAsByte,
            Op.Load(Type.Byte, asPtr, syncAttrs)
          )
          genConvOp(
            buf,
            n,
            Op.Conv(Conv.Trunc, Type.Bool, Val.Local(valueAsByte, Type.Byte))
          )

        case Op.Load(ty, ptr, syncAttrs) =>
          buf.let(
            n,
            Op.Load(ty, genVal(buf, ptr), syncAttrs),
            unwind
          )
      }
    }

    def genStoreOp(buf: Buffer, n: Local, op: Op.Store)(implicit
        pos: Position
    ): Unit = {
      op match {
        // Convert synchronized store(bool) into store(byte)
        // LLVM is not providing synchronization on booleans
        case Op.Store(Type.Bool, ptr, value, syncAttrs @ Some(_)) =>
          val valueAsByte = fresh()
          val asPtr =
            if (platform.useOpaquePointers) ptr
            else {
              val asPtr = fresh()
              genConvOp(buf, asPtr, Op.Conv(Conv.Bitcast, Type.Ptr, ptr))
              Val.Local(asPtr, Type.Ptr)
            }
          genConvOp(buf, valueAsByte, Op.Conv(Conv.Zext, Type.Byte, value))
          genStoreOp(
            buf,
            n,
            Op.Store(
              Type.Byte,
              asPtr,
              Val.Local(valueAsByte, Type.Byte),
              syncAttrs
            )
          )

        case Op.Store(ty, ptr, value, syncAttrs) =>
          buf.let(
            n,
            Op.Store(ty, genVal(buf, ptr), genVal(buf, value), syncAttrs),
            unwind
          )
      }
    }

    def genCompOp(buf: Buffer, n: Local, op: Op.Comp)(implicit
        pos: Position
    ): Unit = {
      val Op.Comp(comp, ty, l, r) = op
      val left = genVal(buf, l)
      val right = genVal(buf, r)
      buf.let(n, Op.Comp(comp, ty, left, right), unwind)
    }

    // Cached function
    private object shouldGenerateSafepoints {
      import scalanative.build.GC._
      private var lastDefn: Defn.Define = _
      private var lastResult: Boolean = false

      private val supportedGC = meta.config.gc match {
        case Immix => true
        case _     => false
      }
      private val multithreadingEnabled = meta.platform.isMultithreadingEnabled
      private val usesSafepoints = multithreadingEnabled && supportedGC

      def apply(defn: Defn.Define): Boolean = {
        if (!usesSafepoints) false
        else if (defn eq lastDefn) lastResult
        else {
          lastDefn = defn
          val defnNeedsSafepoints = defn.name match {
            case Global.Member(_, sig) =>
              // Exclude accessors and generated methods
              def mayContainLoops = defn.insts.exists(_.isInstanceOf[Inst.Jump])
              !sig.isGenerated && (defn.insts.size > 4 || mayContainLoops)
            case _ => false // unreachable or generated
          }
          lastResult = defnNeedsSafepoints
          lastResult
        }
      }
    }
    private def genGCSafepoint(buf: Buffer, genUnwind: Boolean = true)(implicit
        pos: Position
    ): Unit = {
      if (shouldGenerateSafepoints(currentDefn.get)) {
        val handler = {
          if (genUnwind && unwindHandler.isInitialized) unwind
          else Next.None
        }
        val syncAttrs = SyncAttrs(
          memoryOrder = MemoryOrder.Unordered,
          isVolatile = true
        )
        val safepointAddr = buf.load(Type.Ptr, GCSafepoint, handler)
        buf.load(Type.Ptr, safepointAddr, handler, Some(syncAttrs))
      }
    }

    def genCallOp(buf: Buffer, n: Local, op: Op.Call)(implicit
        pos: Position
    ): Unit = {
      val Op.Call(ty, ptr, args) = op
      def genCall() = {
        buf.let(
          n,
          Op.Call(
            ty = ty,
            ptr = genVal(buf, ptr),
            args = args.map(genVal(buf, _))
          ),
          unwind
        )
      }

      def switchThreadState(managed: Boolean) = buf.call(
        GCSetMutatorThreadStateSig,
        GCSetMutatorThreadState,
        Seq(Val.Int(if (managed) 0 else 1)),
        if (unwindHandler.isInitialized) unwind else Next.None
      )

      def shouldSwitchThreadState(name: Global) =
        platform.isMultithreadingEnabled && linked.infos.get(name).exists {
          info =>
            val attrs = info.attrs
            attrs.isExtern && attrs.isBlocking
        }

      ptr match {
        case Val.Global(global, _) if shouldSwitchThreadState(global) =>
          switchThreadState(managed = false)
          genCall()
          genGCSafepoint(buf, genUnwind = false)
          switchThreadState(managed = true)

        case _ => genCall()
      }
    }

    def genMethodOp(buf: Buffer, n: Local, op: Op.Method)(implicit
        pos: Position
    ) = {
      import buf._

      val Op.Method(v, sig) = op
      val obj = genVal(buf, v)

      def genClassVirtualLookup(cls: Class): Unit = {
        val vindex = vtable(cls).index(sig)
        assert(
          vindex != -1,
          s"The virtual table of ${cls.name} does not contain $sig"
        )

        val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
        val methptrptr = let(
          Op.Elem(
            rtti(cls).struct,
            typeptr,
            ClassRttiVtablePath :+ Val.Int(vindex)
          ),
          unwind
        )

        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      def genTraitVirtualLookup(trt: Trait): Unit = {
        val sigid = dispatchTable.traitSigIds(sig)
        val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
        val idptr =
          let(Op.Elem(Rtti.layout, typeptr, RttiTraitIdPath), unwind)
        val id = let(Op.Load(Type.Int, idptr), unwind)
        val rowptr = let(
          Op.Elem(
            Type.Ptr,
            dispatchTable.dispatchVal,
            Seq(Val.Int(dispatchTable.dispatchOffset(sigid)))
          ),
          unwind
        )
        val methptrptr =
          let(Op.Elem(Type.Ptr, rowptr, Seq(id)), unwind)
        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      def genMethodLookup(scope: ScopeInfo): Unit = {
        scope.targets(sig).toSeq match {
          case Seq() =>
            let(n, Op.Copy(Val.Null), unwind)
          case Seq(impl) =>
            let(n, Op.Copy(Val.Global(impl, Type.Ptr)), unwind)
          case _ =>
            obj.ty match {
              case ClassRef(cls) =>
                genClassVirtualLookup(cls)
              case TraitRef(_) if Object.calls.contains(sig) =>
                genClassVirtualLookup(Object)
              case TraitRef(trt) =>
                genTraitVirtualLookup(trt)
              case _ => util.unreachable
            }
        }
      }

      def genStaticMethod(cls: Class): Unit = {
        val method = cls
          .resolve(sig)
          .getOrElse {
            unsupported(
              s"Did not find the signature of method $sig in ${cls.name}"
            )
          }
        let(n, Op.Copy(Val.Global(method, Type.Ptr)), unwind)
      }

      def staticMethodIn(cls: Class): Boolean =
        !sig.isVirtual || !cls.calls.contains(sig)

      // We check type of original value, because it may change inside `genVal` transformation
      // Eg. Val.String is transformed to Const(StructValue) which changes type from Ref to Ptr
      v.ty match {
        // Method call with `null` ref argument might be inlined, in such case materialization of local value in Eval would
        // result with Val.Null. We're directly throwing NPE which normally would be handled in slow path of `genGuardNotNull`
        case Type.Null =>
          let(
            n,
            Op.Call(throwNullPointerTy, throwNullPointerVal, Seq(Val.Null)),
            unwind
          )
          buf.unreachable(Next.None)

        case ClassRef(cls) if staticMethodIn(cls) =>
          genStaticMethod(cls)

        case ScopeRef(scope) =>
          genGuardNotNull(buf, obj)
          genMethodLookup(scope)

        case _ => util.unreachable
      }
    }

    def genDynmethodOp(buf: Buffer, n: Local, op: Op.Dynmethod)(implicit
        pos: Position
    ): Unit = {
      import buf._

      val Op.Dynmethod(v, sig) = op
      val obj = genVal(buf, v)

      def throwIfNull(value: Val) = {
        val notNullL = fresh()
        val noSuchMethodL =
          noSuchMethodSlowPath.getOrElseUpdate(unwindHandler, fresh())

        val condNull = comp(Comp.Ine, Type.Ptr, value, Val.Null, unwind)
        branch(
          condNull,
          Next(notNullL),
          Next.Label(noSuchMethodL, Seq(Val.String(sig.mangle)))
        )
        label(notNullL)
      }

      def genReflectiveLookup(): Val = {
        val methodIndex =
          meta.linked.dynsigs.zipWithIndex.find(_._1 == sig).get._2

        // Load the type information pointer
        val typeptr = load(Type.Ptr, obj, unwind)
        // Load the dynamic hash map for given type, make sure it's not null
        val mapelem = elem(classRttiType, typeptr, ClassRttiDynmapPath, unwind)
        val mapptr = load(Type.Ptr, mapelem, unwind)
        // If hash map is not null, it has to contain at least one entry
        throwIfNull(mapptr)
        // Perform dynamic dispatch via dyndispatch helper
        val methptrptr = call(
          dyndispatchSig,
          dyndispatch,
          Seq(mapptr, Val.Int(methodIndex)),
          unwind
        )
        // Hash map lookup can still not contain given signature
        throwIfNull(methptrptr)
        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      genGuardNotNull(buf, obj)
      genReflectiveLookup()
    }

    def genIsOp(buf: Buffer, n: Local, op: Op.Is)(implicit
        pos: Position
    ): Unit = {
      import buf._

      op match {
        case Op.Is(_, Val.Null | Val.Zero(_)) =>
          let(n, Op.Copy(Val.False), unwind)

        case Op.Is(ty, v) =>
          val obj = genVal(buf, v)
          val isNullL, checkL, resultL = fresh()

          // check if obj is null
          val isNull = let(Op.Comp(Comp.Ieq, Type.Ptr, obj, Val.Null), unwind)
          branch(isNull, Next(isNullL), Next(checkL))

          // in case it's null, result is always false
          label(isNullL)
          jump(resultL, Seq(Val.False))

          // otherwise, do an actual instance check
          label(checkL)
          val isInstanceOf = genIsOp(buf, ty, obj)
          jump(resultL, Seq(isInstanceOf))

          // merge the result of two branches
          label(resultL, Seq(Val.Local(n, op.resty)))
      }
    }

    def genIsOp(buf: Buffer, ty: Type, v: Val)(implicit pos: Position): Val = {
      import buf._
      val obj = genVal(buf, v)

      ty match {
        case ClassRef(cls) if meta.ranges(cls).length == 1 =>
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          let(Op.Comp(Comp.Ieq, Type.Ptr, typeptr, rtti(cls).const), unwind)

        case ClassRef(cls) =>
          val range = meta.ranges(cls)
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          val idptr =
            let(
              Op.Elem(Rtti.layout, typeptr, RttiClassIdPath),
              unwind
            )
          val id = let(Op.Load(Type.Int, idptr), unwind)
          val ge =
            let(Op.Comp(Comp.Sle, Type.Int, Val.Int(range.start), id), unwind)
          val le =
            let(Op.Comp(Comp.Sle, Type.Int, id, Val.Int(range.end)), unwind)
          let(Op.Bin(Bin.And, Type.Bool, ge, le), unwind)

        case TraitRef(trt) =>
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          val idptr =
            let(
              Op.Elem(Rtti.layout, typeptr, RttiClassIdPath),
              unwind
            )
          val id = let(Op.Load(Type.Int, idptr), unwind)
          let(
            Op.Call(
              Generate.ClassHasTraitSig,
              Val.Global(
                Generate.ClassHasTraitName,
                Generate.ClassHasTraitSig
              ),
              Seq(id, Val.Int(meta.ids(trt)))
            ),
            unwind
          )
        case _ =>
          util.unsupported(s"is[$ty] $obj")
      }
    }

    def genAsOp(buf: Buffer, n: Local, op: Op.As)(implicit
        pos: Position
    ): Unit = {
      import buf._

      op match {
        case Op.As(ty: Type.RefKind, v) if v.ty == Type.Null =>
          let(n, Op.Copy(Val.Null), unwind)

        case Op.As(ty: Type.RefKind, obj)
            if obj.ty.isInstanceOf[Type.RefKind] =>
          val v = genVal(buf, obj)
          val checkIfIsInstanceOfL, castL = fresh()
          val failL = classCastSlowPath.getOrElseUpdate(unwindHandler, fresh())

          val isNull = comp(Comp.Ieq, v.ty, v, Val.Null, unwind)
          branch(isNull, Next(castL), Next(checkIfIsInstanceOfL))

          label(checkIfIsInstanceOfL)
          val isInstanceOf = genIsOp(buf, ty, v)
          val toTy = rtti(linked.infos(ty.className)).const
          branch(isInstanceOf, Next(castL), Next.Label(failL, Seq(v, toTy)))

          label(castL)
          if (platform.useOpaquePointers)
            let(n, Op.Copy(v), unwind)
          else
            let(n, Op.Conv(Conv.Bitcast, ty, v), unwind)

        case Op.As(to, v) =>
          util.unsupported(s"can't cast from ${v.ty} to $to")
      }
    }

    def genSizeOfOp(buf: Buffer, n: Local, op: Op.SizeOf)(implicit
        pos: Position
    ): Unit = {
      val size = op.ty match {
        case ClassRef(cls) if op.ty != Type.Unit =>
          if (!cls.allocated) {
            val Global.Top(clsName) = cls.name: @unchecked
            logger.warn(
              s"Referencing size of non allocated type ${clsName} in ${pos.show}"
            )
          }
          meta.layout(cls).size
        case _ => MemoryLayout.sizeOf(op.ty)
      }
      buf.let(n, Op.Copy(Val.Size(size)), unwind)
    }

    def genAlignmentOfOp(buf: Buffer, n: Local, op: Op.AlignmentOf)(implicit
        pos: Position
    ): Unit = {
      val alignment = MemoryLayout.alignmentOf(op.ty)
      buf.let(n, Op.Copy(Val.Size(alignment)), unwind)
    }

    def genClassallocOp(buf: Buffer, n: Local, op: Op.Classalloc)(implicit
        pos: Position
    ): Unit = {
      val Op.Classalloc(ClassRef(cls), v) = op: @unchecked
      val zone = v.map(genVal(buf, _))

      val size = meta.layout(cls).size
      assert(size == size.toInt)

      zone match {
        case Some(zone) =>
          val safeZoneAllocImplMethod = Val.Local(fresh(), Type.Ptr)
          genMethodOp(
            buf,
            safeZoneAllocImplMethod.name,
            Op.Method(zone, safeZoneAllocImpl.sig)
          )
          buf.let(
            n,
            Op.Call(
              safeZoneAllocImplSig,
              safeZoneAllocImplMethod,
              Seq(zone, rtti(cls).const, Val.Size(size.toInt))
            ),
            unwind
          )
        case None =>
          val allocMethod =
            if (size < LARGE_OBJECT_MIN_SIZE) alloc else largeAlloc
          buf.let(
            n,
            Op.Call(
              allocSig,
              allocMethod,
              Seq(rtti(cls).const, Val.Size(size.toInt))
            ),
            unwind
          )
      }
    }

    def genConvOp(buf: Buffer, n: Local, op: Op.Conv)(implicit
        pos: Position
    ): Unit = {
      import buf._

      op match {
        // Fptosi is undefined behaviour on LLVM if the resulting
        // value doesn't fit the MIN...MAX range for given integer type.
        // We insert range checks and return MIN_VALUE for floating values
        // that are numerically less than or equal to MIN_VALUE and MAX_VALUE
        // for the ones which are greate or equal to MAX_VALUE. Additionally,
        // NaNs are converted to 0.
        case Op.Conv(Conv.Fptosi, toty, value) =>
          val v = genVal(buf, value)
          val (imin, imax, fmin, fmax) = toty match {
            case Type.Int =>
              val min = java.lang.Integer.MIN_VALUE
              val max = java.lang.Integer.MAX_VALUE
              v.ty match {
                case Type.Float =>
                  (
                    Val.Int(min),
                    Val.Int(max),
                    Val.Float(min.toFloat),
                    Val.Float(max.toFloat)
                  )
                case Type.Double =>
                  (
                    Val.Int(min),
                    Val.Int(max),
                    Val.Double(min.toDouble),
                    Val.Double(max.toDouble)
                  )
                case _ =>
                  util.unreachable
              }
            case Type.Long =>
              val min = java.lang.Long.MIN_VALUE
              val max = java.lang.Long.MAX_VALUE
              v.ty match {
                case Type.Float =>
                  (
                    Val.Long(min),
                    Val.Long(max),
                    Val.Float(min.toFloat),
                    Val.Float(max.toFloat)
                  )
                case Type.Double =>
                  (
                    Val.Long(min),
                    Val.Long(max),
                    Val.Double(min.toDouble),
                    Val.Double(max.toDouble)
                  )
                case _ =>
                  util.unreachable
              }
            case _ =>
              util.unreachable
          }

          val isNaNL, checkLessThanMinL, lessThanMinL, checkLargerThanMaxL,
              largerThanMaxL, inBoundsL, resultL = fresh()

          val isNaN = comp(Comp.Fne, v.ty, v, v, unwind)
          branch(isNaN, Next(isNaNL), Next(checkLessThanMinL))

          label(isNaNL)
          jump(resultL, Seq(Val.Zero(op.resty)))

          label(checkLessThanMinL)
          val isLessThanMin = comp(Comp.Fle, v.ty, v, fmin, unwind)
          branch(isLessThanMin, Next(lessThanMinL), Next(checkLargerThanMaxL))

          label(lessThanMinL)
          jump(resultL, Seq(imin))

          label(checkLargerThanMaxL)
          val isLargerThanMax = comp(Comp.Fge, v.ty, v, fmax, unwind)
          branch(isLargerThanMax, Next(largerThanMaxL), Next(inBoundsL))

          label(largerThanMaxL)
          jump(resultL, Seq(imax))

          label(inBoundsL)
          val inBoundsResult = let(op, unwind)
          jump(resultL, Seq(inBoundsResult))

          label(resultL, Seq(Val.Local(n, op.resty)))

        case Op.Conv(conv, ty, value) =>
          let(n, Op.Conv(conv, ty, genVal(buf, value)), unwind)
      }
    }

    def genBinOp(buf: Buffer, n: Local, op: Op.Bin)(implicit
        pos: Position
    ): Unit = {
      import buf._

      // LLVM's division by zero is undefined behaviour. We guard
      // the case when the divisor is zero and fail gracefully
      // by throwing an arithmetic exception.
      def checkDivisionByZero(op: Op.Bin): Unit = {
        val Op.Bin(bin, ty: Type.I, dividend, divisor) = op: @unchecked

        val thenL, elseL = fresh()

        val succL = fresh()
        val failL =
          divisionByZeroSlowPath.getOrElseUpdate(unwindHandler, fresh())

        val isZero =
          comp(Comp.Ine, ty, divisor, Val.Zero(ty), unwind)
        branch(isZero, Next(succL), Next(failL))

        label(succL)
        if (bin == Bin.Srem || bin == Bin.Sdiv) {
          checkDivisionOverflow(op)
        } else {
          let(n, op, unwind)
        }
      }

      // Detects taking remainder for division by -1 and replaces
      // it by division by 1 which can't overflow.
      //
      // We implement '%' (remainder) with LLVM's 'srem' and it
      // can overflow for cases:
      //
      // - Int.MinValue % -1
      // - Long.MinValue % -1
      //
      // E.g. On x86_64 'srem' might get translated to 'idiv'
      // which computes both quotient and remainder at once
      // and quotient can overflow.
      def checkDivisionOverflow(op: Op.Bin): Unit = {
        val Op.Bin(bin, ty: Type.I, dividend, divisor) = op: @unchecked

        val mayOverflowL, noOverflowL, didOverflowL, resultL = fresh()

        val minus1 = ty match {
          case Type.Int  => Val.Int(-1)
          case Type.Long => Val.Long(-1L)
          case Type.Size => Val.Size(-1L)
          case _         => util.unreachable
        }
        val minValue = ty match {
          case Type.Int  => Val.Int(java.lang.Integer.MIN_VALUE)
          case Type.Long => Val.Long(java.lang.Long.MIN_VALUE)
          case Type.Size =>
            if (platform.is32Bit) Val.Size(java.lang.Integer.MIN_VALUE)
            else Val.Size(java.lang.Long.MIN_VALUE)
          case _ => util.unreachable
        }

        val divisorIsMinus1 =
          let(Op.Comp(Comp.Ieq, ty, divisor, minus1), unwind)
        branch(divisorIsMinus1, Next(mayOverflowL), Next(noOverflowL))

        label(mayOverflowL)
        val dividendIsMinValue =
          let(Op.Comp(Comp.Ieq, ty, dividend, minValue), unwind)
        branch(dividendIsMinValue, Next(didOverflowL), Next(noOverflowL))

        label(didOverflowL)
        val overflowResult = bin match {
          case Bin.Srem => Val.Zero(ty)
          case Bin.Sdiv => minValue
          case _        => util.unreachable
        }
        jump(resultL, Seq(overflowResult))

        label(noOverflowL)
        val noOverflowResult = let(op, unwind)
        jump(resultL, Seq(noOverflowResult))

        label(resultL, Seq(Val.Local(n, ty)))
      }

      // Shifts are undefined if the bits shifted by are >= bits in the type.
      // We mask the right hand side with bits in type - 1 to make it defined.
      def maskShift(op: Op.Bin) = {
        val Op.Bin(_, ty: Type.I, _, r) = op: @unchecked
        val mask = ty match {
          case Type.Int  => Val.Int(31)
          case Type.Long => Val.Int(63)
          case _         => util.unreachable
        }
        val masked = bin(Bin.And, ty, r, mask, unwind)
        let(n, op.copy(r = masked), unwind)
      }

      op match {
        case op @ Op.Bin(
              bin @ (Bin.Srem | Bin.Urem | Bin.Sdiv | Bin.Udiv),
              ty: Type.I,
              l,
              r
            ) =>
          checkDivisionByZero(op)

        case op @ Op.Bin(
              bin @ (Bin.Shl | Bin.Lshr | Bin.Ashr),
              ty: Type.I,
              l,
              r
            ) =>
          maskShift(op)

        case op =>
          let(n, op, unwind)
      }
    }

    def genBoxOp(buf: Buffer, n: Local, op: Op.Box)(implicit
        pos: Position
    ): Unit = {
      val Op.Box(ty, v) = op
      val from = genVal(buf, v)

      val methodName = BoxTo(ty)
      val moduleName = methodName.top

      val boxTy =
        Type.Function(Seq(Type.Ref(moduleName), Type.unbox(ty)), ty)

      buf.let(
        n,
        Op.Call(boxTy, Val.Global(methodName, Type.Ptr), Seq(Val.Null, from)),
        unwind
      )
    }

    def genUnboxOp(buf: Buffer, n: Local, op: Op.Unbox)(implicit
        pos: Position
    ): Unit = {
      val Op.Unbox(ty, v) = op
      val from = genVal(buf, v)

      val methodName = UnboxTo(ty)
      val moduleName = methodName.top

      val unboxTy =
        Type.Function(Seq(Type.Ref(moduleName), ty), Type.unbox(ty))

      buf.let(
        n,
        Op.Call(unboxTy, Val.Global(methodName, Type.Ptr), Seq(Val.Null, from)),
        unwind
      )
    }

    def genModuleOp(buf: Buffer, n: Local, op: Op.Module)(implicit
        pos: Position
    ) = {
      val Op.Module(name) = op

      meta.linked.infos(name) match {
        case cls: Class if cls.isConstantModule =>
          val instance = name.member(Sig.Generated("instance"))
          buf.let(n, Op.Copy(Val.Global(instance, Type.Ptr)), unwind)

        case _ =>
          val loadSig = Type.Function(Seq.empty, Type.Ref(name))
          val load = Val.Global(name.member(Sig.Generated("load")), Type.Ptr)

          buf.let(n, Op.Call(loadSig, load, Seq.empty), unwind)
      }
    }

    def genArrayallocOp(buf: Buffer, n: Local, op: Op.Arrayalloc)(implicit
        pos: Position
    ): Unit = {
      val Op.Arrayalloc(ty, v1, v2) = op
      val init = genVal(buf, v1)
      val zone = v2.map(genVal(buf, _))
      init match {
        case len if len.ty == Type.Int =>
          val (arrayAlloc, arrayAllocSig) = zone match {
            case Some(_) => (arrayZoneAlloc, arrayZoneAllocSig)
            case None    => (arrayHeapAlloc, arrayHeapAllocSig)
          }
          val sig = arrayAllocSig.getOrElse(ty, arrayAllocSig(Rt.Object))
          val func = arrayAlloc.getOrElse(ty, arrayAlloc(Rt.Object))
          val module = genModuleOp(buf, fresh(), Op.Module(func.owner))
          buf.let(
            n,
            Op.Call(
              sig,
              Val.Global(func, Type.Ptr),
              zone match {
                case Some(zone) => Seq(module, len, zone)
                case None       => Seq(module, len)
              }
            ),
            unwind
          )
        case arrval: Val.ArrayValue =>
          val sig = arraySnapshotSig.getOrElse(ty, arraySnapshotSig(Rt.Object))
          val func = arraySnapshot.getOrElse(ty, arraySnapshot(Rt.Object))
          val module = genModuleOp(buf, fresh(), Op.Module(func.owner))
          val len = Val.Int(arrval.values.length)
          val init = Val.Const(arrval)
          buf.let(
            n,
            Op.Call(sig, Val.Global(func, Type.Ptr), Seq(module, len, init)),
            unwind
          )
        case _ => util.unreachable
      }
    }

    private def arrayMemoryLayout(
        ty: nir.Type,
        length: Int = 0
    ): Type.StructValue = Type.StructValue(
      Seq(ArrayHeader.layout, Type.ArrayValue(ty, length))
    )
    private def arrayValuePath(idx: Val) = Seq(zero, one, idx)

    def genArrayloadOp(buf: Buffer, n: Local, op: Op.Arrayload)(implicit
        pos: Position
    ): Unit = {
      val Op.Arrayload(ty, v, idx) = op
      val arr = genVal(buf, v)

      val len = fresh()

      genArraylengthOp(buf, len, Op.Arraylength(arr))
      genGuardInBounds(buf, idx, Val.Local(len, Type.Int))

      val arrTy = arrayMemoryLayout(ty)
      val elemPtr = buf.elem(arrTy, arr, arrayValuePath(idx), unwind)
      buf.let(n, Op.Load(ty, elemPtr), unwind)
    }

    def genArraystoreOp(buf: Buffer, n: Local, op: Op.Arraystore)(implicit
        pos: Position
    ): Unit = {
      val Op.Arraystore(ty, arr, idx, v) = op
      val len = fresh()
      val value = genVal(buf, v)

      genArraylengthOp(buf, len, Op.Arraylength(arr))
      genGuardInBounds(buf, idx, Val.Local(len, Type.Int))

      val arrTy = arrayMemoryLayout(ty)
      val elemPtr = buf.elem(arrTy, arr, arrayValuePath(idx), unwind)
      genStoreOp(buf, n, Op.Store(ty, elemPtr, value))
    }

    def genArraylengthOp(buf: Buffer, n: Local, op: Op.Arraylength)(implicit
        pos: Position
    ): Unit = {
      val Op.Arraylength(v) = op
      val arr = genVal(buf, v)

      val sig = arrayLengthSig
      val func = arrayLength

      genGuardNotNull(buf, arr)
      val lenPtr =
        buf.elem(ArrayHeader.layout, arr, ArrayHeaderLengthPath, unwind)
      buf.let(n, Op.Load(Type.Int, lenPtr), unwind)
    }

    def genStackallocOp(buf: Buffer, n: Local, op: Op.Stackalloc)(implicit
        pos: Position
    ): Unit = {
      val Op.Stackalloc(ty, size) = op
      val initValue = Val.Zero(ty).canonicalize
      val pointee = buf.let(n, op, unwind)
      size match {
        case Val.Size(1) if initValue.isCanonical =>
          buf.let(
            Op.Store(ty, pointee, initValue, None),
            unwind
          )
        case sizeV =>
          val elemSize = MemoryLayout.sizeOf(ty)
          val size = sizeV match {
            case Val.Size(v) => Val.Size(v * elemSize)
            case _ =>
              val asSize = sizeV.ty match {
                case Type.FixedSizeI(width, _) =>
                  if (width == platform.sizeOfPtrBits) sizeV
                  else if (width > platform.sizeOfPtrBits)
                    buf.conv(Conv.Trunc, Type.Size, sizeV, unwind)
                  else
                    buf.conv(Conv.Zext, Type.Size, sizeV, unwind)

                case _ => sizeV
              }
              if (elemSize == 1) asSize
              else
                buf.let(
                  Op.Bin(Bin.Imul, Type.Size, asSize, Val.Size(elemSize)),
                  unwind
                )
          }
          buf.call(memsetSig, memset, Seq(pointee, Val.Int(0), size), unwind)
      }
    }

    def genStringVal(value: String): Val = {
      val StringCls = ClassRef.unapply(Rt.StringName).get
      val CharArrayCls = ClassRef.unapply(CharArrayName).get

      val chars = value.toCharArray
      val charsLength = Val.Int(chars.length)
      val charsConst = Val.Const(
        Val.StructValue(
          rtti(CharArrayCls).const ::
            meta.lockWordVals :::
            charsLength ::
            Val.Int(2) :: // stride is used only by GC
            Val.ArrayValue(Type.Char, chars.toSeq.map(Val.Char(_))) :: Nil
        )
      )

      val fieldValues = stringFieldNames.map {
        case Rt.StringValueName          => charsConst
        case Rt.StringOffsetName         => zero
        case Rt.StringCountName          => charsLength
        case Rt.StringCachedHashCodeName => Val.Int(stringHashCode(value))
        case _                           => util.unreachable
      }

      Val.Const(
        Val.StructValue(
          rtti(StringCls).const ::
            meta.lockWordVals ++
            fieldValues
        )
      )
    }

    private def genThisValueNullGuardIfUsed(
        defn: Defn.Define,
        buf: nir.Buffer,
        createUnwindHandler: () => Option[Local]
    ) = {
      def usesValue(expected: Val): Boolean = {
        var wasUsed = false
        import scala.util.control.Breaks._
        breakable {
          new Traverse {
            override def onVal(value: Val): Unit = {
              wasUsed = expected eq value
              if (wasUsed) break()
              else super.onVal(value)
            }
            // We're not intrested in cheecking these structures, skip them
            override def onType(ty: Type): Unit = ()
            override def onNext(next: Next): Unit = ()
          }.onDefn(defn)
        }
        wasUsed
      }

      val Global.Member(_, sig) = defn.name: @unchecked
      val Inst.Label(_, args) = defn.insts.head: @unchecked

      val canHaveThisValue =
        !(sig.isStatic || sig.isClinit || sig.isExtern)

      if (canHaveThisValue) {
        args.headOption.foreach { thisValue =>
          thisValue.ty match {
            case ref: Type.Ref if ref.isNullable && usesValue(thisValue) =>
              implicit def pos: Position = defn.pos
              ScopedVar.scoped(
                unwindHandler := createUnwindHandler()
              ) {
                genGuardNotNull(buf, thisValue)
              }
            case _ => ()
          }
        }
      }
    }
  }

  // Update java.lang.String::hashCode whenever you change this method.
  def stringHashCode(s: String): Int =
    if (s.length == 0) {
      0
    } else {
      val value = s.toCharArray
      var hash = 0
      var i = 0
      while (i < value.length) {
        hash = value(i) + ((hash << 5) - hash)
        i += 1
      }
      hash
    }

  val LARGE_OBJECT_MIN_SIZE = 8192

  val allocSig = Type.Function(Seq(Type.Ptr, Type.Size), Type.Ptr)

  val allocSmallName = extern("scalanative_alloc_small")
  val alloc = Val.Global(allocSmallName, allocSig)

  val largeAllocName = extern("scalanative_alloc_large")
  val largeAlloc = Val.Global(largeAllocName, allocSig)

  val SafeZone = Type.Ref(Global.Top("scala.scalanative.memory.SafeZone"))
  val safeZoneAllocImplSig =
    Type.Function(Seq(SafeZone, Type.Ptr, Type.Size), Type.Ptr)
  val safeZoneAllocImpl = SafeZone.name.member(
    Sig.Method("allocImpl", Seq(Type.Ptr, Type.Size, Type.Ptr))
  )

  val dyndispatchName = extern("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.Int), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    Global.Member(excptnGlobal, Sig.Ctor(Seq(nir.Rt.String)))

  val excInitSig = Type.Function(
    Seq(Type.Ref(excptnGlobal), Type.Ref(Global.Top("java.lang.String"))),
    Type.Unit
  )
  val excInit = Val.Global(excptnInitGlobal, Type.Ptr)

  val CharArrayName =
    Global.Top("scala.scalanative.runtime.CharArray")

  val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  val RuntimeBoxes = Global.Top("scala.scalanative.runtime.Boxes$")

  val BoxTo: Map[Type, Global] = Type.boxClasses.map { cls =>
    val name = cls.asInstanceOf[Global.Top].id
    val boxty = Type.Ref(Global.Top(name))
    val module = if (name.startsWith("java.")) BoxesRunTime else RuntimeBoxes
    val id = "boxTo" + name.split("\\.").last
    val tys = Seq(nir.Type.unbox(boxty), boxty)
    val meth = module.member(Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  val UnboxTo: Map[Type, Global] = Type.boxClasses.map { cls =>
    val name = cls.asInstanceOf[Global.Top].id
    val boxty = Type.Ref(Global.Top(name))
    val module = if (name.startsWith("java.")) BoxesRunTime else RuntimeBoxes
    val id = {
      val last = name.split("\\.").last
      val suffix =
        if (last == "Integer") "Int"
        else if (last == "Character") "Char"
        else last
      "unboxTo" + suffix
    }
    val tys = Seq(nir.Rt.Object, nir.Type.unbox(boxty))
    val meth = module.member(Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  private def extern(id: String): Global =
    Global.Member(Global.Top("__"), Sig.Extern(id))

  val unitName = Global.Top("scala.scalanative.runtime.BoxedUnit$")
  val unitInstance = unitName.member(Sig.Generated("instance"))
  val unit = Val.Global(unitInstance, Type.Ptr)

  val throwName = extern("scalanative_throw")
  val throwSig = Type.Function(Seq(Type.Ptr), Type.Nothing)
  val throw_ = Val.Global(throwName, Type.Ptr)

  val arrayHeapAlloc = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname: @unchecked
      val arrcls = Type.Ref(arrname)
      ty -> Global.Member(
        Global.Top(id + "$"),
        Sig.Method("alloc", Seq(Type.Int, arrcls))
      )
  }.toMap
  val arrayHeapAllocSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname: @unchecked
      ty -> Type.Function(
        Seq(Type.Ref(Global.Top(id + "$")), Type.Int),
        Type.Ref(arrname)
      )
  }.toMap
  val arrayZoneAlloc = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname: @unchecked
      val arrcls = Type.Ref(arrname)
      ty -> Global.Member(
        Global.Top(id + "$"),
        Sig.Method("alloc", Seq(Type.Int, SafeZone, arrcls))
      )
  }.toMap
  val arrayZoneAllocSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname: @unchecked
      ty -> Type.Function(
        Seq(Type.Ref(Global.Top(id + "$")), Type.Int, SafeZone),
        Type.Ref(arrname)
      )
  }.toMap
  val arraySnapshot = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname: @unchecked
      val arrcls = Type.Ref(arrname)
      ty -> Global.Member(
        Global.Top(id + "$"),
        Sig.Method("snapshot", Seq(Type.Int, Type.Ptr, arrcls))
      )
  }.toMap
  val arraySnapshotSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname: @unchecked
      ty -> Type.Function(
        Seq(Type.Ref(Global.Top(id + "$")), Type.Int, Type.Ptr),
        Type.Ref(arrname)
      )
  }.toMap
  val arrayApplyGeneric = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(
        arrname,
        Sig.Method("apply", Seq(Type.Int, nir.Rt.Object))
      )
  }
  val arrayApply = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(arrname, Sig.Method("apply", Seq(Type.Int, ty)))
  }.toMap
  val arrayApplySig = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Type.Function(Seq(Type.Ref(arrname), Type.Int), ty)
  }.toMap
  val arrayUpdateGeneric = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(
        arrname,
        Sig.Method("update", Seq(Type.Int, nir.Rt.Object, Type.Unit))
      )
  }
  val arrayUpdate = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(
        arrname,
        Sig.Method("update", Seq(Type.Int, ty, Type.Unit))
      )
  }.toMap
  val arrayUpdateSig = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Type.Function(Seq(Type.Ref(arrname), Type.Int, ty), Type.Unit)
  }.toMap
  val arrayLength =
    Global.Member(
      Global.Top("scala.scalanative.runtime.Array"),
      Sig.Method("length", Seq(Type.Int))
    )
  val arrayLengthSig =
    Type.Function(
      Seq(Type.Ref(Global.Top("scala.scalanative.runtime.Array"))),
      Type.Int
    )

  val throwDivisionByZeroTy =
    Type.Function(Seq(Rt.Runtime), Type.Nothing)
  val throwDivisionByZero =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwDivisionByZero", Seq(Type.Nothing))
    )
  val throwDivisionByZeroVal =
    Val.Global(throwDivisionByZero, Type.Ptr)

  val throwClassCastTy =
    Type.Function(Seq(Rt.Runtime, Type.Ptr, Type.Ptr), Type.Nothing)
  val throwClassCast =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwClassCast", Seq(Type.Ptr, Type.Ptr, Type.Nothing))
    )
  val throwClassCastVal =
    Val.Global(throwClassCast, Type.Ptr)

  val throwNullPointerTy =
    Type.Function(Seq(Rt.Runtime), Type.Nothing)
  val throwNullPointer =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwNullPointer", Seq(Type.Nothing))
    )
  val throwNullPointerVal =
    Val.Global(throwNullPointer, Type.Ptr)

  val throwUndefinedTy =
    Type.Function(Seq(Type.Ptr), Type.Nothing)
  val throwUndefined =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwUndefined", Seq(Type.Nothing))
    )
  val throwUndefinedVal =
    Val.Global(throwUndefined, Type.Ptr)

  val throwOutOfBoundsTy =
    Type.Function(Seq(Type.Ptr, Type.Int), Type.Nothing)
  val throwOutOfBounds =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwOutOfBounds", Seq(Type.Int, Type.Nothing))
    )
  val throwOutOfBoundsVal =
    Val.Global(throwOutOfBounds, Type.Ptr)

  val throwNoSuchMethodTy =
    Type.Function(Seq(Type.Ptr, Type.Ptr), Type.Nothing)
  val throwNoSuchMethod =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwNoSuchMethod", Seq(Rt.String, Type.Nothing))
    )
  val throwNoSuchMethodVal =
    Val.Global(throwNoSuchMethod, Type.Ptr)

  val GC = Global.Top("scala.scalanative.runtime.GC$")
  val GCSafepointName = GC.member(Sig.Extern("scalanative_gc_safepoint"))
  val GCSafepoint = Val.Global(GCSafepointName, Type.Ptr)

  val GCSetMutatorThreadStateSig = Type.Function(Seq(Type.Int), Type.Unit)
  val GCSetMutatorThreadState = Val.Global(
    GC.member(Sig.Extern("scalanative_gc_set_mutator_thread_state")),
    Type.Ptr
  )

  val memsetSig =
    Type.Function(Seq(Type.Ptr, Type.Int, Type.Size), Type.Ptr)
  val memset = Val.Global(extern("memset"), Type.Ptr)

  val RuntimeNull = Type.Ref(Global.Top("scala.runtime.Null$"))
  val RuntimeNothing = Type.Ref(Global.Top("scala.runtime.Nothing$"))

  val injects: Seq[Defn] = {
    implicit val pos = Position.NoPosition
    val buf = mutable.UnrolledBuffer.empty[Defn]
    buf += Defn.Declare(Attrs.None, allocSmallName, allocSig)
    buf += Defn.Declare(Attrs.None, largeAllocName, allocSig)
    buf += Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig)
    buf += Defn.Declare(Attrs.None, throwName, throwSig)
    buf += Defn.Declare(Attrs(isExtern = true), memset.name, memsetSig)
    buf.toSeq
  }

  def depends(implicit platform: PlatformInfo): Seq[Global] = {
    val buf = mutable.UnrolledBuffer.empty[Global]
    buf += Rt.ClassName
    buf += Rt.ClassIdName
    buf += Rt.ClassTraitIdName
    buf += Rt.ClassNameName
    buf += Rt.ClassSizeName
    buf += Rt.ClassIdRangeUntilName
    buf += Rt.StringName
    buf += Rt.StringValueName
    buf += Rt.StringOffsetName
    buf += Rt.StringCountName
    buf += Rt.StringCachedHashCodeName
    buf += CharArrayName
    buf += BoxesRunTime
    buf += RuntimeBoxes
    buf += unitName
    buf ++= BoxTo.values
    buf ++= UnboxTo.values
    buf += arrayLength
    buf ++= arrayHeapAlloc.values
    buf ++= arrayZoneAlloc.values
    buf ++= arraySnapshot.values
    buf ++= arrayApplyGeneric.values
    buf ++= arrayApply.values
    buf ++= arrayUpdateGeneric.values
    buf ++= arrayUpdate.values
    buf += throwDivisionByZero
    buf += throwClassCast
    buf += throwNullPointer
    buf += throwUndefined
    buf += throwOutOfBounds
    buf += throwNoSuchMethod
    buf += RuntimeNull.name
    buf += RuntimeNothing.name
    if (platform.isMultithreadingEnabled) {
      buf += GCSafepoint.name
      buf += GCSetMutatorThreadState.name
    }
    buf.toSeq
  }
}
