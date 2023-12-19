package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.util.{ScopedVar, unsupported}
import scalanative.linker._
import scalanative.interflow.UseDef.eliminateDeadCode

object Lower {

  def apply(
      defns: Seq[nir.Defn]
  )(implicit meta: Metadata, logger: build.Logger): Seq[nir.Defn] =
    (new Impl).onDefns(defns)

  private final class Impl(implicit meta: Metadata, logger: build.Logger)
      extends nir.Transform {
    import meta._
    import meta.config
    import meta.layouts.{Rtti, ClassRtti, ArrayHeader}

    implicit val analysis: ReachabilityAnalysis.Result = meta.analysis

    val Object = analysis.infos(nir.Rt.Object.name).asInstanceOf[Class]

    private val zero = nir.Val.Int(0)
    private val one = nir.Val.Int(1)
    val RttiClassIdPath = Seq(zero, nir.Val.Int(Rtti.ClassIdIdx))
    val RttiTraitIdPath = Seq(zero, nir.Val.Int(Rtti.TraitIdIdx))
    val ClassRttiDynmapPath = Seq(zero, nir.Val.Int(ClassRtti.DynmapIdx))
    val ClassRttiVtablePath = Seq(zero, nir.Val.Int(ClassRtti.VtableIdx))
    val ArrayHeaderLengthPath = Seq(zero, nir.Val.Int(ArrayHeader.LengthIdx))

    // Type of the bare runtime type information struct.
    private val classRttiType =
      rtti(analysis.infos(nir.Global.Top("java.lang.Object"))).struct

    // Names of the fields of the java.lang.String in the memory layout order.
    private val stringFieldNames = {
      val node = ClassRef.unapply(nir.Rt.StringName).get
      val names = layout(node).entries.map(_.name)
      assert(names.length == 4, "java.lang.String is expected to have 4 fields")
      names
    }

    private val fresh = new util.ScopedVar[nir.Fresh]
    private val unwindHandler = new util.ScopedVar[Option[nir.Local]]
    private val currentDefn = new util.ScopedVar[nir.Defn.Define]
    private val nullGuardedVals = mutable.Set.empty[nir.Val]
    private def currentDefnRetType = {
      val nir.Type.Function(_, ret) = currentDefn.get.ty
      ret
    }

    private val unreachableSlowPath =
      mutable.Map.empty[Option[nir.Local], nir.Local]
    private val nullPointerSlowPath =
      mutable.Map.empty[Option[nir.Local], nir.Local]
    private val divisionByZeroSlowPath =
      mutable.Map.empty[Option[nir.Local], nir.Local]
    private val classCastSlowPath =
      mutable.Map.empty[Option[nir.Local], nir.Local]
    private val outOfBoundsSlowPath =
      mutable.Map.empty[Option[nir.Local], nir.Local]
    private val noSuchMethodSlowPath =
      mutable.Map.empty[Option[nir.Local], nir.Local]

    private def unwind: nir.Next =
      unwindHandler.get.fold[nir.Next](nir.Next.None) { handler =>
        val exc = nir.Val.Local(fresh(), nir.Rt.Object)
        nir.Next.Unwind(exc, nir.Next.Label(handler, Seq(exc)))
      }

    override def onDefns(defns: Seq[nir.Defn]): Seq[nir.Defn] = {
      val buf = mutable.UnrolledBuffer.empty[nir.Defn]

      defns.foreach {
        case _: nir.Defn.Class | _: nir.Defn.Module | _: nir.Defn.Trait =>
          ()
        case nir.Defn.Declare(attrs, MethodRef(_: Class | _: Trait, _), _)
            if !attrs.isExtern =>
          ()
        case nir.Defn.Var(attrs, FieldRef(_: Class, _), _, _)
            if !attrs.isExtern =>
          ()
        case defn =>
          buf += onDefn(defn)
      }

      buf.toSeq
    }

    override def onDefn(defn: nir.Defn): nir.Defn = defn match {
      case defn: nir.Defn.Define =>
        val nir.Type.Function(_, ty) = defn.ty
        ScopedVar.scoped(
          fresh := nir.Fresh(defn.insts),
          currentDefn := defn
        ) {
          try super.onDefn(defn)
          finally nullGuardedVals.clear()
        }
      case _ =>
        super.onDefn(defn)
    }

    override def onType(ty: nir.Type): nir.Type = ty

    def genNext(buf: nir.Buffer, next: nir.Next)(implicit
        pos: nir.Position
    ): nir.Next = {
      next match {
        case nir.Next.Unwind(exc, next) =>
          nir.Next.Unwind(exc, genNext(buf, next))
        case nir.Next.Case(value, next) =>
          nir.Next.Case(genVal(buf, value), genNext(buf, next))
        case nir.Next.Label(name, args) =>
          nir.Next.Label(name, args.map(genVal(buf, _)))
        case n => n
      }
    }

    private def optionallyBoxedUnit(v: nir.Val)(implicit
        pos: nir.Position
    ): nir.Val = {
      require(
        v.ty == nir.Type.Unit,
        s"Definition is expected to return Unit type, found ${v.ty}"
      )
      if (currentDefnRetType == nir.Type.Unit) nir.Val.Unit
      else unit
    }

    override def onInsts(insts: Seq[nir.Inst]): Seq[nir.Inst] = {
      val buf = new nir.Buffer()(fresh)
      val handlers = new nir.Buffer()(fresh)

      buf += insts.head

      def newUnwindHandler(
          next: nir.Next
      )(implicit pos: nir.Position): Option[nir.Local] =
        next match {
          case nir.Next.None =>
            None
          case nir.Next.Unwind(exc, next) =>
            val handler = fresh()
            handlers.label(handler, Seq(exc))
            handlers.jump(next)
            Some(handler)
          case _ =>
            util.unreachable
        }

      insts.foreach {
        case inst @ nir.Inst.Let(n, nir.Op.Var(ty), unwind) =>
          buf.let(n, nir.Op.Stackalloc(ty, one), unwind)(inst.pos, inst.scopeId)
        case _ => ()
      }

      val nir.Inst.Label(firstLabel, _) = insts.head: @unchecked
      val labelPositions = insts
        .collect { case nir.Inst.Label(id, _) => id }
        .zipWithIndex
        .toMap
      var currentBlockPosition = labelPositions(firstLabel)

      genThisValueNullGuardIfUsed(
        currentDefn.get,
        buf,
        () => newUnwindHandler(nir.Next.None)(insts.head.pos)
      )

      implicit var lastScopeId: nir.ScopeId = nir.ScopeId.TopLevel
      insts.tail.foreach {
        case inst @ nir.Inst.Let(n, op, unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)(inst.pos)
          ) {
            lastScopeId = inst.scopeId
            genLet(buf, n, op)(inst.pos, lastScopeId)
          }

        case inst @ nir.Inst.Throw(v, unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)(inst.pos)
          ) {
            genThrow(buf, v)(inst.pos, lastScopeId)
          }

        case inst @ nir.Inst.Unreachable(unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)(inst.pos)
          ) {
            genUnreachable(buf)(inst.pos)
          }

        case inst @ nir.Inst.Ret(v) =>
          implicit val pos: nir.Position = inst.pos
          currentDefn.get.name match {
            case nir.Global.Member(ClassRef(cls), sig)
                if sig.isCtor && cls.hasFinalFields =>
              // Release memory fence after initialization of constructor with final fields
              buf.fence(nir.MemoryOrder.Release)
            case _ => ()
          }
          genGCSafepoint(buf)
          val retVal =
            if (v.ty == nir.Type.Unit) optionallyBoxedUnit(v)
            else genVal(buf, v)
          buf += nir.Inst.Ret(retVal)

        case inst @ nir.Inst.Jump(next) =>
          implicit val pos: nir.Position = inst.pos
          // Generate safepoint before backward jumps, eg. in loops
          next match {
            case nir.Next.Label(target, _)
                if labelPositions(target) < currentBlockPosition =>
              genGCSafepoint(buf)
            case _ => ()
          }
          buf += nir.Inst.Jump(genNext(buf, next))

