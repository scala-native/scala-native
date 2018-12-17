package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._
import scalanative.codegen.MemoryLayout
import scalanative.util.unreachable

trait Eval { self: Interflow =>
  def run(insts: Array[Inst],
          offsets: Map[Local, Int],
          from: Local,
          blockFresh: Fresh)(implicit state: State): Inst.Cf = {
    var pc = offsets(from) + 1

    while (true) {
      val inst = insts(pc)
      def bailOut =
        throw BailOut("can't eval inst: " + inst.show)
      inst match {
        case _: Inst.Label =>
          unreachable
        case Inst.Let(local, op, unwind) =>
          val value = eval(local, op, unwind, blockFresh)
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
          return Inst.Jump(Next.Label(target, evalArgs))
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
                    return Inst.Jump(Next.Label(caseTarget, evalArgs))
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
              return Inst.Jump(Next.Label(name, eargs))
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

  def eval(local: Local, op: Op, unwind: Next, blockFresh: Fresh)(
      implicit state: State,
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
        val emeth = eval(meth)
        val eargs = args.map(eval)

        emeth match {
          case Val.Global(name, _) if intrinsics.contains(name) =>
            intrinsic(local, sig, name, eargs, unwind)
          case _ =>
            val mmeth   = materialize(emeth)
            val margs   = eargs.map(materialize(_))
            val margtys = margs.map(_.ty)

            val (msig, mtarget) = mmeth match {
              case Val.Global(name, _) =>
                visitDuplicate(name, margtys)
                  .map { defn =>
                    (defn.ty, Val.Global(defn.name, Type.Ptr))
                  }
                  .getOrElse {
                    (sig, mmeth)
                  }
              case _ =>
                (sig, mmeth)
            }

            val isDuplicate =
              mmeth match {
                case Val.Global(Global.Member(_, _: Sig.Duplicate), _) =>
                  true
                case _ =>
                  false
              }

            val cargs =
              if (!isDuplicate) {
                margs
              } else {
                val Type.Function(sigtys, _) = msig

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

            emit.call(msig, mtarget, cargs, unwind)
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
            eval(bin, ty, l, r)
          case (l, r) =>
            emit.bin(bin, ty, materialize(l), materialize(r), unwind)
        }
      case Op.Comp(comp, ty, l, r) =>
        (comp, eval(l), eval(r)) match {
          case (_, l, r) if l.isCanonical && r.isCanonical =>
            eval(comp, ty, l, r)
          case (Comp.Ieq, Val.Virtual(addr), r) if !r.isCanonical =>
            Val.False
          case (Comp.Ieq, l, Val.Virtual(addr)) if !r.isCanonical =>
            Val.False
          case (Comp.Ine, Val.Virtual(addr), r) if !r.isCanonical =>
            Val.True
          case (Comp.Ine, l, Val.Virtual(addr)) if !r.isCanonical =>
            Val.True
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
                log(
                  s"isinstanceof ${obj.ty.show} a ${cls.ty.show} ? ${res.show}")
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
          case Val.Virtual(addr) =>
            val instance = state.derefVirtual(addr)
            if (boxname == instance.cls.name) {
              instance.values(0)
            } else {
              Val.Zero(Type.Nothing)
            }
          case value =>
            emit.unbox(boxty, materialize(value), unwind)
        }
      case Op.Arrayalloc(ty, init) =>
        eval(init) match {
          case Val.Int(count) if count < 4096 =>
            Val.Virtual(state.allocArray(ty, count))
          case Val.ArrayValue(_, values) if values.size < 4096 =>
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
          case (Val.Virtual(addr), Val.Int(offset)) =>
            val instance = state.derefVirtual(addr)
            instance.values(offset)
          case (arr, idx) =>
            emit.arrayload(ty, materialize(arr), materialize(idx), unwind)
        }
      case Op.Arraystore(ty, arr, idx, value) =>
        (eval(arr), eval(idx)) match {
          case (Val.Virtual(addr), Val.Int(offset)) =>
            val instance = state.derefVirtual(addr)
            instance.values(offset) = eval(value)
            Val.Unit
          case (arr, idx) =>
            def fallback =
              emit.arraystore(ty,
                              materialize(arr),
                              materialize(idx),
                              materialize(eval(value)),
                              unwind)
            eval(value) match {
              case Val.Virtual(addr) =>
                arr.ty match {
                  case ArrayRef(elemty, _) =>
                    state.deref(addr).cls.ty match {
                      case BoxRef(boxty) if elemty == boxty =>
                        val boxvalue = state.derefVirtual(addr).values(0)
                        emit.arraystore(elemty,
                                        materialize(arr),
                                        materialize(idx),
                                        materialize(boxvalue),
                                        unwind)
                      case _ =>
                        fallback
                    }
                  case _ =>
                    fallback
                }
              case _ =>
                fallback
            }
        }
      case Op.Arraylength(arr) =>
        eval(arr) match {
          case Val.Virtual(addr) =>
            val instance = state.derefVirtual(addr)
            Val.Int(instance.values.length)
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

  def eval(bin: Bin, ty: Type, l: Val, r: Val)(implicit state: State): Val = {
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
          case (Val.Int(l), Val.Int(r)) if r != 0 =>
            Val.Int(l / r)
          case (Val.Long(l), Val.Long(r)) if r != 0L =>
            Val.Long(l / r)
          case _ =>
            bailOut
        }
      case Bin.Udiv =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) if r != 0 =>
            Val.Int(java.lang.Integer.divideUnsigned(l, r))
          case (Val.Long(l), Val.Long(r)) if r != 0 =>
            Val.Long(java.lang.Long.divideUnsigned(l, r))
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
          case (Val.Int(l), Val.Int(r)) if r != 0 =>
            Val.Int(l % r)
          case (Val.Long(l), Val.Long(r)) if r != 0L =>
            Val.Long(l % r)
          case _ =>
            bailOut
        }
      case Bin.Urem =>
        (l, r) match {
          case (Val.Int(l), Val.Int(r)) if r != 0 =>
            Val.Int(java.lang.Integer.remainderUnsigned(l, r))
          case (Val.Long(l), Val.Long(r)) if r != 0L =>
            Val.Long(java.lang.Long.remainderUnsigned(l, r))
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
          case (Val.Bool(l), Val.Bool(r))           => Val.Bool(l == r)
          case (Val.Int(l), Val.Int(r))             => Val.Bool(l == r)
          case (Val.Long(l), Val.Long(r))           => Val.Bool(l == r)
          case (Val.Null, Val.Null)                 => Val.True
          case (Val.Virtual(l), Val.Virtual(r))     => Val.Bool(l == r)
          case (Val.Global(l, _), Val.Global(r, _)) => Val.Bool(l == r)
          case (Val.Null | _: Val.Virtual | _: Val.Global,
                Val.Null | _: Val.Virtual | _: Val.Global) =>
            Val.False
          case _ => bailOut
        }
      case Comp.Ine =>
        (l, r) match {
          case (Val.Bool(l), Val.Bool(r))           => Val.Bool(l != r)
          case (Val.Int(l), Val.Int(r))             => Val.Bool(l != r)
          case (Val.Long(l), Val.Long(r))           => Val.Bool(l != r)
          case (Val.Null, Val.Null)                 => Val.False
          case (Val.Virtual(l), Val.Virtual(r))     => Val.Bool(l != r)
          case (Val.Global(l, _), Val.Global(r, _)) => Val.Bool(l != r)
          case (Val.Null | _: Val.Virtual | _: Val.Global,
                Val.Null | _: Val.Virtual | _: Val.Global) =>
            Val.True
          case _ => bailOut
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
        bailOut
      case Conv.Fptosi =>
        (value, ty) match {
          case (Val.Float(v), Type.Int)   => Val.Int(v.toInt)
          case (Val.Double(v), Type.Int)  => Val.Int(v.toInt)
          case (Val.Float(v), Type.Long)  => Val.Long(v.toLong)
          case (Val.Double(v), Type.Long) => Val.Long(v.toLong)
          case _                          => bailOut
        }
      case Conv.Uitofp =>
        bailOut
      case Conv.Sitofp =>
        (value, ty) match {
          case (Val.Int(v), Type.Float)   => Val.Float(v.toFloat)
          case (Val.Int(v), Type.Double)  => Val.Double(v.toDouble)
          case (Val.Long(v), Type.Float)  => Val.Float(v.toFloat)
          case (Val.Long(v), Type.Double) => Val.Double(v.toDouble)
          case _                          => bailOut
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
          case (Val.Int(value), Type.Float) =>
            Val.Float(java.lang.Float.intBitsToFloat(value))
          case (Val.Long(value), Type.Double) =>
            Val.Double(java.lang.Double.longBitsToDouble(value))
          case (Val.Float(value), Type.Int) =>
            Val.Int(java.lang.Float.floatToRawIntBits(value))
          case (Val.Double(value), Type.Long) =>
            Val.Long(java.lang.Double.doubleToRawLongBits(value))
          case _ =>
            bailOut
        }
    }
  }

  def eval(value: Val)(implicit state: State): Val = value match {
    case Val.Local(local, _) if local.id >= 0 =>
      state.loadLocal(local) match {
        case value: Val.Virtual =>
          eval(value)
        case value =>
          value
      }
    case Val.Virtual(addr) if state.escaped(addr) =>
      state.derefEscaped(addr).escapedValue
    case Val.String(value) =>
      Val.Virtual(state.allocString(value))
    case _ =>
      value.canonicalize
  }
}
