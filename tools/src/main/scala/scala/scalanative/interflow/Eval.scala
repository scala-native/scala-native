package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._
import scalanative.codegen.MemoryLayout
import scalanative.util.{unreachable, And}

trait Eval { self: Interflow =>
  def run(insts: Array[Inst], offsets: Map[Local, Int], from: Local)(
      implicit state: State): Inst.Cf = {
    var pc = offsets(from) + 1

    while (true) {
      val inst = insts(pc)
      def bailOut =
        throw BailOut("can't eval inst: " + inst.show)
      inst match {
        case _: Inst.Label =>
          unreachable
        case Inst.Let(local, op, unwind) =>
          val value = eval(local, op, unwind)
          if (value.ty == Type.Nothing) {
            return Inst.Unreachable(unwind)
          } else {
            val shortUnitValue =
              if (value.ty == Type.Unit) Val.Unit else value
            state.storeLocal(local, shortUnitValue)
            pc += 1
          }
        case Inst.Ret(v) =>
          return Inst.Ret(eval(v))
        case Inst.Jump(Next.Label(target, args)) =>
          val evalArgs = args.map(eval)
          val next     = Next.Label(target, evalArgs)
          return Inst.Jump(next)
        case Inst.If(cond,
                     Next.Label(thenTarget, thenArgs),
                     Next.Label(elseTarget, elseArgs)) =>
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
              return Inst.If(cond, thenNext, elseNext)
          }
        case Inst.Switch(scrut,
                         Next.Label(defaultTarget, defaultArgs),
                         cases) =>
          def defaultNext =
            Next.Label(defaultTarget, defaultArgs.map(eval))
          eval(scrut) match {
            case value if value.isCanonical =>
              cases
                .collectFirst {
                  case Next.Case(caseValue, Next.Label(caseTarget, caseArgs))
                      if caseValue == value =>
                    val evalArgs = caseArgs.map(eval)
                    val next     = Next.Label(caseTarget, evalArgs)
                    return Inst.Jump(next)
                }
                .getOrElse {
                  return Inst.Jump(defaultNext)
                }
            case scrut =>
              return Inst.Switch(scrut, defaultNext, cases)
          }
        case Inst.Throw(v, unwind) =>
          val excv = eval(v)
          unwind match {
            case Next.None =>
              return Inst.Throw(excv, Next.None)
            case Next.Unwind(Val.Local(exc, _), Next.Label(name, args)) =>
              state.storeLocal(exc, excv)
              val eargs = args.map(eval)
              val next  = Next.Label(name, eargs)
              return Inst.Jump(next)
            case _ =>
              unreachable
          }
        case Inst.Unreachable(unwind) =>
          unwind match {
            case Next.None =>
              return Inst.Unreachable(unwind)
            case Next.Unwind(Val.Local(exc, _), Next.Label(name, args)) =>
              val eexc = Val.Local(state.fresh(), Rt.Object)
              state.storeLocal(exc, eexc)
              val eargs = args.map(eval)
              return Inst.Unreachable(
                Next.Unwind(eexc, Next.Label(name, eargs)))
            case _ =>
              unreachable
          }
      }
    }

