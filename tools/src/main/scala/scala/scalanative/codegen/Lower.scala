package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.util.ScopedVar
import scalanative.nir._
import scalanative.linker.{
  Class,
  Trait,
  Ref,
  ScopeRef,
  ClassRef,
  TraitRef,
  FieldRef,
  MethodRef
}
import scalanative.interflow.UseDef.eliminateDeadCode

object Lower {

  def apply(defns: Seq[Defn])(implicit meta: Metadata): Seq[Defn] =
    (new Impl).onDefns(defns)

  private final class Impl(implicit meta: Metadata) extends Transform {
    import meta._

    implicit val linked = meta.linked

    val Object = linked.infos(Rt.Object.name).asInstanceOf[Class]

    // Type of the bare runtime type information struct.
    private val classRttiType =
      rtti(linked.infos(Global.Top("java.lang.Object"))).struct

    // Names of the fields of the java.lang.String in the memory layout order.
    private val stringFieldNames = {
      val node  = ClassRef.unapply(StringName).get
      val names = layout(node).entries.map(_.name)
      assert(names.length == 4, "java.lang.String is expected to have 4 fields")
      names
    }

    private val fresh         = new util.ScopedVar[Fresh]
    private val unwindHandler = new util.ScopedVar[Option[Local]]

    private val unreachableSlowPath    = mutable.Map.empty[Option[Local], Local]
    private val nullPointerSlowPath    = mutable.Map.empty[Option[Local], Local]
    private val divisionByZeroSlowPath = mutable.Map.empty[Option[Local], Local]
    private val classCastSlowPath      = mutable.Map.empty[Option[Local], Local]
    private val outOfBoundsSlowPath    = mutable.Map.empty[Option[Local], Local]
    private val noSuchMethodSlowPath   = mutable.Map.empty[Option[Local], Local]

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

      buf
    }

    override def onDefn(defn: Defn): Defn = defn match {
      case defn: Defn.Define =>
        val Type.Function(_, ty) = defn.ty
        ScopedVar.scoped(
          fresh := Fresh(defn.insts)
        ) {
          super.onDefn(defn)
        }
      case _ =>
        super.onDefn(defn)
    }

    override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
      val buf      = new nir.Buffer()(fresh)
      val handlers = new nir.Buffer()(fresh)
      import buf._

      buf += insts.head

      def newUnwindHandler(next: Next): Option[Local] = next match {
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
        case Inst.Let(n, Op.Var(ty), unwind) =>
          buf.let(n, Op.Stackalloc(ty, Val.Int(1)), unwind)
        case _ =>
          ()
      }

      insts.tail.foreach {
        case inst @ Inst.Let(n, op, unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)
          ) {
            genLet(buf, n, op)
          }

