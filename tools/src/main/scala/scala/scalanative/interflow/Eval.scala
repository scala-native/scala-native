package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._
import scalanative.codegen.MemoryLayout
import scalanative.util.{unreachable, And}

trait Eval { self: Interflow =>
  def run(insts: Array[Inst], offsets: Map[Local, Int], from: Local)(implicit
      state: State
  ): Inst.Cf = {
    import state.{materialize, delay}

    var pc = offsets(from) + 1

    while (true) {
      val inst = insts(pc)
      implicit val pos: Position = inst.pos
      def bailOut =
        throw BailOut("can't eval inst: " + inst.show)
      inst match {
        case _: Inst.Label =>
          unreachable
        case Inst.Let(local, op, unwind) =>
          if (unwind ne Next.None) {
            throw BailOut("try-catch")
          }
          val value = eval(op)
          if (value.ty == Type.Nothing) {
            return Inst.Unreachable(unwind)(inst.pos)
          } else {
            val ty = value match {
              case InstanceRef(ty) => ty
              case _               => value.ty
            }
            val shortUnitValue =
              if (ty == Type.Unit) Val.Unit else value
            state.storeLocal(local, shortUnitValue)
            pc += 1
          }
        case Inst.Ret(v) =>
          return Inst.Ret(eval(v))
        case Inst.Jump(Next.Label(target, args)) =>
          val evalArgs = args.map(eval)
          val next = Next.Label(target, evalArgs)
          return Inst.Jump(next)
        case Inst.If(
              cond,
              Next.Label(thenTarget, thenArgs),
              Next.Label(elseTarget, elseArgs)
            ) =>
          def thenNext =
            Next.Label(thenTarget, thenArgs.map(eval))
          def elseNext =
            Next.Label(elseTarget, elseArgs.map(eval))
          val next = eval(cond) match {
            case Val.True =>
              return Inst.Jump(thenNext)
            case Val.False =>
              return Inst.Jump(elseNext)
            case cond =>
              return Inst.If(materialize(cond), thenNext, elseNext)
          }
        case Inst.Switch(
              scrut,
              Next.Label(defaultTarget, defaultArgs),
              cases
            ) =>
          def defaultNext =
            Next.Label(defaultTarget, defaultArgs.map(eval))
          eval(scrut) match {
            case value if value.isCanonical =>
              val next = cases
                .collectFirst {
                  case Next.Case(caseValue, Next.Label(caseTarget, caseArgs))
                      if caseValue == value =>
                    val evalArgs = caseArgs.map(eval)
                    val next = Next.Label(caseTarget, evalArgs)
                    next
                }
                .getOrElse(defaultNext)
              return Inst.Jump(next)
            case scrut =>
              return Inst.Switch(materialize(scrut), defaultNext, cases)
          }
        case Inst.Throw(v, unwind) =>
          if (unwind ne Next.None) {
            throw BailOut("try-catch")
          }
          return Inst.Throw(eval(v), Next.None)
        case Inst.Unreachable(unwind) =>
          if (unwind ne Next.None) {
            throw BailOut("try-catch")
          }
          return Inst.Unreachable(Next.None)
        case _ =>
          bailOut
      }
    }

    unreachable
  }