    unreachable
  }

  def eval(local: Local, op: Op, unwind: Next)(implicit state: State,
                                               linked: linker.Result): Val = {
    import state.materialize
    def emit = {
      if (unwind ne Next.None) {
        throw BailOut("try-catch")
      }
      state.emit
    }
    def bailOut =
      throw BailOut("can't eval op: " + op.show)
    op match {
      case Op.Call(sig, meth, args) =>
        eval(meth) match {
          case Val.Global(name, _) if intrinsics.contains(name) =>
            intrinsic(local, sig, name, args, unwind)
          case emeth =>
            val eargs = args.map(eval)
            val argtys = eargs.map {
              case Val.Virtual(addr) =>
                state.deref(addr).cls.ty
              case value =>
                value.ty
            }

            val (dsig, dtarget) = emeth match {
              case Val.Global(name, _) =>
                visitDuplicate(name, argtys)
                  .map { defn =>
                    (defn.ty, Val.Global(defn.name, Type.Ptr))
                  }
                  .getOrElse {
                    (sig, emeth)
                  }
              case _ =>
                (sig, emeth)
            }

            def fallback = {
              val mtarget = materialize(dtarget)
              val margs   = eargs.map(materialize)

              val isDuplicate =
                mtarget match {
                  case Val.Global(Global.Member(_, _: Sig.Duplicate), _) =>
                    true
                  case _ =>
                    false
                }

              val cargs =
                if (!isDuplicate) {
                  margs
                } else {
                  val Type.Function(sigtys, _) = dsig

                  // Method target might have a more precise signature
                  // than what's known currently available at the call site.
                  // This is a side effect of a method target selection taking
                  // into account which classes are allocated across whole program.
                  margs.zip(sigtys).map {
                    case (marg, ty) =>
                      if (!Sub.is(marg.ty, ty)) {
                        emit.conv(Conv.Bitcast, ty, marg, unwind)
                      } else {
                        marg
                      }
                  }
                }

              emit.call(dsig, mtarget, cargs, unwind)
            }

            dtarget match {
              case Val.Global(name, _) if shallInline(name, eargs, unwind) =>
                inline(name, eargs, unwind)
              case _ =>
                fallback
            }
        }
      case Op.Load(ty, ptr) =>
        emit.load(ty, materialize(eval(ptr)), unwind)
      case Op.Store(ty, ptr, value) =>
        emit.store(ty, materialize(eval(ptr)), materialize(eval(value)), unwind)
      case Op.Elem(ty, ptr, indexes) =>
        emit.elem(ty,
                  materialize(eval(ptr)),
                  indexes.map(i => materialize(eval(i))),
                  unwind)
      case Op.Extract(aggr, indexes) =>
        emit.extract(materialize(eval(aggr)), indexes, unwind)
      case Op.Insert(aggr, value, indexes) =>
        emit.insert(materialize(eval(aggr)),
                    materialize(eval(value)),
                    indexes,
                    unwind)
      case Op.Stackalloc(ty, n) =>
        emit.stackalloc(ty, materialize(eval(n)), unwind)
      case Op.Bin(bin, ty, l, r) =>
        (eval(l), eval(r)) match {
          case (l, r) if l.isCanonical && r.isCanonical =>
            eval(bin, ty, l, r, unwind)
          case (l, r) =>
            emit.bin(bin, ty, materialize(l), materialize(r), unwind)
        }
      case Op.Comp(comp, ty, l, r) =>
        (comp, eval(l), eval(r)) match {
          case (_, l, r) if l.isCanonical && r.isCanonical =>
            eval(comp, ty, l, r)

          // Two virtual allocations will compare equal if
          // and only if they have the same virtual address.
          case (Comp.Ieq, Val.Virtual(l), Val.Virtual(r)) =>
            Val.Bool(l == r)
          case (Comp.Ine, Val.Virtual(l), Val.Virtual(r)) =>
            Val.Bool(l != r)

          // Not-yet-materialized virtual allocation will never be
          // the same as already existing allocation (be it null
          // or any other value).
          case (Comp.Ieq, Val.Virtual(addr), r) =>
            Val.False
          case (Comp.Ieq, l, Val.Virtual(addr)) =>
            Val.False
          case (Comp.Ine, Val.Virtual(addr), r) =>
            Val.True
          case (Comp.Ine, l, Val.Virtual(addr)) =>
            Val.True

          // Comparing non-nullable value with null will always
          // yield the same result.
          case (Comp.Ieq, v @ Of(ty: Type.RefKind), Val.Null)
              if !ty.isNullable =>
            Val.False
          case (Comp.Ieq, Val.Null, v @ Of(ty: Type.RefKind))
              if !ty.isNullable =>
            Val.False
          case (Comp.Ine, v @ Of(ty: Type.RefKind), Val.Null)
              if !ty.isNullable =>
            Val.True
          case (Comp.Ine, Val.Null, v @ Of(ty: Type.RefKind))
              if !ty.isNullable =>
            Val.True

          // Comparing two non-null module references will
          // yield true only if it's the same module.
          case (Comp.Ieq,
                l @ Of(And(lty: Type.RefKind, ClassRef(lcls))),
                r @ Of(And(rty: Type.RefKind, ClassRef(rcls))))
              if !lty.isNullable && lty.isExact && lcls.isModule
                && !rty.isNullable && rty.isExact && rcls.isModule =>
            Val.Bool(lcls.name == rcls.name)
          case (Comp.Ine,
                l @ Of(And(lty: Type.RefKind, ClassRef(lcls))),
                r @ Of(And(rty: Type.RefKind, ClassRef(rcls))))
              if !lty.isNullable && lty.isExact && lcls.isModule
                && !rty.isNullable && rty.isExact && rcls.isModule =>
            Val.Bool(lcls.name != rcls.name)

          case (_, l, r) =>
            emit.comp(comp, ty, materialize(l), materialize(r), unwind)
        }
      case Op.Conv(conv, ty, value) =>
        eval(value) match {
          case value if value.isCanonical =>
            eval(conv, ty, value)
          case value =>
            emit.conv(conv, ty, materialize(value), unwind)
        }
      case Op.Classalloc(ClassRef(cls)) =>
        Val.Virtual(state.allocClass(cls))
      case Op.Fieldload(ty, obj, name @ FieldRef(cls, fld)) =>
        eval(obj) match {
          case Val.Virtual(addr) =>
            val instance = state.derefVirtual(addr)
            instance.values(fld.index)
          case obj =>
            emit.fieldload(ty, materialize(obj), name, unwind)
        }
      case Op.Fieldstore(ty, obj, name @ FieldRef(cls, fld), value) =>
        eval(obj) match {
          case Val.Virtual(addr) =>
            val instance = state.derefVirtual(addr)
            instance.values(fld.index) = eval(value)
            Val.Unit
          case obj =>
            emit.fieldstore(ty,
                            materialize(obj),
                            name,
                            materialize(eval(value)),
                            unwind)
        }
      case Op.Method(obj, sig) =>
        eval(obj) match {
          case Val.Virtual(addr) =>
            val cls      = state.deref(addr).cls
            val resolved = cls.resolve(sig).get
            Val.Global(resolved, Type.Ptr)
          case obj if obj.ty == Type.Null =>
            emit.method(materialize(obj), sig, unwind)
          case obj =>
            val targets = obj.ty match {
              case ExactClassRef(cls, _) =>
                cls.resolve(sig).toSeq
              case ScopeRef(scope) =>
                scope.targets(sig)
              case _ =>
                bailOut
            }
            targets match {
              case Seq() =>
                Val.Zero(Type.Nothing)
              case Seq(meth) =>
                Val.Global(meth, Type.Ptr)
              case meths =>
                meths.foreach(visitRoot)
                emit.method(materialize(obj), sig, unwind)
            }
        }
      case Op.Dynmethod(obj, dynsig) =>
        linked.dynimpls.foreach {
          case impl @ Global.Member(_, sig) if sig.toProxy == dynsig =>
            visitRoot(impl)
          case _ =>
            ()
        }
        emit.dynmethod(materialize(eval(obj)), dynsig, unwind)
      case Op.Module(clsName) =>
        val init = clsName member Sig.Ctor(Seq.empty)
        if (originals.contains(init)) {
          visitRoot(init)
        }
        emit.module(clsName, unwind)
      case Op.As(ty, obj) =>
        val refty = ty match {
          case ty: Type.RefKind => ty
          case _                => bailOut
        }
        eval(obj) match {
          case obj @ Val.Virtual(addr)
              if Sub.is(state.deref(addr).cls, refty) =>
            obj
          case obj if obj.ty == Type.Null =>
            obj
          case obj =>
            obj.ty match {
              case ClassRef(cls) if Sub.is(cls, refty) =>
                obj
              case _ =>
                emit.as(ty, materialize(obj), unwind)
            }
        }
      case Op.Is(ty, obj) =>
        val refty = ty match {
          case ty: Type.RefKind => ty
          case _                => bailOut
        }
        eval(obj) match {
          case Val.Virtual(addr) =>
            Val.Bool(Sub.is(state.deref(addr).cls, refty))
          case obj if obj.ty == Type.Null =>
            Val.False
          case obj =>
            obj.ty match {
              case ExactClassRef(cls, nullable) =>
                val isStatically = Sub.is(cls, refty)
                val res = if (!isStatically) {
                  Val.False
                } else if (!nullable) {
                  Val.True
                } else {
                  emit.comp(Comp.Ine, Rt.Object, obj, Val.Null, unwind)
                }
                res
              case _ =>
                emit.is(refty, materialize(obj), unwind)
            }
        }
      case Op.Copy(v) =>
        eval(v)
      case Op.Sizeof(ty) =>
        Val.Long(MemoryLayout.sizeOf(ty))
      case Op.Box(Type.Ref(boxname, _, _), value) =>
        Val.Virtual(state.allocBox(boxname, eval(value)))
      case Op.Unbox(boxty @ Type.Ref(boxname, _, _), value) =>
        eval(value) match {
          case VirtualRef(_, cls, Array(value)) if boxname == cls.name =>
            value
          case value =>
            emit.unbox(boxty, materialize(value), unwind)
        }
      case Op.Arrayalloc(ty, init) =>
        eval(init) match {
          case Val.Int(count) if count <= 128 =>
            Val.Virtual(state.allocArray(ty, count))
          case Val.ArrayValue(_, values) if values.size <= 128 =>
            val addr     = state.allocArray(ty, values.size)
            val instance = state.derefVirtual(addr)
            values.zipWithIndex.foreach {
              case (v, idx) =>
                instance.values(idx) = v
            }
            Val.Virtual(addr)
          case init =>
            emit.arrayalloc(ty, materialize(init), unwind)
        }
      case Op.Arrayload(ty, arr, idx) =>
        (eval(arr), eval(idx)) match {
          case (VirtualRef(_, _, values), Val.Int(offset))
              if inBounds(values, offset) =>
            values(offset)
          case (arr, idx) =>
            emit.arrayload(ty, materialize(arr), materialize(idx), unwind)
        }
      case Op.Arraystore(ty, arr, idx, value) =>
        (eval(arr), eval(idx)) match {
          case (VirtualRef(_, _, values), Val.Int(offset))
              if inBounds(values, offset) =>
            values(offset) = eval(value)
            Val.Unit
          case (arr, idx) =>
            emit.arraystore(ty,
                            materialize(arr),
                            materialize(idx),
                            materialize(eval(value)),
                            unwind)
        }
      case Op.Arraylength(arr) =>
        eval(arr) match {
          case VirtualRef(_, _, values) =>
            Val.Int(values.length)
          case arr =>
            emit.arraylength(materialize(arr), unwind)
        }
      case Op.Var(ty) =>
        Val.Local(state.newVar(ty), Type.Var(ty))
      case Op.Varload(slot) =>
        val Val.Local(local, _) = eval(slot)
        state.loadVar(local)
      case Op.Varstore(slot, value) =>
        val Val.Local(local, _) = eval(slot)
        state.storeVar(local, eval(value))
        Val.Unit
    }
  }

  def eval(bin: Bin, ty: Type, l: Val, r: Val, unwind: Next)(
      implicit state: State): Val = {
    import state.materialize
    def emit = {
      if (unwind ne Next.None) {
        throw BailOut("try-catch")
      }
      state.emit.bin(bin, ty, materialize(l), materialize(r), unwind)
    }
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
              emit
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0L) {
              Val.Long(l / r)
            } else {
              emit
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
              emit
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0) {
              Val.Long(java.lang.Long.divideUnsigned(l, r))
            } else {
              emit
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
              emit
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0L) {
              Val.Long(l % r)
            } else {
              emit
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
              emit
            }
          case (Val.Long(l), Val.Long(r)) =>
            if (r != 0L) {
              Val.Long(java.lang.Long.remainderUnsigned(l, r))
            } else {
              emit
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
        s"can't eval comp op: $comp[${ty.show}] ${l.show}, ${r.show}")
    comp match {
      case Comp.Ieq =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r))                           => Val.Bool(l == r)
          case (Val.Int(l), Val.Int(r))                             => Val.Bool(l == r)
          case (Val.Long(l), Val.Long(r))                           => Val.Bool(l == r)
          case (Val.Null, Val.Null)                                 => Val.True
          case (Val.Global(l, _), Val.Global(r, _))                 => Val.Bool(l == r)
          case (Val.Null | _: Val.Global, Val.Null | _: Val.Global) => Val.False
          case _                                                    => bailOut
        }
      case Comp.Ine =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r))                           => Val.Bool(l != r)
          case (Val.Int(l), Val.Int(r))                             => Val.Bool(l != r)
          case (Val.Long(l), Val.Long(r))                           => Val.Bool(l != r)
          case (Val.Null, Val.Null)                                 => Val.False
          case (Val.Global(l, _), Val.Global(r, _))                 => Val.Bool(l != r)
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
      case _ if ty == value.ty =>
        value
      case Conv.Trunc =>
        (value, ty) match {
          case (Val.Char(v), Type.Byte)  => Val.Byte(v.toByte)
          case (Val.Short(v), Type.Byte) => Val.Byte(v.toByte)
          case (Val.Int(v), Type.Byte)   => Val.Byte(v.toByte)
          case (Val.Int(v), Type.Short)  => Val.Short(v.toShort)
          case (Val.Int(v), Type.Char)   => Val.Char(v.toChar)
          case (Val.Long(v), Type.Byte)  => Val.Int(v.toByte)
          case (Val.Long(v), Type.Short) => Val.Int(v.toShort)
          case (Val.Long(v), Type.Int)   => Val.Int(v.toInt)
          case (Val.Long(v), Type.Char)  => Val.Char(v.toChar)
          case _                         => bailOut
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
          case _                         => bailOut
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
          case _                     => bailOut
        }
      case Conv.Inttoptr =>
        (value, ty) match {
          case (Val.Long(0L), Type.Ptr) => Val.Null
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
          case (Val.Null, Type.Ptr) =>
            Val.Null
          case _ =>
            bailOut
        }
    }
  }

  def eval(value: Val)(implicit state: State): Val = {
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
      case _ =>
        value.canonicalize
    }
  }

  private def inBounds(values: Array[Val], offset: Int): Boolean = {
    offset >= 0 && offset < values.length
  }
}