        case Inst.Throw(v, unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)
          ) {
            genThrow(buf, v)
          }

        case Inst.Unreachable(unwind) =>
          ScopedVar.scoped(
            unwindHandler := newUnwindHandler(unwind)
          ) {
            genUnreachable(buf)
          }

        case inst =>
          buf += inst
      }

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

      eliminateDeadCode(buf.toSeq.map(super.onInst))
    }

    override def onVal(value: Val): Val = value match {
      case Val.Global(ScopeRef(node), _) =>
        Val.Global(rtti(node).name, Type.Ptr)
      case Val.String(v) =>
        genStringVal(v)
      case Val.Unit =>
        unit
      case _ =>
        super.onVal(value)
    }

    def genNullPointerSlowPath(buf: Buffer): Unit = {
      nullPointerSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(throwNullPointerTy,
                     throwNullPointerVal,
                     Seq(Val.Null),
                     unwind)
            buf.unreachable(Next.None)
          }
      }
    }

    def genDivisionByZeroSlowPath(buf: Buffer): Unit = {
      divisionByZeroSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            buf.label(slowPath)
            buf.call(throwDivisionByZeroTy,
                     throwDivisionByZeroVal,
                     Seq(Val.Null),
                     unwind)
            buf.unreachable(Next.None)
          }
      }
    }

    def genClassCastSlowPath(buf: Buffer): Unit = {
      classCastSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val obj  = Val.Local(fresh(), Type.Ptr)
            val toty = Val.Local(fresh(), Type.Ptr)

            buf.label(slowPath, Seq(obj, toty))
            val fromty = buf.let(Op.Load(Type.Ptr, obj), unwind)
            buf.call(throwClassCastTy,
                     throwClassCastVal,
                     Seq(Val.Null, fromty, toty),
                     unwind)
            buf.unreachable(Next.None)
          }
      }
    }

    def genUnreachableSlowPath(buf: Buffer): Unit = {
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

    def genOutOfBoundsSlowPath(buf: Buffer): Unit = {
      outOfBoundsSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val idx = Val.Local(fresh(), Type.Int)

            buf.label(slowPath, Seq(idx))
            buf.call(throwOutOfBoundsTy,
                     throwOutOfBoundsVal,
                     Seq(Val.Null, idx),
                     unwind)
            buf.unreachable(Next.None)
          }
      }
    }

    def genNoSuchMethodSlowPath(buf: Buffer): Unit = {
      noSuchMethodSlowPath.toSeq.sortBy(_._2.id).foreach {
        case (slowPathUnwindHandler, slowPath) =>
          ScopedVar.scoped(
            unwindHandler := slowPathUnwindHandler
          ) {
            val sig = Val.Local(fresh(), Type.Ptr)

            buf.label(slowPath, Seq(sig))
            buf.call(throwNoSuchMethodTy,
                     throwNoSuchMethodVal,
                     Seq(Val.Null, sig),
                     unwind)
            buf.unreachable(Next.None)
          }
      }
    }

    def genLet(buf: Buffer, n: Local, op: Op): Unit = op.resty match {
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

    def genThrow(buf: Buffer, exc: Val) = {
      genGuardNotNull(buf, exc)
      genOp(buf, fresh(), Op.Call(throwSig, throw_, Seq(exc)))
      buf.unreachable(Next.None)
    }

    def genUnreachable(buf: Buffer) = {
      val failL = unreachableSlowPath.getOrElseUpdate(unwindHandler, fresh())

      buf.jump(Next(failL))
    }

    def genOp(buf: Buffer, n: Local, op: Op): Unit = op match {
      case op: Op.Fieldload =>
        genFieldloadOp(buf, n, op)
      case op: Op.Fieldstore =>
        genFieldstoreOp(buf, n, op)
      case op: Op.Method =>
        genMethodOp(buf, n, op)
      case op: Op.Dynmethod =>
        genDynmethodOp(buf, n, op)
      case op: Op.Is =>
        genIsOp(buf, n, op)
      case op: Op.As =>
        genAsOp(buf, n, op)
      case op: Op.Sizeof =>
        genSizeofOp(buf, n, op)
      case op: Op.Classalloc =>
        genClassallocOp(buf, n, op)
      case op: Op.Conv =>
        genConvOp(buf, n, op)
      case op: Op.Bin =>
        genBinOp(buf, n, op)
      case op: Op.Box =>
        genBoxOp(buf, n, op)
      case op: Op.Unbox =>
        genUnboxOp(buf, n, op)
      case op: Op.Module =>
        genModuleOp(buf, n, op)
      case op: Op.Var =>
        ()
      case Op.Varload(Val.Local(slot, Type.Var(ty))) =>
        buf.let(n, Op.Load(ty, Val.Local(slot, Type.Ptr)), unwind)
      case Op.Varstore(Val.Local(slot, Type.Var(ty)), value) =>
        buf.let(n, Op.Store(ty, Val.Local(slot, Type.Ptr), value), unwind)
      case op: Op.Arrayalloc =>
        genArrayallocOp(buf, n, op)
      case op: Op.Arrayload =>
        genArrayloadOp(buf, n, op)
      case op: Op.Arraystore =>
        genArraystoreOp(buf, n, op)
      case op: Op.Arraylength =>
        genArraylengthOp(buf, n, op)
      case _ =>
        buf.let(n, op, unwind)
    }

    def genGuardNotNull(buf: Buffer, obj: Val): Unit = obj.ty match {
      case ty: Type.RefKind if !ty.isNullable =>
        ()

      case _ =>
        import buf._

        val notNullL = fresh()
        val isNullL =
          nullPointerSlowPath.getOrElseUpdate(unwindHandler, fresh())

        val isNull = comp(Comp.Ine, obj.ty, obj, Val.Null, unwind)
        branch(isNull, Next(notNullL), Next(isNullL))
        label(notNullL)
    }

    def genGuardInBounds(buf: Buffer, idx: Val, len: Val): Unit = {
      import buf._

      val inBoundsL = fresh()
      val outOfBoundsL =
        outOfBoundsSlowPath.getOrElseUpdate(unwindHandler, fresh())

      val gt0      = comp(Comp.Sge, Type.Int, idx, Val.Int(0), unwind)
      val ltLen    = comp(Comp.Slt, Type.Int, idx, len, unwind)
      val inBounds = bin(Bin.And, Type.Bool, gt0, ltLen, unwind)
      branch(inBounds, Next(inBoundsL), Next.Label(outOfBoundsL, Seq(idx)))
      label(inBoundsL)
    }

    def genFieldElemOp(buf: Buffer, obj: Val, name: Global) = {
      import buf._

      val FieldRef(cls: Class, fld) = name

      val layout = meta.layout(cls)
      val ty     = layout.struct
      val index  = layout.index(fld)

      genGuardNotNull(buf, obj)
      elem(ty, obj, Seq(Val.Int(0), Val.Int(index)), unwind)
    }

    def genFieldloadOp(buf: Buffer, n: Local, op: Op.Fieldload) = {
      val Op.Fieldload(ty, obj, name) = op

      val elem = genFieldElemOp(buf, obj, name)
      buf.let(n, Op.Load(ty, elem), unwind)
    }

    def genFieldstoreOp(buf: Buffer, n: Local, op: Op.Fieldstore) = {
      val Op.Fieldstore(ty, obj, name, value) = op

      val elem = genFieldElemOp(buf, obj, name)
      buf.let(n, Op.Store(ty, elem, value), unwind)
    }

    def genMethodOp(buf: Buffer, n: Local, op: Op.Method) = {
      import buf._

      val Op.Method(obj, sig) = op

      def genClassVirtualLookup(cls: Class): Unit = {
        val vindex  = vtable(cls).index(sig)
        val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
        val methptrptr = let(
          Op.Elem(rtti(cls).struct,
                  typeptr,
                  Seq(Val.Int(0), meta.vtableIndex, Val.Int(vindex))),
          unwind)

        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      def genTraitVirtualLookup(trt: Trait): Unit = {
        val sigid   = dispatchTable.traitSigIds(sig)
        val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
        val idptr =
          let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(1))), unwind)
        val id = let(Op.Load(Type.Int, idptr), unwind)
        val rowptr = let(
          Op.Elem(Type.Ptr,
                  dispatchTable.dispatchVal,
                  Seq(Val.Int(dispatchTable.dispatchOffset(sigid)))),
          unwind)
        val methptrptr =
          let(Op.Elem(Type.Ptr, rowptr, Seq(id)), unwind)
        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      def genMethodLookup(): Unit = {
        val targets = obj.ty match {
          case ScopeRef(scope) =>
            scope.targets(sig).toSeq
          case _ =>
            Seq()
        }

        targets match {
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
            }
        }
      }

      genGuardNotNull(buf, obj)
      genMethodLookup()
    }

    def genDynmethodOp(buf: Buffer, n: Local, op: Op.Dynmethod): Unit = {
      import buf._

      val Op.Dynmethod(obj, sig) = op

      def throwIfNull(value: Val) = {
        val notNullL = fresh()
        val noSuchMethodL =
          noSuchMethodSlowPath.getOrElseUpdate(unwindHandler, fresh())

        val condNull = comp(Comp.Ine, Type.Ptr, value, Val.Null, unwind)
        branch(condNull,
               Next(notNullL),
               Next.Label(noSuchMethodL, Seq(Val.String(sig.mangle))))
        label(notNullL)
      }

      def genReflectiveLookup(): Val = {
        val methodIndex =
          meta.linked.dynsigs.zipWithIndex.find(_._1 == sig).get._2

        // Load the type information pointer
        val typeptr = load(Type.Ptr, obj, unwind)
        // Load the dynamic hash map for given type, make sure it's not null
        val mapelem = elem(classRttiType,
                           typeptr,
                           Seq(Val.Int(0), meta.dynmapIndex),
                           unwind)
        val mapptr = load(Type.Ptr, mapelem, unwind)
        // If hash map is not null, it has to contain at least one entry
        throwIfNull(mapptr)
        // Perform dynamic dispatch via dyndispatch helper
        val methptrptr = call(dyndispatchSig,
                              dyndispatch,
                              Seq(mapptr, Val.Int(methodIndex)),
                              unwind)
        // Hash map lookup can still not contain given signature
        throwIfNull(methptrptr)
        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      genGuardNotNull(buf, obj)
      genReflectiveLookup()
    }

    def genIsOp(buf: Buffer, n: Local, op: Op.Is): Unit = {
      import buf._

      op match {
        case Op.Is(_, Val.Null | Val.Zero(_)) =>
          let(n, Op.Copy(Val.False), unwind)

        case Op.Is(ty, obj) =>
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

    def genIsOp(buf: Buffer, ty: Type, obj: Val): Val = {
      import buf._

      ty match {
        case ClassRef(cls) if meta.ranges(cls).length == 1 =>
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          let(Op.Comp(Comp.Ieq, Type.Ptr, typeptr, rtti(cls).const), unwind)

        case ClassRef(cls) =>
          val range   = meta.ranges(cls)
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          val idptr =
            let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))), unwind)
          val id = let(Op.Load(Type.Int, idptr), unwind)
          val ge =
            let(Op.Comp(Comp.Sle, Type.Int, Val.Int(range.start), id), unwind)
          val le =
            let(Op.Comp(Comp.Sle, Type.Int, id, Val.Int(range.end)), unwind)
          let(Op.Bin(Bin.And, Type.Bool, ge, le), unwind)

        case TraitRef(trt) =>
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          val idptr =
            let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))), unwind)
          val id = let(Op.Load(Type.Int, idptr), unwind)
          val boolptr = let(
            Op.Elem(hasTraitTables.classHasTraitTy,
                    hasTraitTables.classHasTraitVal,
                    Seq(Val.Int(0), id, Val.Int(meta.ids(trt)))),
            unwind)
          let(Op.Load(Type.Bool, boolptr), unwind)

        case _ =>
          util.unsupported(s"is[$ty] $obj")
      }
    }

    def genAsOp(buf: Buffer, n: Local, op: Op.As): Unit = {
      import buf._

      op match {
        case Op.As(ty: Type.RefKind, v) if v.ty == Type.Null =>
          let(n, Op.Copy(Val.Null), unwind)

        case Op.As(ty: Type.RefKind, v) if v.ty.isInstanceOf[Type.RefKind] =>
          val checkIfIsInstanceOfL, castL = fresh()
          val failL                       = classCastSlowPath.getOrElseUpdate(unwindHandler, fresh())

          val isNull = comp(Comp.Ieq, v.ty, v, Val.Null, unwind)
          branch(isNull, Next(castL), Next(checkIfIsInstanceOfL))

          label(checkIfIsInstanceOfL)
          val isInstanceOf = genIsOp(buf, ty, v)
          val toTy         = Val.Global(rtti(linked.infos(ty.className)).name, Type.Ptr)
          branch(isInstanceOf, Next(castL), Next.Label(failL, Seq(v, toTy)))

          label(castL)
          let(n, Op.Conv(Conv.Bitcast, ty, v), unwind)

        case Op.As(to, v) =>
          util.unsupported(s"can't cast from ${v.ty} to $to")
      }
    }

    def genSizeofOp(buf: Buffer, n: Local, op: Op.Sizeof): Unit = {
      val Op.Sizeof(ty) = op

      buf.let(n, Op.Copy(Val.Long(MemoryLayout.sizeOf(ty))), unwind)
    }

    def genClassallocOp(buf: Buffer, n: Local, op: Op.Classalloc): Unit = {
      val Op.Classalloc(ClassRef(cls)) = op

      val size = MemoryLayout.sizeOf(layout(cls).struct)
      val allocMethod =
        if (size < LARGE_OBJECT_MIN_SIZE) alloc else largeAlloc

      buf.let(
        n,
        Op.Call(allocSig, allocMethod, Seq(rtti(cls).const, Val.Long(size))),
        unwind)
    }

    def genConvOp(buf: Buffer, n: Local, op: Op.Conv): Unit = {
      import buf._

      op match {
        // Fptosi is undefined behaviour on LLVM if the resulting
        // value doesn't fit the MIN...MAX range for given integer type.
        // We insert range checks and return MIN_VALUE for floating values
        // that are numerically less than or equal to MIN_VALUE and MAX_VALUE
        // for the ones which are greate or equal to MAX_VALUE. Additionally,
        // NaNs are converted to 0.
        case Op.Conv(Conv.Fptosi, toty, v) =>
          val (imin, imax, fmin, fmax) = toty match {
            case Type.Int =>
              val min = java.lang.Integer.MIN_VALUE
              val max = java.lang.Integer.MAX_VALUE
              v.ty match {
                case Type.Float =>
                  (Val.Int(min),
                   Val.Int(max),
                   Val.Float(min.toFloat),
                   Val.Float(max.toFloat))
                case Type.Double =>
                  (Val.Int(min),
                   Val.Int(max),
                   Val.Double(min.toDouble),
                   Val.Double(max.toDouble))
                case _ =>
                  util.unreachable
              }
            case Type.Long =>
              val min = java.lang.Long.MIN_VALUE
              val max = java.lang.Long.MAX_VALUE
              v.ty match {
                case Type.Float =>
                  (Val.Long(min),
                   Val.Long(max),
                   Val.Float(min.toFloat),
                   Val.Float(max.toFloat))
                case Type.Double =>
                  (Val.Long(min),
                   Val.Long(max),
                   Val.Double(min.toDouble),
                   Val.Double(max.toDouble))
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

        case _ =>
          let(n, op, unwind)
      }
    }

    def genBinOp(buf: Buffer, n: Local, op: Op.Bin): Unit = {
      import buf._

      // LLVM's division by zero is undefined behaviour. We guard
      // the case when the divisor is zero and fail gracefully
      // by throwing an arithmetic exception.
      def checkDivisionByZero(op: Op.Bin): Unit = {
        val Op.Bin(bin, ty: Type.I, dividend, divisor) = op

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
        val Op.Bin(bin, ty: Type.I, dividend, divisor) = op

        val mayOverflowL, noOverflowL, didOverflowL, resultL = fresh()

        val minus1 = ty match {
          case Type.Int  => Val.Int(-1)
          case Type.Long => Val.Long(-1L)
          case _         => util.unreachable
        }
        val minValue = ty match {
          case Type.Int  => Val.Int(java.lang.Integer.MIN_VALUE)
          case Type.Long => Val.Long(java.lang.Long.MIN_VALUE)
          case _         => util.unreachable
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
        val Op.Bin(_, ty: Type.I, _, r) = op
        val mask = ty match {
          case Type.Int  => Val.Int(31)
          case Type.Long => Val.Int(63)
          case _         => util.unreachable
        }
        val masked = bin(Bin.And, ty, r, mask, unwind)
        let(n, op.copy(r = masked), unwind)
      }

      op match {
        case op @ Op.Bin(bin @ (Bin.Srem | Bin.Urem | Bin.Sdiv | Bin.Udiv),
                         ty: Type.I,
                         l,
                         r) =>
          checkDivisionByZero(op)

        case op @ Op.Bin(bin @ (Bin.Shl | Bin.Lshr | Bin.Ashr),
                         ty: Type.I,
                         l,
                         r) =>
          maskShift(op)

        case op =>
          let(n, op, unwind)
      }
    }

    def genBoxOp(buf: Buffer, n: Local, op: Op.Box): Unit = {
      val Op.Box(ty, from) = op

      val methodName = BoxTo(ty)
      val moduleName = methodName.top

      val boxTy =
        Type.Function(Seq(Type.Ref(moduleName), Type.unbox(ty)), ty)

      buf.let(
        n,
        Op.Call(boxTy, Val.Global(methodName, Type.Ptr), Seq(Val.Null, from)),
        unwind)
    }

    def genUnboxOp(buf: Buffer, n: Local, op: Op.Unbox): Unit = {
      val Op.Unbox(ty, from) = op

      val methodName = UnboxTo(ty)
      val moduleName = methodName.top

      val unboxTy =
        Type.Function(Seq(Type.Ref(moduleName), ty), Type.unbox(ty))

      buf.let(
        n,
        Op.Call(unboxTy, Val.Global(methodName, Type.Ptr), Seq(Val.Null, from)),
        unwind)
    }

    def genModuleOp(buf: Buffer, n: Local, op: Op.Module) = {
      val Op.Module(name) = op

      meta.linked.infos(name) match {
        case cls: Class if cls.isConstantModule =>
          val instance = name.member(Sig.Generated("instance"))
          buf.let(n, Op.Copy(Val.Global(instance, Type.Ptr)), unwind)

        case _ =>
          val loadSig = Type.Function(Seq(), Type.Ref(name))
          val load    = Val.Global(name.member(Sig.Generated("load")), Type.Ptr)

          buf.let(n, Op.Call(loadSig, load, Seq()), unwind)
      }
    }

    def genArrayallocOp(buf: Buffer, n: Local, op: Op.Arrayalloc): Unit = {
      val Op.Arrayalloc(ty, init) = op
      init match {
        case len if len.ty == Type.Int =>
          val sig  = arrayAllocSig.getOrElse(ty, arrayAllocSig(Rt.Object))
          val func = arrayAlloc.getOrElse(ty, arrayAlloc(Rt.Object))
          buf.let(n,
                  Op.Call(sig, Val.Global(func, Type.Ptr), Seq(Val.Null, len)),
                  unwind)
        case arrval: Val.ArrayValue =>
          val sig  = arraySnapshotSig.getOrElse(ty, arraySnapshotSig(Rt.Object))
          val func = arraySnapshot.getOrElse(ty, arraySnapshot(Rt.Object))
          val len  = Val.Int(arrval.values.length)
          val init = Val.Const(arrval)
          buf.let(
            n,
            Op.Call(sig, Val.Global(func, Type.Ptr), Seq(Val.Null, len, init)),
            unwind)
      }
    }

    def genArrayloadOp(buf: Buffer, n: Local, op: Op.Arrayload): Unit = {
      val Op.Arrayload(ty, arr, idx) = op

      val len = fresh()

      genArraylengthOp(buf, len, Op.Arraylength(arr))
      genGuardInBounds(buf, idx, Val.Local(len, Type.Int))

      val arrTy = Type.StructValue(
        Seq(Type.Ptr, Type.Int, Type.Int, Type.ArrayValue(ty, 0)))
      val elemPath = Seq(Val.Int(0), Val.Int(3), idx)
      val elemPtr  = buf.elem(arrTy, arr, elemPath, unwind)
      buf.let(n, Op.Load(ty, elemPtr), unwind)
    }

    def genArraystoreOp(buf: Buffer, n: Local, op: Op.Arraystore): Unit = {
      val Op.Arraystore(ty, arr, idx, value) = op

      val len = fresh()

      genArraylengthOp(buf, len, Op.Arraylength(arr))
      genGuardInBounds(buf, idx, Val.Local(len, Type.Int))

      val arrTy = Type.StructValue(
        Seq(Type.Ptr, Type.Int, Type.Int, Type.ArrayValue(ty, 0)))
      val elemPtr =
        buf.elem(arrTy, arr, Seq(Val.Int(0), Val.Int(3), idx), unwind)
      buf.let(n, Op.Store(ty, elemPtr, value), unwind)
    }

    def genArraylengthOp(buf: Buffer, n: Local, op: Op.Arraylength): Unit = {
      val Op.Arraylength(arr) = op

      val sig  = arrayLengthSig
      val func = arrayLength

      genGuardNotNull(buf, arr)
      val arrTy  = Type.StructValue(Seq(Type.Ptr, Type.Int))
      val lenPtr = buf.elem(arrTy, arr, Seq(Val.Int(0), Val.Int(1)), unwind)
      buf.let(n, Op.Load(Type.Int, lenPtr), unwind)
    }

    def genStringVal(value: String): Val = {
      val StringCls    = ClassRef.unapply(StringName).get
      val CharArrayCls = ClassRef.unapply(CharArrayName).get

      val chars       = value.toCharArray
      val charsLength = Val.Int(chars.length)
      val charsConst = Val.Const(
        Val.StructValue(
          Seq(
            rtti(CharArrayCls).const,
            charsLength,
            Val.Int(0), // padding to get next field aligned properly
            Val.ArrayValue(Type.Char, chars.map(Val.Char))
          )
        ))

      val fieldValues = stringFieldNames.map {
        case StringValueName          => charsConst
        case StringOffsetName         => Val.Int(0)
        case StringCountName          => charsLength
        case StringCachedHashCodeName => Val.Int(stringHashCode(value))
        case _                        => util.unreachable
      }

      Val.Const(Val.StructValue(rtti(StringCls).const +: fieldValues))
    }
  }

  // Update java.lang.String::hashCode whenever you change this method.
  def stringHashCode(s: String): Int =
    if (s.length == 0) {
      0
    } else {
      val value = s.toCharArray
      var hash  = 0
      var i     = 0
      while (i < value.length) {
        hash = value(i) + ((hash << 5) - hash)
        i += 1
      }
      hash
    }

  val LARGE_OBJECT_MIN_SIZE = 8192

  val allocSig = Type.Function(Seq(Type.Ptr, Type.Long), Type.Ptr)

  val allocSmallName = extern("scalanative_alloc_small")
  val alloc          = Val.Global(allocSmallName, allocSig)

  val largeAllocName = extern("scalanative_alloc_large")
  val largeAlloc     = Val.Global(largeAllocName, allocSig)

  val dyndispatchName = extern("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.Int), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    Global.Member(excptnGlobal, Sig.Ctor(Seq(nir.Rt.String)))

  val excInitSig = Type.Function(
    Seq(Type.Ref(excptnGlobal), Type.Ref(Global.Top("java.lang.String"))),
    Type.Unit)
  val excInit = Val.Global(excptnInitGlobal, Type.Ptr)

  val StringName       = Rt.String.name
  val StringValueName  = StringName.member(Sig.Field("value"))
  val StringOffsetName = StringName.member(Sig.Field("offset"))
  val StringCountName  = StringName.member(Sig.Field("count"))
  val StringCachedHashCodeName =
    StringName.member(Sig.Field("cachedHashCode"))

  val CharArrayName =
    Global.Top("scala.scalanative.runtime.CharArray")

  val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  val RuntimeBoxes = Global.Top("scala.scalanative.runtime.Boxes$")

  val BoxTo: Map[Type, Global] = Type.boxClasses.map { cls =>
    val name   = cls.asInstanceOf[Global.Top].id
    val boxty  = Type.Ref(Global.Top(name))
    val module = if (name.startsWith("java.")) BoxesRunTime else RuntimeBoxes
    val id     = "boxTo" + name.split("\\.").last
    val tys    = Seq(nir.Type.unbox(boxty), boxty)
    val meth   = module.member(Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  val UnboxTo: Map[Type, Global] = Type.boxClasses.map { cls =>
    val name   = cls.asInstanceOf[Global.Top].id
    val boxty  = Type.Ref(Global.Top(name))
    val module = if (name.startsWith("java.")) BoxesRunTime else RuntimeBoxes
    val id = {
      val last = name.split("\\.").last
      val suffix =
        if (last == "Integer") "Int"
        else if (last == "Character") "Char"
        else last
      "unboxTo" + suffix
    }
    val tys  = Seq(nir.Rt.Object, nir.Type.unbox(boxty))
    val meth = module.member(Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  private def extern(id: String): Global =
    Global.Member(Global.Top("__"), Sig.Extern(id))

  val unitName     = Global.Top("scala.scalanative.runtime.BoxedUnit$")
  val unitInstance = unitName.member(Sig.Generated("instance"))
  val unit         = Val.Global(unitInstance, Type.Ptr)

  val throwName = extern("scalanative_throw")
  val throwSig  = Type.Function(Seq(Type.Ptr), Type.Nothing)
  val throw_    = Val.Global(throwName, Type.Ptr)

  val arrayAlloc = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      val arrcls         = Type.Ref(arrname)
      ty -> Global.Member(Global.Top(id + "$"),
                          Sig.Method("alloc", Seq(Type.Int, arrcls)))
  }.toMap
  val arrayAllocSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      ty -> Type.Function(Seq(Type.Ref(Global.Top(id + "$")), Type.Int),
                          Type.Ref(arrname))
  }.toMap
  val arraySnapshot = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      val arrcls         = Type.Ref(arrname)
      ty -> Global.Member(
        Global.Top(id + "$"),
        Sig.Method("snapshot", Seq(Type.Int, Type.Ptr, arrcls)))
  }.toMap
  val arraySnapshotSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      ty -> Type.Function(
        Seq(Type.Ref(Global.Top(id + "$")), Type.Int, Type.Ptr),
        Type.Ref(arrname))
  }.toMap
  val arrayApplyGeneric = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(arrname,
                          Sig.Method("apply", Seq(Type.Int, nir.Rt.Object)))
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
        Sig.Method("update", Seq(Type.Int, nir.Rt.Object, Type.Unit)))
  }
  val arrayUpdate = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(arrname,
                          Sig.Method("update", Seq(Type.Int, ty, Type.Unit)))
  }.toMap
  val arrayUpdateSig = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Type.Function(Seq(Type.Ref(arrname), Type.Int, ty), Type.Unit)
  }.toMap
  val arrayLength =
    Global.Member(Global.Top("scala.scalanative.runtime.Array"),
                  Sig.Method("length", Seq(Type.Int)))
  val arrayLengthSig =
    Type.Function(Seq(Type.Ref(Global.Top("scala.scalanative.runtime.Array"))),
                  Type.Int)

  val throwDivisionByZeroTy =
    Type.Function(Seq(Rt.Runtime), Type.Nothing)
  val throwDivisionByZero =
    Global.Member(Rt.Runtime.name,
                  Sig.Method("throwDivisionByZero", Seq(Type.Nothing)))
  val throwDivisionByZeroVal =
    Val.Global(throwDivisionByZero, Type.Ptr)

  val throwClassCastTy =
    Type.Function(Seq(Rt.Runtime, Type.Ptr, Type.Ptr), Type.Nothing)
  val throwClassCast =
    Global.Member(
      Rt.Runtime.name,
      Sig.Method("throwClassCast", Seq(Type.Ptr, Type.Ptr, Type.Nothing)))
  val throwClassCastVal =
    Val.Global(throwClassCast, Type.Ptr)

  val throwNullPointerTy =
    Type.Function(Seq(Rt.Runtime), Type.Nothing)
  val throwNullPointer =
    Global.Member(Rt.Runtime.name,
                  Sig.Method("throwNullPointer", Seq(Type.Nothing)))
  val throwNullPointerVal =
    Val.Global(throwNullPointer, Type.Ptr)

  val throwUndefinedTy =
    Type.Function(Seq(Type.Ptr), Type.Nothing)
  val throwUndefined =
    Global.Member(Rt.Runtime.name,
                  Sig.Method("throwUndefined", Seq(Type.Nothing)))
  val throwUndefinedVal =
    Val.Global(throwUndefined, Type.Ptr)

  val throwOutOfBoundsTy =
    Type.Function(Seq(Type.Ptr, Type.Int), Type.Nothing)
  val throwOutOfBounds =
    Global.Member(Rt.Runtime.name,
                  Sig.Method("throwOutOfBounds", Seq(Type.Int, Type.Nothing)))
  val throwOutOfBoundsVal =
    Val.Global(throwOutOfBounds, Type.Ptr)

  val throwNoSuchMethodTy =
    Type.Function(Seq(Type.Ptr, Type.Ptr), Type.Nothing)
  val throwNoSuchMethod =
    Global.Member(Rt.Runtime.name,
                  Sig.Method("throwNoSuchMethod", Seq(Rt.String, Type.Nothing)))
  val throwNoSuchMethodVal =
    Val.Global(throwNoSuchMethod, Type.Ptr)

  val RuntimeNull    = Type.Ref(Global.Top("scala.runtime.Null$"))
  val RuntimeNothing = Type.Ref(Global.Top("scala.runtime.Nothing$"))

  val injects: Seq[Defn] = {
    val buf = mutable.UnrolledBuffer.empty[Defn]
    buf += Defn.Declare(Attrs.None, allocSmallName, allocSig)
    buf += Defn.Declare(Attrs.None, largeAllocName, allocSig)
    buf += Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig)
    buf += Defn.Declare(Attrs.None, throwName, throwSig)
    buf
  }

  val depends: Seq[Global] = {
    val buf = mutable.UnrolledBuffer.empty[Global]
    buf += StringName
    buf += StringValueName
    buf += StringOffsetName
    buf += StringCountName
    buf += StringCachedHashCodeName
    buf += CharArrayName
    buf += BoxesRunTime
    buf += RuntimeBoxes
    buf += unitName
    buf ++= BoxTo.values
    buf ++= UnboxTo.values
    buf += arrayLength
    buf ++= arrayAlloc.values
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
    buf
  }
}