  def eval(
      op: Op
  )(implicit state: State, linked: linker.Result, origPos: Position): Val = {
    import state.{emit, materialize, delay}
    def bailOut =
      throw BailOut("can't eval op: " + op.show)
    op match {
      case Op.Call(sig, meth, args) =>
        val emeth = eval(meth)

        def nonIntrinsic = {
          val eargs = args.map(eval)
          val argtys = eargs.map {
            case VirtualRef(_, cls, _) =>
              cls.ty
            case DelayedRef(op) =>
              op.resty
            case value =>
              value.ty
          }

          val (dsig, dtarget) = emeth match {
            case Val.Global(name, _) =>
              visitDuplicate(name, argtys)
                .map { defn => (defn.ty, Val.Global(defn.name, Type.Ptr)) }
                .getOrElse {
                  visitRoot(name)
                  (sig, emeth)
                }
            case _ =>
              (sig, emeth)
          }

          def fallback = {
            val mtarget = materialize(dtarget)
            val margs = adapt(eargs, dsig).map(materialize)

            emit(Op.Call(dsig, mtarget, margs))
          }

          dtarget match {
            case Val.Global(name, _) if shallInline(name, eargs) =>
              `inline`(name, eargs)
            case DelayedRef(op: Op.Method) if shallPolyInline(op, eargs) =>
              polyInline(op, eargs)
            case _ =>
              fallback
          }
        }

        emeth match {
          case Val.Global(name, _) if intrinsics.contains(name) =>
            intrinsic(sig, name, args).getOrElse {
              nonIntrinsic
            }
          case _ =>
            nonIntrinsic
        }
      case op @ Op.Load(ty, ptr, syncAttrs) =>
        emit(
          op.copy(ptr = materialize(eval(ptr)))
        )
      case op @ Op.Store(ty, ptr, value, syncAttrs) =>
        emit(
          op.copy(
            ptr = materialize(eval(ptr)),
            value = materialize(eval(value))
          )
        )
      case Op.Elem(ty, ptr, indexes) =>
        delay(Op.Elem(ty, eval(ptr), indexes.map(eval)))
      case Op.Extract(aggr, indexes) =>
        delay(Op.Extract(eval(aggr), indexes))
      case Op.Insert(aggr, value, indexes) =>
        delay(Op.Insert(eval(aggr), eval(value), indexes))
      case Op.Stackalloc(ty, n) =>
        emit(Op.Stackalloc(ty, materialize(eval(n))))
      case op @ Op.Bin(bin, ty, l, r) =>
        (eval(l), eval(r)) match {
          case (l, r) if l.isCanonical && r.isCanonical =>
            eval(bin, ty, l, r)
          case (l, r) =>
            if (l.isCanonical && op.isCommutative) {
              combine(bin, ty, r, l)
            } else {
              combine(bin, ty, l, r)
            }
        }
      case Op.Comp(comp, ty, l, r) =>
        (comp, eval(l), eval(r)) match {
          case (_, l, r) if l.isCanonical && r.isCanonical =>
            eval(comp, ty, l, r)
          case (_, l, r) =>
            if (l.isCanonical && op.isCommutative) {
              combine(comp, ty, l, r)
            } else {
              combine(comp, ty, r, l)
            }
        }
      case Op.Conv(conv, ty, value) =>
        eval(value) match {
          case value if value.isCanonical =>
            eval(conv, ty, value)
          case value =>
            combine(conv, ty, value)
        }
      case Op.Classalloc(ClassRef(cls)) =>
        Val.Virtual(state.allocClass(cls))
      case Op.Fieldload(ty, rawObj, name @ FieldRef(cls, fld)) =>
        eval(rawObj) match {
          case VirtualRef(_, _, values) =>
            values(fld.index)
          case DelayedRef(op: Op.Box) =>
            val name = op.ty.asInstanceOf[Type.RefKind].className
            eval(Op.Unbox(Type.Ref(name), rawObj))
          case obj =>
            val objty = obj match {
              case InstanceRef(ty) => ty
              case _               => obj.ty
            }
            objty match {
              case refty: Type.RefKind
                  if nir.Type.boxClasses.contains(refty.className)
                    && !refty.isNullable =>
                eval(Op.Unbox(Type.Ref(refty.className), rawObj))
              case _ =>
                emit(Op.Fieldload(ty, materialize(obj), name))
            }
        }
      case Op.Fieldstore(ty, obj, name @ FieldRef(cls, fld), value) =>
        eval(obj) match {
          case VirtualRef(_, _, values) =>
            values(fld.index) = eval(value)
            Val.Unit
          case obj =>
            emit(
              Op
                .Fieldstore(
                  ty,
                  materialize(obj),
                  name,
                  materialize(eval(value))
                )
            )
        }

      case Op.Field(rawObj, name) =>
        val obj = eval(rawObj)
        visitRoot(name)
        delay(Op.Field(materialize(obj), name))

      case Op.Method(rawObj, sig) =>
        val obj = eval(rawObj)
        val objty = {
          /* If method is not virtual (eg. constructor) we need to ensure that
           * we would fetch for expected type targets (rawObj) instead of real (evaluated) type
           * It might result in calling wrong method and lead to infinite loops, eg. issue #1909
           */
          val realType = obj match {
            case InstanceRef(ty) => ty
            case _               => obj.ty
          }
          val expectedType = rawObj.ty
          val shallUseExpectedType = !sig.isVirtual &&
            Sub.is(realType, expectedType) && !Sub.is(expectedType, realType)

          if (shallUseExpectedType) expectedType
          else realType
        }

        val targets = objty match {
          case Type.Null =>
            Seq.empty
          case ExactClassRef(cls, _) =>
            cls.resolve(sig).toSeq
          case ClassRef(cls) if !sig.isVirtual =>
            cls.resolve(sig).toSeq
          case ScopeRef(scope) =>
            scope.targets(sig)
          case _ =>
            bailOut
        }

        if (targets.size == 0) {
          emit(Op.Method(materialize(obj), sig))
          Val.Zero(Type.Nothing)
        } else if (targets.size == 1) {
          Val.Global(targets.head, Type.Ptr)
        } else {
          targets.foreach(visitRoot)
          delay(Op.Method(materialize(obj), sig))
        }
      case Op.Dynmethod(obj, dynsig) =>
        linked.dynimpls.foreach {
          case impl @ Global.Member(_, sig) if sig.toProxy == dynsig =>
            visitRoot(impl)
          case _ =>
            ()
        }
        emit(Op.Dynmethod(materialize(eval(obj)), dynsig))
      case Op.Module(clsName) =>
        val isPure =
          isPureModule(clsName)
        val isWhitelisted =
          Whitelist.pure.contains(clsName)
        val canDelay =
          isPure || isWhitelisted

        if (canDelay) {
          delay(Op.Module(clsName))
        } else {
          emit(Op.Module(clsName))
        }
      case Op.As(ty, rawObj) =>
        val refty = ty match {
          case ty: Type.RefKind => ty
          case _                => bailOut
        }
        val obj = eval(rawObj)
        def fallback =
          emit(Op.As(ty, materialize(obj)))
        val objty = obj match {
          case InstanceRef(ty) =>
            ty
          case _ =>
            obj.ty
        }
        objty match {
          case Type.Null =>
            Val.Null
          case ScopeRef(scope) if Sub.is(scope, refty) =>
            obj
          case _ =>
            fallback
        }
      case Op.Is(ty, rawObj) =>
        val refty = ty match {
          case ty: Type.RefKind => ty
          case _                => bailOut
        }
        val obj = eval(rawObj)
        def fallback =
          delay(Op.Is(refty, obj))
        def objNotNull =
          delay(Op.Comp(Comp.Ine, Rt.Object, obj, Val.Null))
        val objty = obj match {
          case InstanceRef(ty) =>
            ty
          case _ =>
            obj.ty
        }
        objty match {
          case Type.Null =>
            Val.False
          case And(scoperef: Type.RefKind, ScopeRef(scope)) =>
            if (Sub.is(scope, refty)) {
              if (!scoperef.isNullable) {
                Val.True
              } else {
                objNotNull
              }
            } else if (scoperef.isExact) {
              Val.False
            } else {
              fallback
            }
          case _ =>
            fallback
        }
      case Op.Copy(v) =>
        eval(v)
      case Op.SizeOf(ty) =>
        if (ty.hasKnownSize) Val.Size(MemoryLayout.sizeOf(ty))
        else emit(op)
      case Op.AlignmentOf(ty) =>
        Val.Size(MemoryLayout.alignmentOf(ty))
      case Op.Box(boxty @ Type.Ref(boxname, _, _), value) =>
        // Pointer boxes are special because null boxes to null,
        // which breaks the invariant that all virtual allocations
        // are in fact non-null. We handle them as a delayed op instead.
        if (!Type.isPtrBox(boxty)) {
          Val.Virtual(state.allocBox(boxname, eval(value)))
        } else {
          delay(Op.Box(boxty, eval(value)))
        }
      case Op.Unbox(boxty @ Type.Ref(boxname, _, _), value) =>
        eval(value) match {
          case VirtualRef(_, cls, Array(value)) if boxname == cls.name =>
            value
          case DelayedRef(Op.Box(Type.Ref(innername, _, _), innervalue))
              if innername == boxname =>
            innervalue
          case value =>
            emit(Op.Unbox(boxty, materialize(value)))
        }
      case Op.Arrayalloc(ty, init) =>
        eval(init) match {
          case Val.Int(count) if count <= 128 =>
            Val.Virtual(state.allocArray(ty, count))
          case Val.ArrayValue(_, values) if values.size <= 128 =>
            val addr = state.allocArray(ty, values.size)
            val instance = state.derefVirtual(addr)
            values.zipWithIndex.foreach {
              case (v, idx) =>
                instance.values(idx) = v
            }
            Val.Virtual(addr)
          case init =>
            emit(Op.Arrayalloc(ty, materialize(init)))
        }
      case Op.Arrayload(ty, arr, idx) =>
        (eval(arr), eval(idx)) match {
          case (VirtualRef(_, _, values), Val.Int(offset))
              if inBounds(values, offset) =>
            values(offset)
          case (arr, idx) =>
            emit(Op.Arrayload(ty, materialize(arr), materialize(idx)))
        }
      case Op.Arraystore(ty, arr, idx, value) =>
        (eval(arr), eval(idx)) match {
          case (VirtualRef(_, _, values), Val.Int(offset))
              if inBounds(values, offset) =>
            values(offset) = eval(value)
            Val.Unit
          case (arr, idx) =>
            emit(
              Op.Arraystore(
                ty,
                materialize(arr),
                materialize(idx),
                materialize(eval(value))
              )
            )
        }
      case Op.Arraylength(arr) =>
        eval(arr) match {
          case VirtualRef(_, _, values) =>
            Val.Int(values.length)
          case arr =>
            emit(Op.Arraylength(materialize(arr)))
        }
      case Op.Var(ty) =>
        Val.Local(state.newVar(ty), Type.Var(ty))
      case Op.Varload(slot) =>
        val Val.Local(local, _) = eval(slot): @unchecked
        state.loadVar(local)
      case Op.Varstore(slot, value) =>
        val Val.Local(local, _) = eval(slot): @unchecked
        state.storeVar(local, eval(value))
        Val.Unit
      case _ => util.unreachable
    }
  }