        case inst @ nir.Inst.Label(name, _) =>
          currentBlockPosition = labelPositions(name)
          buf += inst

        case inst =>
          buf += inst
      }

      implicit val pos: nir.Position = nir.Position.NoPosition
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

    override def onInst(inst: nir.Inst): nir.Inst = {
      implicit def pos: nir.Position = inst.pos
      inst match {
        case nir.Inst.Ret(v) if v.ty == nir.Type.Unit =>
          nir.Inst.Ret(optionallyBoxedUnit(v))
        case _ =>
          super.onInst(inst)
      }
    }

    override def onVal(value: nir.Val): nir.Val = value match {
      case nir.Val.ClassOf(_) =>
        util.unsupported("Lowering ClassOf needs nir.Buffer")
      case nir.Val.Global(ScopeRef(node), _) =>
        rtti(node).const
      case nir.Val.String(v) =>
        genStringVal(v)
      case nir.Val.Unit =>
        unit
      case _ =>
        super.onVal(value)
    }

    def genVal(buf: nir.Buffer, value: nir.Val)(implicit
        pos: nir.Position
    ): nir.Val =
      value match {
        case nir.Val.ClassOf(ScopeRef(node)) =>
          rtti(node).const
        case nir.Val.Const(v) =>
          nir.Val.Const(genVal(buf, v))
        case nir.Val.StructValue(values) =>
          nir.Val.StructValue(values.map(genVal(buf, _)))
        case nir.Val.ArrayValue(ty, values) =>
          nir.Val.ArrayValue(onType(ty), values.map(genVal(buf, _)))
        case _ => onVal(value)
      }

    def genNullPointerSlowPath(
        buf: nir.Buffer
    )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Unit = {
      nullPointerSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(
              throwNullPointerTy,
              throwNullPointerVal,
              Seq(nir.Val.Null),
              unwind
            )
            buf.unreachable(nir.Next.None)
          }
      }
    }

    def genDivisionByZeroSlowPath(
        buf: nir.Buffer
    )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Unit = {
      divisionByZeroSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(
              throwDivisionByZeroTy,
              throwDivisionByZeroVal,
              Seq(nir.Val.Null),
              unwind
            )
            buf.unreachable(nir.Next.None)
          }
      }
    }

    def genClassCastSlowPath(
        buf: nir.Buffer
    )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Unit = {
      classCastSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val obj = nir.Val.Local(fresh(), nir.Type.Ptr)
            val toty = nir.Val.Local(fresh(), nir.Type.Ptr)

            buf.label(slowPath, Seq(obj, toty))
            val fromty = buf.let(nir.Op.Load(nir.Type.Ptr, obj), unwind)
            buf.call(
              throwClassCastTy,
              throwClassCastVal,
              Seq(nir.Val.Null, fromty, toty),
              unwind
            )
            buf.unreachable(nir.Next.None)
          }
      }
    }

    def genUnreachableSlowPath(
        buf: nir.Buffer
    )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Unit = {
      unreachableSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(
              throwUndefinedTy,
              throwUndefinedVal,
              Seq(nir.Val.Null),
              unwind
            )
            buf.unreachable(nir.Next.None)
          }
      }
    }

    def genOutOfBoundsSlowPath(
        buf: nir.Buffer
    )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Unit = {
      outOfBoundsSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val idx = nir.Val.Local(fresh(), nir.Type.Int)

            buf.label(slowPath, Seq(idx))
            buf.call(
              throwOutOfBoundsTy,
              throwOutOfBoundsVal,
              Seq(nir.Val.Null, idx),
              unwind
            )
            buf.unreachable(nir.Next.None)
          }
      }
    }

    def genNoSuchMethodSlowPath(
        buf: nir.Buffer
    )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Unit = {
      noSuchMethodSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val sig = nir.Val.Local(fresh(), nir.Type.Ptr)

            buf.label(slowPath, Seq(sig))
            buf.call(
              throwNoSuchMethodTy,
              throwNoSuchMethodVal,
              Seq(nir.Val.Null, sig),
              unwind
            )
            buf.unreachable(nir.Next.None)
          }
      }
    }

    def genLet(buf: nir.Buffer, n: nir.Local, op: nir.Op)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit =
      op.resty match {
        case nir.Type.Unit =>
          genOp(buf, fresh(), op)
          buf.let(n, nir.Op.Copy(unit), unwind)
        case nir.Type.Nothing =>
          genOp(buf, fresh(), op)
          genUnreachable(buf)
          buf.label(fresh(), Seq(nir.Val.Local(n, op.resty)))
        case _ =>
          genOp(buf, n, op)
      }

    def genThrow(buf: nir.Buffer, exc: nir.Val)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      genGuardNotNull(buf, exc)
      genOp(buf, fresh(), nir.Op.Call(throwSig, throw_, Seq(exc)))
      buf.unreachable(nir.Next.None)
    }

    def genUnreachable(buf: nir.Buffer)(implicit pos: nir.Position) = {
      val failL = unreachableSlowPath.getOrElseUpdate(unwindHandler, fresh())

      buf.jump(nir.Next(failL))
    }

    def genOp(buf: nir.Buffer, n: nir.Local, op: nir.Op)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      op match {
        case op: nir.Op.Field =>
          genFieldOp(buf, n, op)
        case op: nir.Op.Fieldload =>
          genFieldloadOp(buf, n, op)
        case op: nir.Op.Fieldstore =>
          genFieldstoreOp(buf, n, op)
        case op: nir.Op.Load =>
          genLoadOp(buf, n, op)
        case op: nir.Op.Store =>
          genStoreOp(buf, n, op)
        case op: nir.Op.Method =>
          genMethodOp(buf, n, op)
        case op: nir.Op.Dynmethod =>
          genDynmethodOp(buf, n, op)
        case op: nir.Op.Is =>
          genIsOp(buf, n, op)
        case op: nir.Op.As =>
          genAsOp(buf, n, op)
        case op: nir.Op.SizeOf =>
          genSizeOfOp(buf, n, op)
        case op: nir.Op.AlignmentOf =>
          genAlignmentOfOp(buf, n, op)
        case op: nir.Op.Classalloc =>
          genClassallocOp(buf, n, op)
        case op: nir.Op.Conv =>
          genConvOp(buf, n, op)
        case op: nir.Op.Call =>
          genCallOp(buf, n, op)
        case op: nir.Op.Comp =>
          genCompOp(buf, n, op)
        case op: nir.Op.Bin =>
          genBinOp(buf, n, op)
        case op: nir.Op.Box =>
          genBoxOp(buf, n, op)
        case op: nir.Op.Unbox =>
          genUnboxOp(buf, n, op)
        case op: nir.Op.Module =>
          genModuleOp(buf, n, op)
        case nir.Op.Var(_) =>
          () // Already emmited
        case nir.Op.Varload(nir.Val.Local(slot, nir.Type.Var(ty))) =>
          buf.let(n, nir.Op.Load(ty, nir.Val.Local(slot, nir.Type.Ptr)), unwind)
        case nir.Op.Varstore(nir.Val.Local(slot, nir.Type.Var(ty)), value) =>
          buf.let(
            n,
            nir.Op
              .Store(ty, nir.Val.Local(slot, nir.Type.Ptr), genVal(buf, value)),
            unwind
          )
        case op: nir.Op.Arrayalloc =>
          genArrayallocOp(buf, n, op)
        case op: nir.Op.Arrayload =>
          genArrayloadOp(buf, n, op)
        case op: nir.Op.Arraystore =>
          genArraystoreOp(buf, n, op)
        case op: nir.Op.Arraylength =>
          genArraylengthOp(buf, n, op)
        case op: nir.Op.Stackalloc =>
          genStackallocOp(buf, n, op)
        case op: nir.Op.Copy =>
          val v = genVal(buf, op.value)
          buf.let(n, nir.Op.Copy(v), unwind)
        case _ =>
          buf.let(n, op, unwind)
      }
    }

    def genGuardNotNull(buf: nir.Buffer, obj: nir.Val)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit =
      obj.ty match {
        case ty: nir.Type.RefKind if !ty.isNullable =>
          ()

        case _ if nullGuardedVals.add(obj) =>
          import buf._
          val v = genVal(buf, obj)

          val notNullL = fresh()
          val isNullL =
            nullPointerSlowPath.getOrElseUpdate(unwindHandler, fresh())

          val isNull = comp(nir.Comp.Ine, v.ty, v, nir.Val.Null, unwind)
          branch(isNull, nir.Next(notNullL), nir.Next(isNullL))
          label(notNullL)

        case _ => ()
      }

    def genGuardInBounds(buf: nir.Buffer, idx: nir.Val, len: nir.Val)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      import buf._

      val inBoundsL = fresh()
      val outOfBoundsL =
        outOfBoundsSlowPath.getOrElseUpdate(unwindHandler, fresh())

      val gt0 = comp(nir.Comp.Sge, nir.Type.Int, idx, zero, unwind)
      val ltLen = comp(nir.Comp.Slt, nir.Type.Int, idx, len, unwind)
      val inBounds = bin(nir.Bin.And, nir.Type.Bool, gt0, ltLen, unwind)
      branch(
        inBounds,
        nir.Next(inBoundsL),
        nir.Next.Label(outOfBoundsL, Seq(idx))
      )
      label(inBoundsL)
    }

    def genFieldElemOp(buf: nir.Buffer, obj: nir.Val, name: nir.Global.Member)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      import buf._
      val v = genVal(buf, obj)
      val FieldRef(cls: Class, fld) = name: @unchecked

      val layout = meta.layout(cls)
      val ty = layout.struct
      val index = layout.index(fld)

      genGuardNotNull(buf, v)
      elem(ty, v, Seq(zero, nir.Val.Int(index)), unwind)
    }

    def genFieldloadOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Fieldload)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      val nir.Op.Fieldload(ty, obj, name) = op
      val field = name match {
        case FieldRef(_, field) => field
        case _ =>
          throw new LinkingException(s"Metadata for field '$name' not found")
      }

      val memoryOrder =
        if (field.attrs.isVolatile) nir.MemoryOrder.SeqCst
        else if (field.attrs.isFinal) nir.MemoryOrder.Acquire
        else nir.MemoryOrder.Unordered
      val elem = genFieldElemOp(buf, genVal(buf, obj), name)
      genLoadOp(buf, n, nir.Op.Load(ty, elem, Some(memoryOrder)))
    }

    def genFieldstoreOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Fieldstore)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      val nir.Op.Fieldstore(ty, obj, name, value) = op
      val field = name match {
        case FieldRef(_, field) => field
        case _ =>
          throw new LinkingException(s"Metadata for field '$name' not found")
      }

      // No explicit memory order for store of final field,
      // all final fields are published with release fence when existing the constructor
      val memoryOrder =
        if (field.attrs.isVolatile) nir.MemoryOrder.SeqCst
        else nir.MemoryOrder.Unordered
      val elem = genFieldElemOp(buf, genVal(buf, obj), name)
      genStoreOp(buf, n, nir.Op.Store(ty, elem, value, Some(memoryOrder)))
    }

    def genFieldOp(buf: nir.Buffer, n: nir.Local, op: nir.Op)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      val nir.Op.Field(obj, name) = op: @unchecked
      val elem = genFieldElemOp(buf, obj, name)
      buf.let(n, nir.Op.Copy(elem), unwind)
    }

    def genLoadOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Load)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      op match {
        // Convert synchronized load(bool) into load(byte)
        // LLVM is not providing synchronization on booleans
        case nir.Op.Load(nir.Type.Bool, ptr, memoryOrder @ Some(_)) =>
          val valueAsByte = fresh()
          val asPtr =
            if (platform.useOpaquePointers) ptr
            else {
              val asPtr = fresh()
              genConvOp(
                buf,
                asPtr,
                nir.Op.Conv(nir.Conv.Bitcast, nir.Type.Ptr, ptr)
              )
              nir.Val.Local(asPtr, nir.Type.Ptr)
            }
          genLoadOp(
            buf,
            valueAsByte,
            nir.Op.Load(nir.Type.Byte, asPtr, memoryOrder)
          )
          genConvOp(
            buf,
            n,
            nir.Op.Conv(
              nir.Conv.Trunc,
              nir.Type.Bool,
              nir.Val.Local(valueAsByte, nir.Type.Byte)
            )
          )

        case nir.Op.Load(ty, ptr, memoryOrder) =>
          buf.let(
            n,
            nir.Op.Load(ty, genVal(buf, ptr), memoryOrder),
            unwind
          )
      }
    }

    def genStoreOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Store)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      op match {
        // Convert synchronized store(bool) into store(byte)
        // LLVM is not providing synchronization on booleans
        case nir.Op.Store(nir.Type.Bool, ptr, value, memoryOrder @ Some(_)) =>
          val valueAsByte = fresh()
          val asPtr =
            if (platform.useOpaquePointers) ptr
            else {
              val asPtr = fresh()
              genConvOp(
                buf,
                asPtr,
                nir.Op.Conv(nir.Conv.Bitcast, nir.Type.Ptr, ptr)
              )
              nir.Val.Local(asPtr, nir.Type.Ptr)
            }
          genConvOp(
            buf,
            valueAsByte,
            nir.Op.Conv(nir.Conv.Zext, nir.Type.Byte, value)
          )
          genStoreOp(
            buf,
            n,
            nir.Op.Store(
              nir.Type.Byte,
              asPtr,
              nir.Val.Local(valueAsByte, nir.Type.Byte),
              memoryOrder
            )
          )

        case nir.Op.Store(ty, ptr, value, memoryOrder) =>
          buf.let(
            n,
            nir.Op.Store(ty, genVal(buf, ptr), genVal(buf, value), memoryOrder),
            unwind
          )
      }
    }

    def genCompOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Comp)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Comp(comp, ty, l, r) = op
      val left = genVal(buf, l)
      val right = genVal(buf, r)
      buf.let(n, nir.Op.Comp(comp, ty, left, right), unwind)
    }

    // Cached function
    private object shouldGenerateSafepoints {
      import scalanative.build.GC._
      private var lastDefn: nir.Defn.Define = _
      private var lastResult: Boolean = false

      private val supportedGC = meta.config.gc match {
        case Immix | Commix => true
        case _              => false
      }
      private val multithreadingEnabled = meta.platform.isMultithreadingEnabled
      private val usesSafepoints = multithreadingEnabled && supportedGC

      def apply(defn: nir.Defn.Define): Boolean = {
        if (!usesSafepoints) false
        else if (defn eq lastDefn) lastResult
        else {
          lastDefn = defn
          val nir.Global.Member(_, sig) = defn.name
          lastResult = {
            // Exclude accessors and generated methods
            def mayContainLoops =
              defn.insts.exists(_.isInstanceOf[nir.Inst.Jump])
            !sig.isGenerated && (defn.insts.size > 4 || mayContainLoops)
          }
          lastResult
        }
      }
    }
    private def genGCSafepoint(buf: nir.Buffer, genUnwind: Boolean = true)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      if (shouldGenerateSafepoints(currentDefn.get)) {
        val handler = {
          if (genUnwind && unwindHandler.isInitialized) unwind
          else nir.Next.None
        }
        val safepointAddr = buf.load(nir.Type.Ptr, GCSafepoint, handler)
        buf.load(
          nir.Type.Ptr,
          safepointAddr,
          handler,
          Some(nir.MemoryOrder.Unordered)
        )
      }
    }

    def genCallOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Call)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Call(ty, ptr, args) = op
      def genCall() = {
        buf.let(
          n,
          nir.Op.Call(
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
        Seq(nir.Val.Int(if (managed) 0 else 1)),
        if (unwindHandler.isInitialized) unwind else nir.Next.None
      )

      def shouldSwitchThreadState(name: nir.Global) =
        platform.isMultithreadingEnabled && analysis.infos.get(name).exists {
          info =>
            val attrs = info.attrs
            attrs.isExtern && attrs.isBlocking
        }

      ptr match {
        case nir.Val.Global(global, _) if shouldSwitchThreadState(global) =>
          switchThreadState(managed = false)
          genCall()
          genGCSafepoint(buf, genUnwind = false)
          switchThreadState(managed = true)

        case _ => genCall()
      }
    }

    def genMethodOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Method)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      import buf._

      val nir.Op.Method(v, sig) = op
      val obj = genVal(buf, v)

      def genClassVirtualLookup(cls: Class): Unit = {
        val vindex = vtable(cls).index(sig)
        assert(
          vindex != -1,
          s"The virtual table of ${cls.name} does not contain $sig"
        )

        val typeptr = let(nir.Op.Load(nir.Type.Ptr, obj), unwind)
        val methptrptr = let(
          nir.Op.Elem(
            rtti(cls).struct,
            typeptr,
            ClassRttiVtablePath :+ nir.Val.Int(vindex)
          ),
          unwind
        )

        let(n, nir.Op.Load(nir.Type.Ptr, methptrptr), unwind)
      }

      def genTraitVirtualLookup(trt: Trait): Unit = {
        val sigid = dispatchTable.traitSigIds(sig)
        val typeptr = let(nir.Op.Load(nir.Type.Ptr, obj), unwind)
        val idptr =
          let(nir.Op.Elem(Rtti.layout, typeptr, RttiTraitIdPath), unwind)
        val id = let(nir.Op.Load(nir.Type.Int, idptr), unwind)
        val rowptr = let(
          nir.Op.Elem(
            nir.Type.Ptr,
            dispatchTable.dispatchVal,
            Seq(nir.Val.Int(dispatchTable.dispatchOffset(sigid)))
          ),
          unwind
        )
        val methptrptr =
          let(nir.Op.Elem(nir.Type.Ptr, rowptr, Seq(id)), unwind)
        let(n, nir.Op.Load(nir.Type.Ptr, methptrptr), unwind)
      }

      def genMethodLookup(scope: ScopeInfo): Unit = {
        scope.targets(sig).toSeq match {
          case Seq() =>
            let(n, nir.Op.Copy(nir.Val.Null), unwind)
          case Seq(impl) =>
            let(n, nir.Op.Copy(nir.Val.Global(impl, nir.Type.Ptr)), unwind)
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
        let(n, nir.Op.Copy(nir.Val.Global(method, nir.Type.Ptr)), unwind)
      }

      def staticMethodIn(cls: Class): Boolean =
        !sig.isVirtual || !cls.calls.contains(sig)

      // We check type of original value, because it may change inside `genVal` transformation
      // Eg. nir.Val.String is transformed to Const(StructValue) which changes type from Ref to Ptr
      v.ty match {
        // Method call with `null` ref argument might be inlined, in such case materialization of local value in Eval would
        // result with nir.Val.Null. We're directly throwing NPE which normally would be handled in slow path of `genGuardNotNull`
        case nir.Type.Null =>
          let(
            n,
            nir.Op
              .Call(throwNullPointerTy, throwNullPointerVal, Seq(nir.Val.Null)),
            unwind
          )
          buf.unreachable(nir.Next.None)

        case ClassRef(cls) if staticMethodIn(cls) =>
          genStaticMethod(cls)

        case ScopeRef(scope) =>
          genGuardNotNull(buf, obj)
          genMethodLookup(scope)

        case _ => util.unreachable
      }
    }

    def genDynmethodOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Dynmethod)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      import buf._

      val nir.Op.Dynmethod(v, sig) = op
      val obj = genVal(buf, v)

      def throwIfNull(value: nir.Val) = {
        val notNullL = fresh()
        val noSuchMethodL =
          noSuchMethodSlowPath.getOrElseUpdate(unwindHandler, fresh())

        val condNull =
          comp(nir.Comp.Ine, nir.Type.Ptr, value, nir.Val.Null, unwind)
        branch(
          condNull,
          nir.Next(notNullL),
          nir.Next.Label(noSuchMethodL, Seq(nir.Val.String(sig.mangle)))
        )
        label(notNullL)
      }

      def genReflectiveLookup(): nir.Val = {
        val methodIndex =
          meta.analysis.dynsigs.zipWithIndex.find(_._1 == sig).get._2

        // Load the type information pointer
        val typeptr = load(nir.Type.Ptr, obj, unwind)
        // Load the dynamic hash map for given type, make sure it's not null
        val mapelem = elem(classRttiType, typeptr, ClassRttiDynmapPath, unwind)
        val mapptr = load(nir.Type.Ptr, mapelem, unwind)
        // If hash map is not null, it has to contain at least one entry
        throwIfNull(mapptr)
        // Perform dynamic dispatch via dyndispatch helper
        val methptrptr = call(
          dyndispatchSig,
          dyndispatch,
          Seq(mapptr, nir.Val.Int(methodIndex)),
          unwind
        )
        // Hash map lookup can still not contain given signature
        throwIfNull(methptrptr)
        let(n, nir.Op.Load(nir.Type.Ptr, methptrptr), unwind)
      }

      genGuardNotNull(buf, obj)
      genReflectiveLookup()
    }

    def genIsOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Is)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      import buf._

      op match {
        case nir.Op.Is(_, nir.Val.Null | nir.Val.Zero(_)) =>
          let(n, nir.Op.Copy(nir.Val.False), unwind)

        case nir.Op.Is(ty, v) =>
          val obj = genVal(buf, v)
          val isNullL, checkL, resultL = fresh()

          // check if obj is null
          val isNull = let(
            nir.Op.Comp(nir.Comp.Ieq, nir.Type.Ptr, obj, nir.Val.Null),
            unwind
          )
          branch(isNull, nir.Next(isNullL), nir.Next(checkL))

          // in case it's null, result is always false
          label(isNullL)
          jump(resultL, Seq(nir.Val.False))

          // otherwise, do an actual instance check
          label(checkL)
          val isInstanceOf = genIsOp(buf, ty, obj)
          jump(resultL, Seq(isInstanceOf))

          // merge the result of two branches
          label(resultL, Seq(nir.Val.Local(n, op.resty)))
      }
    }

    def genIsOp(buf: nir.Buffer, ty: nir.Type, v: nir.Val)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): nir.Val = {
      import buf._
      val obj = genVal(buf, v)

      ty match {
        case ClassRef(cls) if meta.ranges(cls).length == 1 =>
          val typeptr = let(nir.Op.Load(nir.Type.Ptr, obj), unwind)
          let(
            nir.Op.Comp(nir.Comp.Ieq, nir.Type.Ptr, typeptr, rtti(cls).const),
            unwind
          )

        case ClassRef(cls) =>
          val range = meta.ranges(cls)
          val typeptr = let(nir.Op.Load(nir.Type.Ptr, obj), unwind)
          val idptr =
            let(
              nir.Op.Elem(Rtti.layout, typeptr, RttiClassIdPath),
              unwind
            )
          val id = let(nir.Op.Load(nir.Type.Int, idptr), unwind)
          val ge =
            let(
              nir.Op
                .Comp(nir.Comp.Sle, nir.Type.Int, nir.Val.Int(range.start), id),
              unwind
            )
          val le =
            let(
              nir.Op
                .Comp(nir.Comp.Sle, nir.Type.Int, id, nir.Val.Int(range.end)),
              unwind
            )
          let(nir.Op.Bin(nir.Bin.And, nir.Type.Bool, ge, le), unwind)

        case TraitRef(trt) =>
          val typeptr = let(nir.Op.Load(nir.Type.Ptr, obj), unwind)
          val idptr =
            let(
              nir.Op.Elem(Rtti.layout, typeptr, RttiClassIdPath),
              unwind
            )
          val id = let(nir.Op.Load(nir.Type.Int, idptr), unwind)
          let(
            nir.Op.Call(
              Generate.ClassHasTraitSig,
              nir.Val.Global(
                Generate.ClassHasTraitName,
                Generate.ClassHasTraitSig
              ),
              Seq(id, nir.Val.Int(meta.ids(trt)))
            ),
            unwind
          )
        case _ =>
          util.unsupported(s"is[$ty] $obj")
      }
    }

    def genAsOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.As)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      import buf._

      op match {
        case nir.Op.As(ty: nir.Type.RefKind, v) if v.ty == nir.Type.Null =>
          let(n, nir.Op.Copy(nir.Val.Null), unwind)

        case nir.Op.As(ty: nir.Type.RefKind, obj)
            if obj.ty.isInstanceOf[nir.Type.RefKind] =>
          val v = genVal(buf, obj)
          val checkIfIsInstanceOfL, castL = fresh()
          val failL = classCastSlowPath.getOrElseUpdate(unwindHandler, fresh())

          val isNull = comp(nir.Comp.Ieq, v.ty, v, nir.Val.Null, unwind)
          branch(isNull, nir.Next(castL), nir.Next(checkIfIsInstanceOfL))

          label(checkIfIsInstanceOfL)
          val isInstanceOf = genIsOp(buf, ty, v)
          val toTy = rtti(analysis.infos(ty.className)).const
          branch(
            isInstanceOf,
            nir.Next(castL),
            nir.Next.Label(failL, Seq(v, toTy))
          )

          label(castL)
          if (platform.useOpaquePointers)
            let(n, nir.Op.Copy(v), unwind)
          else
            let(n, nir.Op.Conv(nir.Conv.Bitcast, ty, v), unwind)

        case nir.Op.As(to, v) =>
          util.unsupported(s"can't cast from ${v.ty} to $to")
      }
    }

    def genSizeOfOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.SizeOf)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val size = op.ty match {
        case ClassRef(cls) if op.ty != nir.Type.Unit =>
          if (!cls.allocated) {
            val nir.Global.Top(clsName) = cls.name
            logger.warn(
              s"Referencing size of non allocated type ${clsName} in ${srcPosition.show}"
            )
          }
          meta.layout(cls).size
        case _ => MemoryLayout.sizeOf(op.ty)
      }
      buf.let(n, nir.Op.Copy(nir.Val.Size(size)), unwind)
    }

    def genAlignmentOfOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.AlignmentOf)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val alignment = MemoryLayout.alignmentOf(op.ty)
      buf.let(n, nir.Op.Copy(nir.Val.Size(alignment)), unwind)
    }

    def genClassallocOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Classalloc)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Classalloc(ClassRef(cls), v) = op: @unchecked
      val zone = v.map(genVal(buf, _))

      val size = meta.layout(cls).size
      assert(size == size.toInt)

      zone match {
        case Some(zone) =>
          val safeZoneAllocImplMethod = nir.Val.Local(fresh(), nir.Type.Ptr)
          genMethodOp(
            buf,
            safeZoneAllocImplMethod.id,
            nir.Op.Method(zone, safeZoneAllocImpl.sig)
          )
          buf.let(
            n,
            nir.Op.Call(
              safeZoneAllocImplSig,
              safeZoneAllocImplMethod,
              Seq(zone, rtti(cls).const, nir.Val.Size(size.toInt))
            ),
            unwind
          )
        case None =>
          val allocMethod =
            if (size < LARGE_OBJECT_MIN_SIZE) alloc else largeAlloc
          buf.let(
            n,
            nir.Op.Call(
              allocSig(cls.ty),
              allocMethod,
              Seq(rtti(cls).const, nir.Val.Size(size.toInt))
            ),
            unwind
          )
      }
    }

    def genConvOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Conv)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      import buf._

      op match {
        // Fptosi is undefined behaviour on LLVM if the resulting
        // value doesn't fit the MIN...MAX range for given integer type.
        // We insert range checks and return MIN_VALUE for floating values
        // that are numerically less than or equal to MIN_VALUE and MAX_VALUE
        // for the ones which are greate or equal to MAX_VALUE. Additionally,
        // NaNs are converted to 0.
        case nir.Op.Conv(nir.Conv.Fptosi, toty, value) =>
          val v = genVal(buf, value)
          val (imin, imax, fmin, fmax) = toty match {
            case nir.Type.Int =>
              val min = java.lang.Integer.MIN_VALUE
              val max = java.lang.Integer.MAX_VALUE
              v.ty match {
                case nir.Type.Float =>
                  (
                    nir.Val.Int(min),
                    nir.Val.Int(max),
                    nir.Val.Float(min.toFloat),
                    nir.Val.Float(max.toFloat)
                  )
                case nir.Type.Double =>
                  (
                    nir.Val.Int(min),
                    nir.Val.Int(max),
                    nir.Val.Double(min.toDouble),
                    nir.Val.Double(max.toDouble)
                  )
                case _ =>
                  util.unreachable
              }
            case nir.Type.Long =>
              val min = java.lang.Long.MIN_VALUE
              val max = java.lang.Long.MAX_VALUE
              v.ty match {
                case nir.Type.Float =>
                  (
                    nir.Val.Long(min),
                    nir.Val.Long(max),
                    nir.Val.Float(min.toFloat),
                    nir.Val.Float(max.toFloat)
                  )
                case nir.Type.Double =>
                  (
                    nir.Val.Long(min),
                    nir.Val.Long(max),
                    nir.Val.Double(min.toDouble),
                    nir.Val.Double(max.toDouble)
                  )
                case _ =>
                  util.unreachable
              }
            case _ =>
              util.unreachable
          }

          val isNaNL, checkLessThanMinL, lessThanMinL, checkLargerThanMaxL,
              largerThanMaxL, inBoundsL, resultL = fresh()

          val isNaN = comp(nir.Comp.Fne, v.ty, v, v, unwind)
          branch(isNaN, nir.Next(isNaNL), nir.Next(checkLessThanMinL))

          label(isNaNL)
          jump(resultL, Seq(nir.Val.Zero(op.resty)))

          label(checkLessThanMinL)
          val isLessThanMin = comp(nir.Comp.Fle, v.ty, v, fmin, unwind)
          branch(
            isLessThanMin,
            nir.Next(lessThanMinL),
            nir.Next(checkLargerThanMaxL)
          )

          label(lessThanMinL)
          jump(resultL, Seq(imin))

          label(checkLargerThanMaxL)
          val isLargerThanMax = comp(nir.Comp.Fge, v.ty, v, fmax, unwind)
          branch(isLargerThanMax, nir.Next(largerThanMaxL), nir.Next(inBoundsL))

          label(largerThanMaxL)
          jump(resultL, Seq(imax))

          label(inBoundsL)
          val inBoundsResult = let(op, unwind)
          jump(resultL, Seq(inBoundsResult))

          label(resultL, Seq(nir.Val.Local(n, op.resty)))

        case nir.Op.Conv(conv, ty, value) =>
          let(n, nir.Op.Conv(conv, ty, genVal(buf, value)), unwind)
      }
    }

    def genBinOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Bin)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      import buf._

      // LLVM's division by zero is undefined behaviour. We guard
      // the case when the divisor is zero and fail gracefully
      // by throwing an arithmetic exception.
      def checkDivisionByZero(op: nir.Op.Bin): Unit = {
        val nir.Op.Bin(bin, ty: nir.Type.I, dividend, divisor) = op: @unchecked

        val thenL, elseL = fresh()

        val succL = fresh()
        val failL =
          divisionByZeroSlowPath.getOrElseUpdate(unwindHandler, fresh())

        val isZero =
          comp(nir.Comp.Ine, ty, divisor, nir.Val.Zero(ty), unwind)
        branch(isZero, nir.Next(succL), nir.Next(failL))

        label(succL)
        if (bin == nir.Bin.Srem || bin == nir.Bin.Sdiv) {
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
      def checkDivisionOverflow(op: nir.Op.Bin): Unit = {
        val nir.Op.Bin(bin, ty: nir.Type.I, dividend, divisor) = op: @unchecked

        val mayOverflowL, noOverflowL, didOverflowL, resultL = fresh()

        val minus1 = ty match {
          case nir.Type.Int =>
            nir.Val.Int(-1)
          case nir.Type.Long =>
            nir.Val.Long(-1L)
          case nir.Type.Size =>
            nir.Val.Size(-1L)
          case _ =>
            util.unreachable
        }

        val minValue = ty match {
          case nir.Type.Int =>
            nir.Val.Int(java.lang.Integer.MIN_VALUE)
          case nir.Type.Long =>
            nir.Val.Long(java.lang.Long.MIN_VALUE)
          case nir.Type.Size =>
            if (platform.is32Bit) nir.Val.Size(java.lang.Integer.MIN_VALUE)
            else nir.Val.Size(java.lang.Long.MIN_VALUE)
          case _ =>
            util.unreachable
        }

        val divisorIsMinus1 =
          let(nir.Op.Comp(nir.Comp.Ieq, ty, divisor, minus1), unwind)
        branch(divisorIsMinus1, nir.Next(mayOverflowL), nir.Next(noOverflowL))

        label(mayOverflowL)
        val dividendIsMinValue =
          let(nir.Op.Comp(nir.Comp.Ieq, ty, dividend, minValue), unwind)
        branch(
          dividendIsMinValue,
          nir.Next(didOverflowL),
          nir.Next(noOverflowL)
        )

        label(didOverflowL)
        val overflowResult = bin match {
          case nir.Bin.Srem =>
            nir.Val.Zero(ty)
          case nir.Bin.Sdiv =>
            minValue
          case _ =>
            util.unreachable
        }
        jump(resultL, Seq(overflowResult))

        label(noOverflowL)
        val noOverflowResult = let(op, unwind)
        jump(resultL, Seq(noOverflowResult))

        label(resultL, Seq(nir.Val.Local(n, ty)))
      }

      // Shifts are undefined if the bits shifted by are >= bits in the type.
      // We mask the right hand side with bits in type - 1 to make it defined.
      def maskShift(op: nir.Op.Bin) = {
        val nir.Op.Bin(_, ty: nir.Type.I, _, r) = op: @unchecked
        val mask = ty match {
          case nir.Type.Int =>
            nir.Val.Int(31)
          case nir.Type.Long =>
            nir.Val.Int(63)
          case _ =>
            util.unreachable
        }
        val masked = bin(nir.Bin.And, ty, r, mask, unwind)
        let(n, op.copy(r = masked), unwind)
      }

      op match {
        case op @ nir.Op.Bin(
              bin @ (nir.Bin.Srem | nir.Bin.Urem | nir.Bin.Sdiv | nir.Bin.Udiv),
              ty: nir.Type.I,
              l,
              r
            ) =>
          checkDivisionByZero(op)

        case op @ nir.Op.Bin(
              bin @ (nir.Bin.Shl | nir.Bin.Lshr | nir.Bin.Ashr),
              ty: nir.Type.I,
              l,
              r
            ) =>
          maskShift(op)

        case op =>
          let(n, op, unwind)
      }
    }

    def genBoxOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Box)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Box(ty, v) = op
      val from = genVal(buf, v)

      val methodName = BoxTo(ty)
      val moduleName = methodName.top

      val boxTy =
        nir.Type.Function(Seq(nir.Type.Ref(moduleName), nir.Type.unbox(ty)), ty)

      buf.let(
        n,
        nir.Op.Call(
          boxTy,
          nir.Val.Global(methodName, nir.Type.Ptr),
          Seq(nir.Val.Null, from)
        ),
        unwind
      )
    }

    def genUnboxOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Unbox)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Unbox(ty, v) = op
      val from = genVal(buf, v)

      val methodName = UnboxTo(ty)
      val moduleName = methodName.top

      val unboxTy =
        nir.Type.Function(Seq(nir.Type.Ref(moduleName), ty), nir.Type.unbox(ty))

      buf.let(
        n,
        nir.Op.Call(
          unboxTy,
          nir.Val.Global(methodName, nir.Type.Ptr),
          Seq(nir.Val.Null, from)
        ),
        unwind
      )
    }

    def genModuleOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Module)(implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ) = {
      val nir.Op.Module(name) = op

      meta.analysis.infos(name) match {
        case cls: Class if cls.isConstantModule =>
          val instance = name.member(nir.Sig.Generated("instance"))
          buf.let(
            n,
            nir.Op.Copy(nir.Val.Global(instance, nir.Type.Ptr)),
            unwind
          )

        case _ =>
          val loadSig = nir.Type.Function(Seq.empty, nir.Type.Ref(name))
          val load =
            nir.Val.Global(name.member(nir.Sig.Generated("load")), nir.Type.Ptr)

          buf.let(n, nir.Op.Call(loadSig, load, Seq.empty), unwind)
      }
    }

    def genArrayallocOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Arrayalloc)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Arrayalloc(ty, v1, v2) = op
      val init = genVal(buf, v1)
      val zone = v2.map(genVal(buf, _))
      init match {
        case len if len.ty == nir.Type.Int =>
          val (arrayAlloc, arrayAllocSig) = zone match {
            case Some(_) => (arrayZoneAlloc, arrayZoneAllocSig)
            case None    => (arrayHeapAlloc, arrayHeapAllocSig)
          }
          val sig = arrayAllocSig.getOrElse(ty, arrayAllocSig(nir.Rt.Object))
          val func = arrayAlloc.getOrElse(ty, arrayAlloc(nir.Rt.Object))
          val module = genModuleOp(buf, fresh(), nir.Op.Module(func.owner))
          buf.let(
            n,
            nir.Op.Call(
              sig,
              nir.Val.Global(func, nir.Type.Ptr),
              zone match {
                case Some(zone) => Seq(module, len, zone)
                case None       => Seq(module, len)
              }
            ),
            unwind
          )
        case arrval: nir.Val.ArrayValue =>
          val sig =
            arraySnapshotSig.getOrElse(ty, arraySnapshotSig(nir.Rt.Object))
          val func = arraySnapshot.getOrElse(ty, arraySnapshot(nir.Rt.Object))
          val module = genModuleOp(buf, fresh(), nir.Op.Module(func.owner))
          val len = nir.Val.Int(arrval.values.length)
          val init = nir.Val.Const(arrval)
          buf.let(
            n,
            nir.Op.Call(
              sig,
              nir.Val.Global(func, nir.Type.Ptr),
              Seq(module, len, init)
            ),
            unwind
          )
        case _ => util.unreachable
      }
    }

    private def arrayMemoryLayout(
        ty: nir.Type,
        length: Int = 0
    ): nir.Type.StructValue = nir.Type.StructValue(
      Seq(ArrayHeader.layout, nir.Type.ArrayValue(ty, length))
    )
    private def arrayValuePath(idx: nir.Val) = Seq(zero, one, idx)

    def genArrayloadOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Arrayload)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Arrayload(ty, v, idx) = op
      val arr = genVal(buf, v)

      val len = fresh()

      genArraylengthOp(buf, len, nir.Op.Arraylength(arr))
      genGuardInBounds(buf, idx, nir.Val.Local(len, nir.Type.Int))

      val arrTy = arrayMemoryLayout(ty)
      val elemPtr = buf.elem(arrTy, arr, arrayValuePath(idx), unwind)
      buf.let(n, nir.Op.Load(ty, elemPtr), unwind)
    }

    def genArraystoreOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Arraystore)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Arraystore(ty, arr, idx, v) = op
      val len = fresh()
      val value = genVal(buf, v)

      genArraylengthOp(buf, len, nir.Op.Arraylength(arr))
      genGuardInBounds(buf, idx, nir.Val.Local(len, nir.Type.Int))

      val arrTy = arrayMemoryLayout(ty)
      val elemPtr = buf.elem(arrTy, arr, arrayValuePath(idx), unwind)
      genStoreOp(buf, n, nir.Op.Store(ty, elemPtr, value))
    }

    def genArraylengthOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Arraylength)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Arraylength(v) = op
      val arr = genVal(buf, v)

      val sig = arrayLengthSig
      val func = arrayLength

      genGuardNotNull(buf, arr)
      val lenPtr =
        buf.elem(ArrayHeader.layout, arr, ArrayHeaderLengthPath, unwind)
      buf.let(n, nir.Op.Load(nir.Type.Int, lenPtr), unwind)
    }

    def genStackallocOp(buf: nir.Buffer, n: nir.Local, op: nir.Op.Stackalloc)(
        implicit
        srcPosition: nir.Position,
        scopeId: nir.ScopeId
    ): Unit = {
      val nir.Op.Stackalloc(ty, size) = op
      val initValue = nir.Val.Zero(ty).canonicalize
      val pointee = buf.let(n, op, unwind)
      size match {
        case nir.Val.Size(1) if initValue.isCanonical =>
          buf.let(
            nir.Op.Store(ty, pointee, initValue, None),
            unwind
          )
        case sizeV =>
          val elemSize = MemoryLayout.sizeOf(ty)
          val size = sizeV match {
            case nir.Val.Size(v) => nir.Val.Size(v * elemSize)
            case _ =>
              val asSize = sizeV.ty match {
                case nir.Type.FixedSizeI(width, _) =>
                  if (width == platform.sizeOfPtrBits) sizeV
                  else if (width > platform.sizeOfPtrBits)
                    buf.conv(nir.Conv.Trunc, nir.Type.Size, sizeV, unwind)
                  else
                    buf.conv(nir.Conv.Zext, nir.Type.Size, sizeV, unwind)

                case _ => sizeV
              }
              if (elemSize == 1) asSize
              else
                buf.let(
                  nir.Op.Bin(
                    nir.Bin.Imul,
                    nir.Type.Size,
                    asSize,
                    nir.Val.Size(elemSize)
                  ),
                  unwind
                )
          }
          buf.call(
            memsetSig,
            memset,
            Seq(pointee, nir.Val.Int(0), size),
            unwind
          )
      }
    }

    def genStringVal(value: String): nir.Val = {
      val StringCls = ClassRef.unapply(nir.Rt.StringName).get
      val CharArrayCls = ClassRef.unapply(CharArrayName).get

      val chars = value.toCharArray
      val charsLength = nir.Val.Int(chars.length)
      val charsConst = nir.Val.Const(
        nir.Val.StructValue(
          rtti(CharArrayCls).const ::
            meta.lockWordVals :::
            charsLength ::
            nir.Val.Int(2) :: // stride is used only by GC
            nir.Val.ArrayValue(
              nir.Type.Char,
              chars.toSeq.map(nir.Val.Char(_))
            ) :: Nil
        )
      )

      val fieldValues = stringFieldNames.map {
        case nir.Rt.StringValueName =>
          charsConst
        case nir.Rt.StringOffsetName =>
          zero
        case nir.Rt.StringCountName =>
          charsLength
        case nir.Rt.StringCachedHashCodeName =>
          nir.Val.Int(stringHashCode(value))
        case _ =>
          util.unreachable
      }

      nir.Val.Const(
        nir.Val.StructValue(
          rtti(StringCls).const ::
            meta.lockWordVals ++
            fieldValues
        )
      )
    }

    private def genThisValueNullGuardIfUsed(
        defn: nir.Defn.Define,
        buf: nir.Buffer,
        createUnwindHandler: () => Option[nir.Local]
    ) = {
      def usesValue(expected: nir.Val): Boolean = {
        var wasUsed = false
        import scala.util.control.Breaks._
        breakable {
          new nir.Traverse {
            override def onVal(value: nir.Val): Unit = {
              wasUsed = expected eq value
              if (wasUsed) break()
              else super.onVal(value)
            }
            // We're not intrested in cheecking these structures, skip them
            override def onType(ty: nir.Type): Unit = ()
            override def onNext(next: nir.Next): Unit = ()
          }.onDefn(defn)
        }
        wasUsed
      }

      val nir.Global.Member(_, sig) = defn.name
      val nir.Inst.Label(_, args) = defn.insts.head: @unchecked

      val canHaveThisValue =
        !(sig.isStatic || sig.isClinit || sig.isExtern)

      if (canHaveThisValue) {
        args.headOption.foreach { thisValue =>
          thisValue.ty match {
            case ref: nir.Type.Ref if ref.isNullable && usesValue(thisValue) =>
              implicit def pos: nir.Position = defn.pos
              implicit def scopeId: nir.ScopeId = nir.ScopeId.TopLevel
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

  val allocSig: nir.Type.Function =
    nir.Type.Function(Seq(nir.Type.Ptr, nir.Type.Size), nir.Type.Ptr)
  def allocSig(clsType: nir.Type.RefKind): nir.Type.Function =
    allocSig.copy(ret = clsType)

  val allocSmallName = extern("scalanative_alloc_small")
  val alloc = nir.Val.Global(allocSmallName, allocSig)

  val largeAllocName = extern("scalanative_alloc_large")
  val largeAlloc = nir.Val.Global(largeAllocName, allocSig)

  val SafeZone =
    nir.Type.Ref(nir.Global.Top("scala.scalanative.memory.SafeZone"))
  val safeZoneAllocImplSig =
    nir.Type.Function(Seq(SafeZone, nir.Type.Ptr, nir.Type.Size), nir.Type.Ptr)
  val safeZoneAllocImpl = SafeZone.name.member(
    nir.Sig.Method("allocImpl", Seq(nir.Type.Ptr, nir.Type.Size, nir.Type.Ptr))
  )

  val dyndispatchName = extern("scalanative_dyndispatch")
  val dyndispatchSig =
    nir.Type.Function(Seq(nir.Type.Ptr, nir.Type.Int), nir.Type.Ptr)
  val dyndispatch = nir.Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = nir.Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    nir.Global.Member(excptnGlobal, nir.Sig.Ctor(Seq(nir.Rt.String)))

  val excInitSig = nir.Type.Function(
    Seq(
      nir.Type.Ref(excptnGlobal),
      nir.Type.Ref(nir.Global.Top("java.lang.String"))
    ),
    nir.Type.Unit
  )
  val excInit = nir.Val.Global(excptnInitGlobal, nir.Type.Ptr)

  val CharArrayName =
    nir.Global.Top("scala.scalanative.runtime.CharArray")

  val BoxesRunTime = nir.Global.Top("scala.runtime.BoxesRunTime$")
  val RuntimeBoxes = nir.Global.Top("scala.scalanative.runtime.Boxes$")

  val BoxTo: Map[nir.Type, nir.Global] = nir.Type.boxClasses.map { cls =>
    val name = cls.asInstanceOf[nir.Global.Top].id
    val boxty = nir.Type.Ref(nir.Global.Top(name))
    val module = if (name.startsWith("java.")) BoxesRunTime else RuntimeBoxes
    val id = "boxTo" + name.split("\\.").last
    val tys = Seq(nir.Type.unbox(boxty), boxty)
    val meth = module.member(nir.Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  val UnboxTo: Map[nir.Type, nir.Global] = nir.Type.boxClasses.map { cls =>
    val name = cls.asInstanceOf[nir.Global.Top].id
    val boxty = nir.Type.Ref(nir.Global.Top(name))
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
    val meth = module.member(nir.Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  private def extern(id: String): nir.Global.Member =
    nir.Global.Member(nir.Global.Top("__"), nir.Sig.Extern(id))

  val unitName = nir.Global.Top("scala.scalanative.runtime.BoxedUnit$")
  val unitInstance = unitName.member(nir.Sig.Generated("instance"))
  val unit = nir.Val.Global(unitInstance, nir.Type.Ptr)

  val throwName = extern("scalanative_throw")
  val throwSig = nir.Type.Function(Seq(nir.Type.Ptr), nir.Type.Nothing)
  val throw_ = nir.Val.Global(throwName, nir.Type.Ptr)

  val arrayHeapAlloc = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      val nir.Global.Top(id) = arrname
      val arrcls = nir.Type.Ref(arrname)
      ty -> nir.Global.Member(
        nir.Global.Top(id + "$"),
        nir.Sig.Method("alloc", Seq(nir.Type.Int, arrcls))
      )
  }.toMap
  val arrayHeapAllocSig = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      val nir.Global.Top(id) = arrname
      ty -> nir.Type.Function(
        Seq(nir.Type.Ref(nir.Global.Top(id + "$")), nir.Type.Int),
        nir.Type.Ref(arrname)
      )
  }.toMap
  val arrayZoneAlloc = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      val nir.Global.Top(id) = arrname
      val arrcls = nir.Type.Ref(arrname)
      ty -> nir.Global.Member(
        nir.Global.Top(id + "$"),
        nir.Sig.Method("alloc", Seq(nir.Type.Int, SafeZone, arrcls))
      )
  }.toMap
  val arrayZoneAllocSig = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      val nir.Global.Top(id) = arrname
      ty -> nir.Type.Function(
        Seq(nir.Type.Ref(nir.Global.Top(id + "$")), nir.Type.Int, SafeZone),
        nir.Type.Ref(arrname)
      )
  }.toMap
  val arraySnapshot = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      val nir.Global.Top(id) = arrname
      val arrcls = nir.Type.Ref(arrname)
      ty -> nir.Global.Member(
        nir.Global.Top(id + "$"),
        nir.Sig.Method("snapshot", Seq(nir.Type.Int, nir.Type.Ptr, arrcls))
      )
  }.toMap
  val arraySnapshotSig = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      val nir.Global.Top(id) = arrname
      ty -> nir.Type.Function(
        Seq(nir.Type.Ref(nir.Global.Top(id + "$")), nir.Type.Int, nir.Type.Ptr),
        nir.Type.Ref(arrname)
      )
  }.toMap
  val arrayApplyGeneric = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> nir.Global.Member(
        arrname,
        nir.Sig.Method("apply", Seq(nir.Type.Int, nir.Rt.Object))
      )
  }
  val arrayApply = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> nir.Global.Member(
        arrname,
        nir.Sig.Method("apply", Seq(nir.Type.Int, ty))
      )
  }.toMap
  val arrayApplySig = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> nir.Type.Function(Seq(nir.Type.Ref(arrname), nir.Type.Int), ty)
  }.toMap
  val arrayUpdateGeneric = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> nir.Global.Member(
        arrname,
        nir.Sig
          .Method("update", Seq(nir.Type.Int, nir.Rt.Object, nir.Type.Unit))
      )
  }
  val arrayUpdate = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> nir.Global.Member(
        arrname,
        nir.Sig.Method("update", Seq(nir.Type.Int, ty, nir.Type.Unit))
      )
  }.toMap
  val arrayUpdateSig = nir.Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> nir.Type.Function(
        Seq(nir.Type.Ref(arrname), nir.Type.Int, ty),
        nir.Type.Unit
      )
  }.toMap
  val arrayLength =
    nir.Global.Member(
      nir.Global.Top("scala.scalanative.runtime.Array"),
      nir.Sig.Method("length", Seq(nir.Type.Int))
    )
  val arrayLengthSig =
    nir.Type.Function(
      Seq(nir.Type.Ref(nir.Global.Top("scala.scalanative.runtime.Array"))),
      nir.Type.Int
    )

  val throwDivisionByZeroTy =
    nir.Type.Function(Seq(nir.Rt.Runtime), nir.Type.Nothing)
  val throwDivisionByZero =
    nir.Global.Member(
      nir.Rt.Runtime.name,
      nir.Sig.Method("throwDivisionByZero", Seq(nir.Type.Nothing))
    )
  val throwDivisionByZeroVal =
    nir.Val.Global(throwDivisionByZero, nir.Type.Ptr)

  val throwClassCastTy =
    nir.Type.Function(
      Seq(nir.Rt.Runtime, nir.Type.Ptr, nir.Type.Ptr),
      nir.Type.Nothing
    )
  val throwClassCast =
    nir.Global.Member(
      nir.Rt.Runtime.name,
      nir.Sig.Method(
        "throwClassCast",
        Seq(nir.Type.Ptr, nir.Type.Ptr, nir.Type.Nothing)
      )
    )
  val throwClassCastVal =
    nir.Val.Global(throwClassCast, nir.Type.Ptr)

  val throwNullPointerTy =
    nir.Type.Function(Seq(nir.Rt.Runtime), nir.Type.Nothing)
  val throwNullPointer =
    nir.Global.Member(
      nir.Rt.Runtime.name,
      nir.Sig.Method("throwNullPointer", Seq(nir.Type.Nothing))
    )
  val throwNullPointerVal =
    nir.Val.Global(throwNullPointer, nir.Type.Ptr)

  val throwUndefinedTy =
    nir.Type.Function(Seq(nir.Type.Ptr), nir.Type.Nothing)
  val throwUndefined =
    nir.Global.Member(
      nir.Rt.Runtime.name,
      nir.Sig.Method("throwUndefined", Seq(nir.Type.Nothing))
    )
  val throwUndefinedVal =
    nir.Val.Global(throwUndefined, nir.Type.Ptr)

  val throwOutOfBoundsTy =
    nir.Type.Function(Seq(nir.Type.Ptr, nir.Type.Int), nir.Type.Nothing)
  val throwOutOfBounds =
    nir.Global.Member(
      nir.Rt.Runtime.name,
      nir.Sig.Method("throwOutOfBounds", Seq(nir.Type.Int, nir.Type.Nothing))
    )
  val throwOutOfBoundsVal =
    nir.Val.Global(throwOutOfBounds, nir.Type.Ptr)

  val throwNoSuchMethodTy =
    nir.Type.Function(Seq(nir.Type.Ptr, nir.Type.Ptr), nir.Type.Nothing)
  val throwNoSuchMethod =
    nir.Global.Member(
      nir.Rt.Runtime.name,
      nir.Sig.Method("throwNoSuchMethod", Seq(nir.Rt.String, nir.Type.Nothing))
    )
  val throwNoSuchMethodVal =
    nir.Val.Global(throwNoSuchMethod, nir.Type.Ptr)

  val GC = nir.Global.Top("scala.scalanative.runtime.GC$")
  val GCSafepointName = GC.member(nir.Sig.Extern("scalanative_gc_safepoint"))
  val GCSafepoint = nir.Val.Global(GCSafepointName, nir.Type.Ptr)

  val GCSetMutatorThreadStateSig =
    nir.Type.Function(Seq(nir.Type.Int), nir.Type.Unit)
  val GCSetMutatorThreadState = nir.Val.Global(
    GC.member(nir.Sig.Extern("scalanative_gc_set_mutator_thread_state")),
    nir.Type.Ptr
  )

  val memsetSig =
    nir.Type.Function(
      Seq(nir.Type.Ptr, nir.Type.Int, nir.Type.Size),
      nir.Type.Ptr
    )
  val memsetName = extern("memset")
  val memset = nir.Val.Global(memsetName, nir.Type.Ptr)

  val RuntimeNull = nir.Type.Ref(nir.Global.Top("scala.runtime.Null$"))
  val RuntimeNothing = nir.Type.Ref(nir.Global.Top("scala.runtime.Nothing$"))

  val injects: Seq[nir.Defn] = {
    implicit val pos = nir.Position.NoPosition
    val buf = mutable.UnrolledBuffer.empty[nir.Defn]
    buf += nir.Defn.Declare(nir.Attrs.None, allocSmallName, allocSig)
    buf += nir.Defn.Declare(nir.Attrs.None, largeAllocName, allocSig)
    buf += nir.Defn.Declare(nir.Attrs.None, dyndispatchName, dyndispatchSig)
    buf += nir.Defn.Declare(nir.Attrs.None, throwName, throwSig)
    buf += nir.Defn.Declare(nir.Attrs(isExtern = true), memsetName, memsetSig)
    buf.toSeq
  }

  def depends(implicit platform: PlatformInfo): Seq[nir.Global] = {
    val buf = mutable.UnrolledBuffer.empty[nir.Global]
    buf ++= nir.Rt.PrimitiveTypes
    buf += nir.Rt.ClassName
    buf += nir.Rt.ClassIdName
    buf += nir.Rt.ClassTraitIdName
    buf += nir.Rt.ClassNameName
    buf += nir.Rt.ClassSizeName
    buf += nir.Rt.ClassIdRangeUntilName
    buf += nir.Rt.StringName
    buf += nir.Rt.StringValueName
    buf += nir.Rt.StringOffsetName
    buf += nir.Rt.StringCountName
    buf += nir.Rt.StringCachedHashCodeName
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