  def eval(bin: Bin, ty: Type, l: Val, r: Val)(implicit
      state: State,
      origPos: Position
  ): Val = {
    import state.{emit, materialize}
    def fallback =
      emit(Op.Bin(bin, ty, materialize(l), materialize(r)))
    def bailOut =
      throw BailOut(s"can't eval bin op: $bin[${ty.show}] ${l.show}, ${r.show}")
    bin match {
      case Bin.Iadd =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Int(l + r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l + r)
          case _                          => bailOut
        }
      case Bin.Fadd =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Float(l + r)
          case (Val.Double(l), Val.Double(r)) => Val.Double(l + r)
          case _                              => bailOut
        }
      case Bin.Isub =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Int(l - r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l - r)
          case _                          => bailOut
        }
      case Bin.Fsub =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Float(l - r)
          case (Val.Double(l), Val.Double(r)) => Val.Double(l - r)
          case _                              => bailOut
        }
      case Bin.Imul =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Int(l * r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l * r)
          case _                          => bailOut
        }
      case Bin.Fmul =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Float(l * r)
          case (Val.Double(l), Val.Double(r)) => Val.Double(l * r)
          case _                              => bailOut
        }
      case Bin.Sdiv =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            if (r != 0) {
              Val.Int(l / r)
            } else {
              fallback
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0L) {
              Val.Long(l / r)
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case Bin.Udiv =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            if (r != 0) {
              Val.Int(java.lang.Integer.divideUnsigned(l, r))
            } else {
              fallback
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0) {
              Val.Long(java.lang.Long.divideUnsigned(l, r))
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case Bin.Fdiv =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Float(l / r)
          case (Val.Double(l), Val.Double(r)) => Val.Double(l / r)
          case _                              => bailOut
        }
      case Bin.Srem =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            if (r != 0) {
              Val.Int(l % r)
            } else {
              fallback
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0L) {
              Val.Long(l % r)
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case Bin.Urem =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            if (r != 0) {
              Val.Int(java.lang.Integer.remainderUnsigned(l, r))
            } else {
              fallback
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0L) {
              Val.Long(java.lang.Long.remainderUnsigned(l, r))
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case Bin.Frem =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Float(l % r)
          case (Val.Double(l), Val.Double(r)) => Val.Double(l % r)
          case _                              => bailOut
        }
      case Bin.Shl =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Int(l << r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l << r)
          case _                          => bailOut
        }
      case Bin.Lshr =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Int(l >>> r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l >>> r)
          case _                          => bailOut
        }
      case Bin.Ashr =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Int(l >> r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l >> r)
          case _                          => bailOut
        }
      case Bin.And =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r)) => Val.Bool(l & r)
          case (Val.Int(l), Val.Int(r))   => Val.Int(l & r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l & r)
          case _                          => bailOut
        }
      case Bin.Or =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r)) => Val.Bool(l | r)
          case (Val.Int(l), Val.Int(r))   => Val.Int(l | r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l | r)
          case _                          => bailOut
        }
      case Bin.Xor =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r)) => Val.Bool(l ^ r)
          case (Val.Int(l), Val.Int(r))   => Val.Int(l ^ r)
          case (Val.Long(l), Val.Long(r)) => Val.Long(l ^ r)
          case _                          => bailOut
        }
    }
  }

  def eval(comp: Comp, ty: Type, l: Val, r: Val)(implicit state: State): Val = {
    def bailOut =
      throw BailOut(
        s"can't eval comp op: $comp[${ty.show}] ${l.show}, ${r.show}"
      )
    comp match {
      case Comp.Ieq =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r))           => Val.Bool(l == r)
          case (Val.Int(l), Val.Int(r))             => Val.Bool(l == r)
          case (Val.Long(l), Val.Long(r))           => Val.Bool(l == r)
          case (Val.Null, Val.Null)                 => Val.True
          case (Val.Global(l, _), Val.Global(r, _)) => Val.Bool(l == r)
          case (Val.Null | _: Val.Global, Val.Null | _: Val.Global) => Val.False
          case _                                                    => bailOut
        }
      case Comp.Ine =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r))           => Val.Bool(l != r)
          case (Val.Int(l), Val.Int(r))             => Val.Bool(l != r)
          case (Val.Long(l), Val.Long(r))           => Val.Bool(l != r)
          case (Val.Null, Val.Null)                 => Val.False
          case (Val.Global(l, _), Val.Global(r, _)) => Val.Bool(l != r)
          case (Val.Null | _: Val.Global, Val.Null | _: Val.Global) => Val.True
          case _                                                    => bailOut
        }
      case Comp.Ugt =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            Val.Bool(java.lang.Integer.compareUnsigned(l, r) > 0)
          case (Val.Long(l), Val.Long(r)) =>
            Val.Bool(java.lang.Long.compareUnsigned(l, r) > 0)
          case _ =>
            bailOut
        }
      case Comp.Uge =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            Val.Bool(java.lang.Integer.compareUnsigned(l, r) >= 0)
          case (Val.Long(l), Val.Long(r)) =>
            Val.Bool(java.lang.Long.compareUnsigned(l, r) >= 0)
          case _ =>
            bailOut
        }
      case Comp.Ult =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            Val.Bool(java.lang.Integer.compareUnsigned(l, r) < 0)
          case (Val.Long(l), Val.Long(r)) =>
            Val.Bool(java.lang.Long.compareUnsigned(l, r) < 0)
          case _ =>
            bailOut
        }
      case Comp.Ule =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) =>
            Val.Bool(java.lang.Integer.compareUnsigned(l, r) <= 0)
          case (Val.Long(l), Val.Long(r)) =>
            Val.Bool(java.lang.Long.compareUnsigned(l, r) <= 0)
          case _ =>
            bailOut
        }
      case Comp.Sgt =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Bool(l > r)
          case (Val.Long(l), Val.Long(r)) => Val.Bool(l > r)
          case _                          => bailOut
        }
      case Comp.Sge =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Bool(l >= r)
          case (Val.Long(l), Val.Long(r)) => Val.Bool(l >= r)
          case _                          => bailOut
        }
      case Comp.Slt =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Bool(l < r)
          case (Val.Long(l), Val.Long(r)) => Val.Bool(l < r)
          case _                          => bailOut
        }
      case Comp.Sle =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r))   => Val.Bool(l <= r)
          case (Val.Long(l), Val.Long(r)) => Val.Bool(l <= r)
          case _                          => bailOut
        }
      case Comp.Feq =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Bool(l == r)
          case (Val.Double(l), Val.Double(r)) => Val.Bool(l == r)
          case _                              => bailOut
        }
      case Comp.Fne =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Bool(l != r)
          case (Val.Double(l), Val.Double(r)) => Val.Bool(l != r)
          case _                              => bailOut
        }
      case Comp.Fgt =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Bool(l > r)
          case (Val.Double(l), Val.Double(r)) => Val.Bool(l > r)
          case _                              => bailOut
        }
      case Comp.Fge =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Bool(l >= r)
          case (Val.Double(l), Val.Double(r)) => Val.Bool(l >= r)
          case _                              => bailOut
        }
      case Comp.Flt =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Bool(l < r)
          case (Val.Double(l), Val.Double(r)) => Val.Bool(l < r)
          case _                              => bailOut
        }
      case Comp.Fle =>
        (l, r) match {
          case (Val.Float(l), Val.Float(r))   => Val.Bool(l <= r)
          case (Val.Double(l), Val.Double(r)) => Val.Bool(l <= r)
          case _                              => bailOut
        }
    }
  }

  def eval(conv: Conv, ty: Type, value: Val)(implicit state: State): Val = {
    def bailOut =
      throw BailOut(s"can't eval conv op: $conv[${ty.show}] ${value.show}")
    conv match {
      case _ if ty == value.ty => value
      case Conv.SSizeCast | Conv.ZSizeCast =>
        def size(ty: Type) = ty match {
          case Type.Size =>
            if (platform.is32Bit) 32 else 64
          case Type.FixedSizeI(s, _) => s
          case o                     => bailOut
        }
        val fromSize = size(value.ty)
        val toSize = size(ty)

        if (fromSize == toSize) eval(Conv.Bitcast, ty, value)
        else if (fromSize > toSize) eval(Conv.Trunc, ty, value)
        else if (conv == Conv.ZSizeCast) eval(Conv.Zext, ty, value)
        else eval(Conv.Sext, ty, value)

      case Conv.Trunc =>
        (value, ty) match {
          case (Val.Char(v), Type.Byte)  => Val.Byte(v.toByte)
          case (Val.Short(v), Type.Byte) => Val.Byte(v.toByte)
          case (Val.Int(v), Type.Byte)   => Val.Byte(v.toByte)
          case (Val.Int(v), Type.Short)  => Val.Short(v.toShort)
          case (Val.Int(v), Type.Char)   => Val.Char(v.toChar)
          case (Val.Long(v), Type.Byte)  => Val.Byte(v.toByte)
          case (Val.Long(v), Type.Short) => Val.Short(v.toShort)
          case (Val.Long(v), Type.Int)   => Val.Int(v.toInt)
          case (Val.Long(v), Type.Char)  => Val.Char(v.toChar)
          case (Val.Size(v), Type.Byte)  => Val.Byte(v.toByte)
          case (Val.Size(v), Type.Short) => Val.Short(v.toShort)
          case (Val.Size(v), Type.Int) if !platform.is32Bit => Val.Int(v.toInt)
          case (Val.Size(v), Type.Char) => Val.Char(v.toChar)
          case _                        => bailOut
        }
      case Conv.Zext =>
        (value, ty) match {
          case (Val.Char(v), Type.Int) =>
            Val.Int(v.toInt)
          case (Val.Char(v), Type.Long) =>
            Val.Long(v.toLong)
          case (Val.Short(v), Type.Int) =>
            Val.Int(v.toChar.toInt)
          case (Val.Short(v), Type.Long) =>
            Val.Long(v.toChar.toLong)
          case (Val.Int(v), Type.Long) =>
            Val.Long(java.lang.Integer.toUnsignedLong(v))
          case (Val.Int(v), Type.Size) if !platform.is32Bit =>
            Val.Size(java.lang.Integer.toUnsignedLong(v))
          case (Val.Size(v), Type.Long) if platform.is32Bit =>
            Val.Long(java.lang.Integer.toUnsignedLong(v.toInt))
          case _ =>
            bailOut
        }
      case Conv.Sext =>
        (value, ty) match {
          case (Val.Byte(v), Type.Short) => Val.Short(v.toShort)
          case (Val.Byte(v), Type.Char)  => Val.Char(v.toChar)
          case (Val.Byte(v), Type.Int)   => Val.Int(v.toInt)
          case (Val.Byte(v), Type.Long)  => Val.Long(v.toLong)
          case (Val.Short(v), Type.Int)  => Val.Int(v.toInt)
          case (Val.Short(v), Type.Long) => Val.Long(v.toLong)
          case (Val.Int(v), Type.Long)   => Val.Long(v.toLong)
          case (Val.Int(v), Type.Size) if !platform.is32Bit =>
            Val.Size(v.toLong)
          case (Val.Size(v), Type.Long) if platform.is32Bit =>
            Val.Long(v.toInt.toLong)
          case _ => bailOut
        }
      case Conv.Fptrunc =>
        (value, ty) match {
          case (Val.Double(v), Type.Float) => Val.Float(v.toFloat)
          case _                           => bailOut
        }
      case Conv.Fpext =>
        (value, ty) match {
          case (Val.Float(v), Type.Double) => Val.Double(v.toDouble)
          case _                           => bailOut
        }
      case Conv.Fptoui =>
        (value, ty) match {
          case (Val.Float(v), Type.Char)  => Val.Char(v.toChar)
          case (Val.Double(v), Type.Char) => Val.Char(v.toChar)
          case _                          => bailOut
        }
      case Conv.Fptosi =>
        (value, ty) match {
          case (Val.Float(v), Type.Int)   => Val.Int(v.toInt)
          case (Val.Double(v), Type.Int)  => Val.Int(v.toInt)
          case (Val.Float(v), Type.Long)  => Val.Long(v.toLong)
          case (Val.Double(v), Type.Long) => Val.Long(v.toLong)
          case _                          => bailOut
        }
      case Conv.Uitofp =>
        (value, ty) match {
          case (Val.Char(v), Type.Float)  => Val.Float(v.toInt.toFloat)
          case (Val.Char(v), Type.Double) => Val.Double(v.toInt.toFloat)
          case _                          => bailOut
        }
      case Conv.Sitofp =>
        (value, ty) match {
          case (Val.Byte(v), Type.Float)   => Val.Float(v.toFloat)
          case (Val.Byte(v), Type.Double)  => Val.Double(v.toDouble)
          case (Val.Short(v), Type.Float)  => Val.Float(v.toFloat)
          case (Val.Short(v), Type.Double) => Val.Double(v.toDouble)
          case (Val.Int(v), Type.Float)    => Val.Float(v.toFloat)
          case (Val.Int(v), Type.Double)   => Val.Double(v.toDouble)
          case (Val.Long(v), Type.Float)   => Val.Float(v.toFloat)
          case (Val.Long(v), Type.Double)  => Val.Double(v.toDouble)
          case _                           => bailOut
        }
      case Conv.Ptrtoint =>
        (value, ty) match {
          case (Val.Null, Type.Long) => Val.Long(0L)
          case (Val.Null, Type.Int)  => Val.Int(0)
          case _                     => bailOut
        }
      case Conv.Inttoptr =>
        (value, ty) match {
          case (Val.Long(0L), Type.Ptr) => Val.Null
          case (Val.Int(0L), Type.Ptr)  => Val.Null
          case _                        => bailOut
        }
      case Conv.Bitcast =>
        (value, ty) match {
          case (value, ty) if value.ty == ty =>
            value
          case (Val.Char(value), Type.Short) =>
            Val.Short(value.toShort)
          case (Val.Short(value), Type.Char) =>
            Val.Char(value.toChar)
          case (Val.Int(value), Type.Float) =>
            Val.Float(java.lang.Float.intBitsToFloat(value))
          case (Val.Long(value), Type.Double) =>
            Val.Double(java.lang.Double.longBitsToDouble(value))
          case (Val.Float(value), Type.Int) =>
            Val.Int(java.lang.Float.floatToRawIntBits(value))
          case (Val.Double(value), Type.Long) =>
            Val.Long(java.lang.Double.doubleToRawLongBits(value))
          case (Val.Size(value), Type.Int) if platform.is32Bit =>
            Val.Int(value.toInt)
          case (Val.Int(value), Type.Size) if platform.is32Bit =>
            Val.Size(value.toLong)
          case (Val.Size(value), Type.Long) if !platform.is32Bit =>
            Val.Long(value)
          case (Val.Long(value), Type.Size) if !platform.is32Bit =>
            Val.Size(value)
          case (Val.Null, Type.Ptr) =>
            Val.Null
          case _ =>
            bailOut
        }
    }
  }

  def eval(value: Val)(implicit state: State, origPos: Position): Val = {
    value match {
      case Val.Local(local, _) if local.id >= 0 =>
        state.loadLocal(local) match {
          case value: Val.Virtual =>
            eval(value)
          case value =>
            value
        }
      case Val.Virtual(addr) if state.hasEscaped(addr) =>
        state.derefEscaped(addr).escapedValue
      case Val.String(value) =>
        Val.Virtual(state.allocString(value))
      case Val.Global(name, _) =>
        maybeOriginal(name).foreach {
          case defn if defn.attrs.isExtern =>
            visitRoot(defn.name)
          case _ =>
            ()
        }
        value
      case _ =>
        value.canonicalize
    }
  }

  private def inBounds(values: Array[Val], offset: Int): Boolean = {
    inBounds(values.length, offset)
  }

  private def inBounds(length: Int, offset: Int): Boolean = {
    offset >= 0 && offset < length
  }

  private def isPureModule(clsName: Global): Boolean = {
    var visiting = List[Global]()

    def isPureModule(clsName: Global): Boolean = {
      if (hasModulePurity(clsName)) {
        getModulePurity(clsName)
      } else {
        visiting = clsName :: visiting

        val init = clsName member Sig.Ctor(Seq.empty)
        val isPure =
          if (!shallVisit(init)) {
            true
          } else {
            visitDuplicate(init, argumentTypes(init)).fold {
              false
            } { defn => isPureModuleCtor(defn) }
          }
        setModulePurity(clsName, isPure)
        isPure
      }
    }

    def isPureModuleCtor(defn: Defn.Define): Boolean = {
      val Inst.Label(_, Val.Local(self, _) +: _) = defn.insts.head: @unchecked

      val canStoreTo = mutable.Set(self)
      val arrayLength = mutable.Map.empty[Local, Int]

      defn.insts.foreach {
        case Inst.Let(n, Op.Arrayalloc(_, init), _) =>
          canStoreTo += n
          init match {
            case Val.Int(size) =>
              arrayLength(n) = size
            case Val.ArrayValue(_, elems) =>
              arrayLength(n) = elems.size
            case _ =>
              ()
          }
        case Inst.Let(n, _: Op.Classalloc | _: Op.Box | _: Op.Module, _) =>
          canStoreTo += n
        case _ =>
          ()
      }

      def canStoreValue(v: Val): Boolean = v match {
        case _ if v.isCanonical => true
        case Val.Local(n, _)    => canStoreTo.contains(n)
        case _: Val.String      => true
        case _                  => false
      }

      defn.insts.forall {
        case inst @ (_: Inst.Throw | _: Inst.Unreachable) =>
          false
        case _: Inst.Label =>
          true
        case _: Inst.Cf =>
          true
        case Inst.Let(_, op, _) if op.isPure =>
          true
        case Inst.Let(_, _: Op.Classalloc | _: Op.Arrayalloc | _: Op.Box, _) =>
          true
        case inst @ Inst.Let(_, Op.Module(name), _) =>
          if (!visiting.contains(name)) {
            isPureModule(name)
          } else {
            false
          }
        case Inst.Let(_, Op.Fieldload(_, Val.Local(to, _), _), _)
            if canStoreTo.contains(to) =>
          true
        case inst @ Inst.Let(_, Op.Fieldstore(_, Val.Local(to, _), _, value), _)
            if canStoreTo.contains(to) =>
          canStoreValue(value)
        case Inst.Let(_, Op.Arrayload(_, Val.Local(to, _), Val.Int(idx)), _)
            if canStoreTo.contains(to)
              && inBounds(arrayLength.getOrElse(to, -1), idx) =>
          true
        case Inst.Let(
              _,
              Op.Arraystore(_, Val.Local(to, _), Val.Int(idx), value),
              _
            )
            if canStoreTo.contains(to)
              && inBounds(arrayLength.getOrElse(to, -1), idx) =>
          canStoreValue(value)
        case Inst.Let(_, Op.Arraylength(Val.Local(to, _)), _)
            if canStoreTo.contains(to) =>
          true
        case inst =>
          false
      }
    }

    isPureModule(clsName)
  }
}
