package scala.scalanative
package interflow

import scala.collection.mutable
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.linker._
import scala.scalanative.codegen.MemoryLayout
import scala.scalanative.util.{unreachable, And}

private[interflow] trait Eval { self: Interflow =>
  def interflow: Interflow = self
  final val preserveDebugInfo: Boolean =
    self.config.compilerConfig.sourceLevelDebuggingConfig.generateLocalVariables

  def run(
      insts: Array[nir.Inst],
      offsets: Map[nir.Local, Int],
      from: nir.Local,
      debugInfo: DebugInfo,
      scopeMapping: nir.ScopeId => nir.ScopeId
  )(implicit state: State): nir.Inst.Cf = {
    import state.{materialize, delay}

    var pc = offsets(from)

    if (preserveDebugInfo && pc == 0) {
      val nir.Inst.Label(_, params) = insts.head: @unchecked
      for {
        param <- params
        name <- debugInfo.localNames.get(param.id)
      } {
        state.localNames.getOrElseUpdate(param.id, name)
      }
    }

    pc += 1

    // Implicit scopeId required for materialization of insts other than Inst.Let
    implicit var lastScopeId = scopeMapping(nir.ScopeId.TopLevel)
    while (true) {
      val inst = insts(pc)
      implicit val srcPosition: nir.SourcePosition = inst.pos
      def bailOut =
        throw BailOut("can't eval inst: " + inst.show)
      inst match {
        case _: nir.Inst.Label =>
          unreachable
        case let @ nir.Inst.Let(local, op, unwind) =>
          lastScopeId = scopeMapping(let.scopeId)
          if (unwind ne nir.Next.None) {
            throw BailOut("try-catch")
          }
          val value = eval(op)
          if (preserveDebugInfo) {
            val localName = debugInfo.localNames.get(local)
            value match {
              case nir.Val.Local(id, _) =>
                localName.foreach(state.localNames.getOrElseUpdate(id, _))
              case nir.Val.Virtual(addr) =>
                localName.foreach(state.virtualNames.getOrElseUpdate(addr, _))
              case _ => ()
            }
          }
          if (value.ty == nir.Type.Nothing) {
            return nir.Inst.Unreachable(unwind)(inst.pos)
          } else {
            val ty = value match {
              case InstanceRef(ty) => ty
              case _               => value.ty
            }
            val shortUnitValue =
              if (ty == nir.Type.Unit) nir.Val.Unit else value
            state.storeLocal(local, shortUnitValue)
            pc += 1
          }
        case nir.Inst.Ret(v) =>
          return nir.Inst.Ret(eval(v))
        case nir.Inst.Jump(nir.Next.Label(target, args)) =>
          val evalArgs = args.map(eval)
          val next = nir.Next.Label(target, evalArgs)
          return nir.Inst.Jump(next)
        case nir.Inst.If(
              cond,
              nir.Next.Label(thenTarget, thenArgs),
              nir.Next.Label(elseTarget, elseArgs)
            ) =>
          def thenNext =
            nir.Next.Label(thenTarget, thenArgs.map(eval))
          def elseNext =
            nir.Next.Label(elseTarget, elseArgs.map(eval))
          val next = eval(cond) match {
            case nir.Val.True =>
              return nir.Inst.Jump(thenNext)
            case nir.Val.False =>
              return nir.Inst.Jump(elseNext)
            case cond =>
              return nir.Inst.If(materialize(cond), thenNext, elseNext)
          }
        case nir.Inst.Switch(
              scrut,
              nir.Next.Label(defaultTarget, defaultArgs),
              cases
            ) =>
          def defaultNext =
            nir.Next.Label(defaultTarget, defaultArgs.map(eval))
          eval(scrut) match {
            case value if value.isCanonical =>
              val next = cases
                .collectFirst {
                  case nir.Next.Case(
                        caseValue,
                        nir.Next.Label(caseTarget, caseArgs)
                      ) if caseValue == value =>
                    val evalArgs = caseArgs.map(eval)
                    val next = nir.Next.Label(caseTarget, evalArgs)
                    next
                }
                .getOrElse(defaultNext)
              return nir.Inst.Jump(next)
            case scrut =>
              return nir.Inst.Switch(materialize(scrut), defaultNext, cases)
          }
        case nir.Inst.Throw(v, unwind) =>
          if (unwind ne nir.Next.None) {
            throw BailOut("try-catch")
          }
          return nir.Inst.Throw(eval(v), nir.Next.None)
        case nir.Inst.Unreachable(unwind) =>
          if (unwind ne nir.Next.None) {
            throw BailOut("try-catch")
          }
          return nir.Inst.Unreachable(nir.Next.None)
        case _ =>
          bailOut
      }
    }

    unreachable
  }

  def eval(
      op: nir.Op
  )(implicit
      state: State,
      analysis: ReachabilityAnalysis.Result,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): nir.Val = {
    import state.{emit, materialize, delay}
    def bailOut =
      throw BailOut("can't eval op: " + op.show)
    op match {
      case nir.Op.Call(sig, meth, args) =>
        val emeth = eval(meth)

        def nonIntrinsic = {
          val eargs = args.map(eval)
          val argtys = eargs.map {
            case VirtualRef(_, cls, _) => cls.ty
            case DelayedRef(op)        => op.resty
            case value                 => value.ty
          }

          val (dsig, dtarget) = emeth match {
            case nir.Val.Global(name: nir.Global.Member, _) =>
              visitDuplicate(name, argtys)
                .map { defn =>
                  (defn.ty, nir.Val.Global(defn.name, nir.Type.Ptr))
                }
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

            emit(nir.Op.Call(dsig, mtarget, margs))
          }

          (dtarget, eargs) match {
            case (nir.Val.Global(name: nir.Global.Member, _), _)
                if shallInline(name, eargs) =>
              `inline`(name, eargs)
            case PolyInlined(polyInlined) =>
              polyInlined
            case _ =>
              fallback
          }
        }

        emeth match {
          case nir.Val.Global(name: nir.Global.Member, _) =>
            intrinsic(sig, name, args).getOrElse {
              nonIntrinsic
            }
          case _ =>
            nonIntrinsic
        }
      case op @ nir.Op.Load(ty, ptr, _) =>
        emit(
          op.copy(ptr = materialize(eval(ptr)))
        )
      case op @ nir.Op.Store(ty, ptr, value, _) =>
        emit(
          op.copy(
            ptr = materialize(eval(ptr)),
            value = materialize(eval(value))
          )
        )
      case nir.Op.Elem(ty, ptr, indexes) =>
        delay(nir.Op.Elem(ty, eval(ptr), indexes.map(eval)))
      case nir.Op.Extract(aggr, indexes) =>
        delay(nir.Op.Extract(eval(aggr), indexes))
      case nir.Op.Insert(aggr, value, indexes) =>
        delay(nir.Op.Insert(eval(aggr), eval(value), indexes))
      case nir.Op.Stackalloc(ty, n) =>
        emit(nir.Op.Stackalloc(ty, materialize(eval(n))))
      case op @ nir.Op.Bin(bin, ty, l, r) =>
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
      case nir.Op.Comp(comp, ty, l, r) =>
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
      case nir.Op.Conv(conv, ty, value) =>
        eval(value) match {
          case value if value.isCanonical =>
            eval(conv, ty, value)
          case value =>
            combine(conv, ty, value)
        }
      case nir.Op.Classalloc(ClassRef(cls), zone) =>
        val zonePtr = zone.map(instance => materialize(eval(instance)))
        nir.Val.Virtual(state.allocClass(cls, zonePtr))
      case nir.Op.Fieldload(ty, rawObj, name @ FieldRef(cls, fld)) =>
        eval(rawObj) match {
          case VirtualRef(_, _, values)   => values(fld.index)
          case DelayedRef(op: nir.Op.Box) =>
            val name = op.ty.asInstanceOf[nir.Type.RefKind].className
            eval(nir.Op.Unbox(nir.Type.Ref(name), rawObj))
          case obj =>
            val objty = obj match {
              case InstanceRef(ty) => ty
              case _               => obj.ty
            }
            objty match {
              case refty: nir.Type.RefKind
                  if nir.Type.boxClasses.contains(refty.className)
                    && !refty.isNullable =>
                eval(nir.Op.Unbox(nir.Type.Ref(refty.className), rawObj))
              case _ =>
                emit(nir.Op.Fieldload(ty, materialize(obj), name))
            }
        }
      case nir.Op.Fieldstore(ty, obj, name @ FieldRef(cls, fld), value) =>
        eval(obj) match {
          case VirtualRef(_, _, values) =>
            values(fld.index) = eval(value)
            nir.Val.Unit
          case obj =>
            emit(
              nir.Op
                .Fieldstore(
                  ty,
                  materialize(obj),
                  name,
                  materialize(eval(value))
                )
            )
        }

      case nir.Op.Field(rawObj, name) =>
        val obj = eval(rawObj)
        visitRoot(name)
        delay(nir.Op.Field(materialize(obj), name))

      case nir.Op.Method(rawObj, sig) =>
        val obj = eval(rawObj)
        val objty = {
          /* If method is not virtual (e.g. constructor) we need to ensure that
           * we would fetch for expected type targets (rawObj) instead of real (evaluated) type
           * It might result in calling wrong method and lead to infinite loops, e.g. issue #1909
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
          case nir.Type.Null =>
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
          emit(nir.Op.Method(materialize(obj), sig))
          nir.Val.Zero(nir.Type.Nothing)
        } else if (targets.size == 1) {
          nir.Val.Global(targets.head, nir.Type.Ptr)
        } else {
          targets.foreach(visitRoot)
          delay(nir.Op.Method(materialize(obj), sig))
        }
      case nir.Op.Dynmethod(obj, dynsig) =>
        analysis.dynimpls.foreach {
          case impl @ nir.Global.Member(_, sig) if sig.toProxy == dynsig =>
            visitRoot(impl)
          case _ =>
            ()
        }
        emit(nir.Op.Dynmethod(materialize(eval(obj)), dynsig))
      case nir.Op.Module(clsName) =>
        val isPure = isPureModule(clsName)
        val isAllowlisted = Allowlist.pure.contains(clsName)
        val canDelay = isPure || isAllowlisted

        if (canDelay) delay(nir.Op.Module(clsName))
        else emit(nir.Op.Module(clsName))

      case nir.Op.As(ty, rawObj) =>
        val refty = ty match {
          case ty: nir.Type.RefKind => ty
          case _                    => bailOut
        }
        val obj = eval(rawObj)
        def fallback =
          emit(nir.Op.As(ty, materialize(obj)))
        val objty = obj match {
          case InstanceRef(ty) =>
            ty
          case _ =>
            obj.ty
        }
        objty match {
          case nir.Type.Null =>
            nir.Val.Null
          case ScopeRef(scope) if Sub.is(scope, refty) =>
            obj
          case _ =>
            fallback
        }
      case nir.Op.Is(ty, rawObj) =>
        val refty = ty match {
          case ty: nir.Type.RefKind => ty
          case _                    => bailOut
        }
        val obj = eval(rawObj)
        def fallback =
          delay(nir.Op.Is(refty, obj))
        def objNotNull =
          delay(nir.Op.Comp(nir.Comp.Ine, nir.Rt.Object, obj, nir.Val.Null))
        val objty = obj match {
          case InstanceRef(ty) =>
            ty
          case _ =>
            obj.ty
        }
        objty match {
          case nir.Type.Null =>
            nir.Val.False
          case And(scoperef: nir.Type.RefKind, ScopeRef(scope)) =>
            if (Sub.is(scope, refty)) {
              if (!scoperef.isNullable) {
                nir.Val.True
              } else {
                objNotNull
              }
            } else if (scoperef.isExact) {
              nir.Val.False
            } else {
              fallback
            }
          case _ =>
            fallback
        }
      case nir.Op.Copy(v) =>
        eval(v)
      case nir.Op.SizeOf(ty) =>
        if (ty.hasKnownSize) nir.Val.Size(MemoryLayout.sizeOf(ty))
        else emit(op)
      case nir.Op.AlignmentOf(ty) =>
        nir.Val.Size(MemoryLayout.alignmentOf(ty))
      case nir.Op.Box(boxty @ nir.Type.Ref(boxname, _, _), value) =>
        // Pointer boxes are special because null boxes to null,
        // which breaks the invariant that all virtual allocations
        // are in fact non-null. We handle them as a delayed op instead.
        if (!nir.Type.isPtrBox(boxty)) {
          nir.Val.Virtual(state.allocBox(boxname, eval(value)))
        } else {
          delay(nir.Op.Box(boxty, eval(value)))
        }
      case nir.Op.Unbox(boxty @ nir.Type.Ref(boxname, _, _), value) =>
        eval(value) match {
          case VirtualRef(_, cls, Array(value)) if boxname == cls.name =>
            value
          case ConvRef(
                nir.Conv.Bitcast,
                _,
                VirtualRef(_, cls, Array(value))
              ) if boxname == cls.name =>
            value
          case DelayedRef(nir.Op.Box(nir.Type.Ref(innername, _, _), innervalue))
              if innername == boxname =>
            innervalue
          case value =>
            emit(nir.Op.Unbox(boxty, materialize(value)))
        }
      case nir.Op.Arrayalloc(ty, init, zone) =>
        eval(init) match {
          case nir.Val.Int(count) if count <= 128 =>
            nir.Val.Virtual(
              state.allocArray(
                ty,
                count,
                zone.map(instance => materialize(eval(instance)))
              )
            )
          case nir.Val.ArrayValue(_, values) if values.size <= 128 =>
            val addr =
              state.allocArray(
                ty,
                values.size,
                zone.map(instance => materialize(eval(instance)))
              )
            val instance = state.derefVirtual(addr)
            values.zipWithIndex.foreach {
              case (v, idx) =>
                instance.values(idx) = v
            }
            nir.Val.Virtual(addr)
          case init =>
            emit(
              nir.Op.Arrayalloc(
                ty,
                materialize(init),
                zone.map(instance => materialize(eval(instance)))
              )
            )
        }
      case nir.Op.Arrayload(ty, arr, idx) =>
        (eval(arr), eval(idx)) match {
          case (VirtualRef(_, _, values), nir.Val.Int(offset))
              if inBounds(values, offset) =>
            values(offset)
          case (arr, idx) =>
            emit(nir.Op.Arrayload(ty, materialize(arr), materialize(idx)))
        }
      case nir.Op.Arraystore(ty, arr, idx, value) =>
        (eval(arr), eval(idx)) match {
          case (VirtualRef(_, _, values), nir.Val.Int(offset))
              if inBounds(values, offset) =>
            values(offset) = eval(value)
            nir.Val.Unit
          case (arr, idx) =>
            emit(
              nir.Op.Arraystore(
                ty,
                materialize(arr),
                materialize(idx),
                materialize(eval(value))
              )
            )
        }
      case nir.Op.Arraylength(arr) =>
        eval(arr) match {
          case VirtualRef(_, _, values) => nir.Val.Int(values.length)
          case arr => emit(nir.Op.Arraylength(materialize(arr)))
        }
      case nir.Op.Var(ty) =>
        nir.Val.Local(state.newVar(ty), nir.Type.Var(ty))
      case nir.Op.Varload(slot) =>
        val nir.Val.Local(local, _) = eval(slot): @unchecked
        state.loadVar(local)
      case nir.Op.Varstore(slot, value) =>
        val nir.Val.Local(local, _) = eval(slot): @unchecked
        state.storeVar(local, eval(value))
        nir.Val.Unit
      case _ => util.unreachable
    }
  }

  def eval(bin: nir.Bin, ty: nir.Type, l: nir.Val, r: nir.Val)(implicit
      state: State,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): nir.Val = {
    import state.{emit, materialize}
    def fallback =
      emit(nir.Op.Bin(bin, ty, materialize(l), materialize(r)))
    def bailOut =
      throw BailOut(s"can't eval bin op: $bin[${ty.show}] ${l.show}, ${r.show}")
    bin match {
      case nir.Bin.Iadd =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l + r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l + r)
          case _                                  => bailOut
        }
      case nir.Bin.Fadd =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Float(l + r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Double(l + r)
          case _                                      => bailOut
        }
      case nir.Bin.Isub =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l - r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l - r)
          case _                                  => bailOut
        }
      case nir.Bin.Fsub =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Float(l - r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Double(l - r)
          case _                                      => bailOut
        }
      case nir.Bin.Imul =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l * r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l * r)
          case _                                  => bailOut
        }
      case nir.Bin.Fmul =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Float(l * r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Double(l * r)
          case _                                      => bailOut
        }
      case nir.Bin.Sdiv =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            if (r != 0) {
              nir.Val.Int(l / r)
            } else {
              fallback
            }
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            if (r != 0L) {
              nir.Val.Long(l / r)
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case nir.Bin.Udiv =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            if (r != 0) {
              nir.Val.Int(java.lang.Integer.divideUnsigned(l, r))
            } else {
              fallback
            }
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            if (r != 0) {
              nir.Val.Long(java.lang.Long.divideUnsigned(l, r))
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case nir.Bin.Fdiv =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Float(l / r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Double(l / r)
          case _                                      => bailOut
        }
      case nir.Bin.Srem =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            if (r != 0) {
              nir.Val.Int(l % r)
            } else {
              fallback
            }
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            if (r != 0L) {
              nir.Val.Long(l % r)
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case nir.Bin.Urem =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            if (r != 0) {
              nir.Val.Int(java.lang.Integer.remainderUnsigned(l, r))
            } else {
              fallback
            }
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            if (r != 0L) {
              nir.Val.Long(java.lang.Long.remainderUnsigned(l, r))
            } else {
              fallback
            }
          case _ =>
            bailOut
        }
      case nir.Bin.Frem =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Float(l % r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Double(l % r)
          case _                                      => bailOut
        }
      case nir.Bin.Shl =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l << r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l << r)
          case _                                  => bailOut
        }
      case nir.Bin.Lshr =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l >>> r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l >>> r)
          case _                                  => bailOut
        }
      case nir.Bin.Ashr =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l >> r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l >> r)
          case _                                  => bailOut
        }
      case nir.Bin.And =>
        (l, r) match {
          case (nir.Val.Bool(l), nir.Val.Bool(r)) => nir.Val.Bool(l & r)
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l & r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l & r)
          case _                                  => bailOut
        }
      case nir.Bin.Or =>
        (l, r) match {
          case (nir.Val.Bool(l), nir.Val.Bool(r)) => nir.Val.Bool(l | r)
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l | r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l | r)
          case _                                  => bailOut
        }
      case nir.Bin.Xor =>
        (l, r) match {
          case (nir.Val.Bool(l), nir.Val.Bool(r)) => nir.Val.Bool(l ^ r)
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Int(l ^ r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Long(l ^ r)
          case _                                  => bailOut
        }
    }
  }

  def eval(comp: nir.Comp, ty: nir.Type, l: nir.Val, r: nir.Val)(implicit
      state: State
  ): nir.Val = {
    def bailOut =
      throw BailOut(
        s"can't eval comp op: $comp[${ty.show}] ${l.show}, ${r.show}"
      )
    comp match {
      case nir.Comp.Ieq =>
        (l, r) match {
          case (nir.Val.Bool(l), nir.Val.Bool(r)) => nir.Val.Bool(l == r)
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Bool(l == r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Bool(l == r)
          case (nir.Val.Size(l), nir.Val.Size(r)) => nir.Val.Bool(l == r)
          case (nir.Val.Null, nir.Val.Null)       => nir.Val.True
          case (nir.Val.Global(l, _), nir.Val.Global(r, _)) =>
            nir.Val.Bool(l == r)
          case (
                nir.Val.Null | _: nir.Val.Global,
                nir.Val.Null | _: nir.Val.Global
              ) =>
            nir.Val.False
          case _ => bailOut
        }
      case nir.Comp.Ine =>
        (l, r) match {
          case (nir.Val.Bool(l), nir.Val.Bool(r)) => nir.Val.Bool(l != r)
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Bool(l != r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Bool(l != r)
          case (nir.Val.Size(l), nir.Val.Size(r)) => nir.Val.Bool(l != r)
          case (nir.Val.Null, nir.Val.Null)       => nir.Val.False
          case (nir.Val.Global(l, _), nir.Val.Global(r, _)) =>
            nir.Val.Bool(l != r)
          case (
                nir.Val.Null | _: nir.Val.Global,
                nir.Val.Null | _: nir.Val.Global
              ) =>
            nir.Val.True
          case _ => bailOut
        }
      case nir.Comp.Ugt =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            nir.Val.Bool(java.lang.Integer.compareUnsigned(l, r) > 0)
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) > 0)
          case (nir.Val.Size(l), nir.Val.Size(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) > 0)
          case _ =>
            bailOut
        }
      case nir.Comp.Uge =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            nir.Val.Bool(java.lang.Integer.compareUnsigned(l, r) >= 0)
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) >= 0)
          case (nir.Val.Size(l), nir.Val.Size(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) >= 0)
          case _ =>
            bailOut
        }
      case nir.Comp.Ult =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            nir.Val.Bool(java.lang.Integer.compareUnsigned(l, r) < 0)
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) < 0)
          case (nir.Val.Size(l), nir.Val.Size(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) < 0)
          case _ =>
            bailOut
        }
      case nir.Comp.Ule =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r)) =>
            nir.Val.Bool(java.lang.Integer.compareUnsigned(l, r) <= 0)
          case (nir.Val.Long(l), nir.Val.Long(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) <= 0)
          case (nir.Val.Size(l), nir.Val.Size(r)) =>
            nir.Val.Bool(java.lang.Long.compareUnsigned(l, r) <= 0)
          case _ =>
            bailOut
        }
      case nir.Comp.Sgt =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Bool(l > r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Bool(l > r)
          case (nir.Val.Size(l), nir.Val.Size(r)) => nir.Val.Bool(l > r)
          case _                                  => bailOut
        }
      case nir.Comp.Sge =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Bool(l >= r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Bool(l >= r)
          case (nir.Val.Size(l), nir.Val.Size(r)) => nir.Val.Bool(l >= r)
          case _                                  => bailOut
        }
      case nir.Comp.Slt =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Bool(l < r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Bool(l < r)
          case (nir.Val.Size(l), nir.Val.Size(r)) => nir.Val.Bool(l < r)
          case _                                  => bailOut
        }
      case nir.Comp.Sle =>
        (l, r) match {
          case (nir.Val.Int(l), nir.Val.Int(r))   => nir.Val.Bool(l <= r)
          case (nir.Val.Long(l), nir.Val.Long(r)) => nir.Val.Bool(l <= r)
          case (nir.Val.Size(l), nir.Val.Size(r)) => nir.Val.Bool(l <= r)
          case _                                  => bailOut
        }
      case nir.Comp.Feq =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Bool(l == r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Bool(l == r)
          case _                                      => bailOut
        }
      case nir.Comp.Fne =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Bool(l != r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Bool(l != r)
          case _                                      => bailOut
        }
      case nir.Comp.Fgt =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Bool(l > r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Bool(l > r)
          case _                                      => bailOut
        }
      case nir.Comp.Fge =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Bool(l >= r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Bool(l >= r)
          case _                                      => bailOut
        }
      case nir.Comp.Flt =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Bool(l < r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Bool(l < r)
          case _                                      => bailOut
        }
      case nir.Comp.Fle =>
        (l, r) match {
          case (nir.Val.Float(l), nir.Val.Float(r))   => nir.Val.Bool(l <= r)
          case (nir.Val.Double(l), nir.Val.Double(r)) => nir.Val.Bool(l <= r)
          case _                                      => bailOut
        }
    }
  }

  def eval(conv: nir.Conv, ty: nir.Type, value: nir.Val)(implicit
      state: State
  ): nir.Val = {
    def bailOut =
      throw BailOut(s"can't eval conv op: $conv[${ty.show}] ${value.show}")
    conv match {
      case _ if ty == value.ty                     => value
      case nir.Conv.SSizeCast | nir.Conv.ZSizeCast =>
        def size(ty: nir.Type) = ty match {
          case nir.Type.Size =>
            if (platform.is32Bit) 32 else 64
          case i: nir.Type.FixedSizeI =>
            i.width
          case o =>
            bailOut
        }
        val fromSize = size(value.ty)
        val toSize = size(ty)

        if (fromSize == toSize) eval(nir.Conv.Bitcast, ty, value)
        else if (fromSize > toSize) eval(nir.Conv.Trunc, ty, value)
        else if (conv == nir.Conv.ZSizeCast) eval(nir.Conv.Zext, ty, value)
        else eval(nir.Conv.Sext, ty, value)

      case nir.Conv.Trunc =>
        (value, ty) match {
          case (nir.Val.Char(v), nir.Type.Byte)  => nir.Val.Byte(v.toByte)
          case (nir.Val.Short(v), nir.Type.Byte) => nir.Val.Byte(v.toByte)
          case (nir.Val.Int(v), nir.Type.Byte)   => nir.Val.Byte(v.toByte)
          case (nir.Val.Int(v), nir.Type.Short)  => nir.Val.Short(v.toShort)
          case (nir.Val.Int(v), nir.Type.Char)   => nir.Val.Char(v.toChar)
          case (nir.Val.Long(v), nir.Type.Byte)  => nir.Val.Byte(v.toByte)
          case (nir.Val.Long(v), nir.Type.Short) => nir.Val.Short(v.toShort)
          case (nir.Val.Long(v), nir.Type.Int)   => nir.Val.Int(v.toInt)
          case (nir.Val.Long(v), nir.Type.Char)  => nir.Val.Char(v.toChar)
          case (nir.Val.Size(v), nir.Type.Byte)  => nir.Val.Byte(v.toByte)
          case (nir.Val.Size(v), nir.Type.Short) => nir.Val.Short(v.toShort)
          case (nir.Val.Size(v), nir.Type.Int) if !platform.is32Bit =>
            nir.Val.Int(v.toInt)
          case (nir.Val.Size(v), nir.Type.Char) => nir.Val.Char(v.toChar)
          case _                                => bailOut
        }
      case nir.Conv.Zext =>
        (value, ty) match {
          case (nir.Val.Char(v), nir.Type.Int) =>
            nir.Val.Int(v.toInt)
          case (nir.Val.Char(v), nir.Type.Long) =>
            nir.Val.Long(v.toLong)
          case (nir.Val.Short(v), nir.Type.Int) =>
            nir.Val.Int(v.toChar.toInt)
          case (nir.Val.Short(v), nir.Type.Long) =>
            nir.Val.Long(v.toChar.toLong)
          case (nir.Val.Int(v), nir.Type.Long) =>
            nir.Val.Long(java.lang.Integer.toUnsignedLong(v))
          case (nir.Val.Int(v), nir.Type.Size) if !platform.is32Bit =>
            nir.Val.Size(java.lang.Integer.toUnsignedLong(v))
          case (nir.Val.Size(v), nir.Type.Long) if platform.is32Bit =>
            nir.Val.Long(java.lang.Integer.toUnsignedLong(v.toInt))
          case _ =>
            bailOut
        }
      case nir.Conv.Sext =>
        (value, ty) match {
          case (nir.Val.Byte(v), nir.Type.Short) => nir.Val.Short(v.toShort)
          case (nir.Val.Byte(v), nir.Type.Char)  => nir.Val.Char(v.toChar)
          case (nir.Val.Byte(v), nir.Type.Int)   => nir.Val.Int(v.toInt)
          case (nir.Val.Byte(v), nir.Type.Long)  => nir.Val.Long(v.toLong)
          case (nir.Val.Short(v), nir.Type.Int)  => nir.Val.Int(v.toInt)
          case (nir.Val.Short(v), nir.Type.Long) => nir.Val.Long(v.toLong)
          case (nir.Val.Int(v), nir.Type.Long)   => nir.Val.Long(v.toLong)
          case (nir.Val.Int(v), nir.Type.Size) if !platform.is32Bit =>
            nir.Val.Size(v.toLong)
          case (nir.Val.Size(v), nir.Type.Long) if platform.is32Bit =>
            nir.Val.Long(v.toInt.toLong)
          case _ => bailOut
        }
      case nir.Conv.Fptrunc =>
        (value, ty) match {
          case (nir.Val.Double(v), nir.Type.Float) => nir.Val.Float(v.toFloat)
          case _                                   => bailOut
        }
      case nir.Conv.Fpext =>
        (value, ty) match {
          case (nir.Val.Float(v), nir.Type.Double) => nir.Val.Double(v.toDouble)
          case _                                   => bailOut
        }
      case nir.Conv.Fptoui =>
        (value, ty) match {
          case (nir.Val.Float(v), nir.Type.Char)  => nir.Val.Char(v.toChar)
          case (nir.Val.Double(v), nir.Type.Char) => nir.Val.Char(v.toChar)
          case _                                  => bailOut
        }
      case nir.Conv.Fptosi =>
        (value, ty) match {
          case (nir.Val.Float(v), nir.Type.Int)   => nir.Val.Int(v.toInt)
          case (nir.Val.Double(v), nir.Type.Int)  => nir.Val.Int(v.toInt)
          case (nir.Val.Float(v), nir.Type.Long)  => nir.Val.Long(v.toLong)
          case (nir.Val.Double(v), nir.Type.Long) => nir.Val.Long(v.toLong)
          case _                                  => bailOut
        }
      case nir.Conv.Uitofp =>
        (value, ty) match {
          case (nir.Val.Char(v), nir.Type.Float) =>
            nir.Val.Float(v.toInt.toFloat)
          case (nir.Val.Char(v), nir.Type.Double) =>
            nir.Val.Double(v.toInt.toFloat)
          case _ => bailOut
        }
      case nir.Conv.Sitofp =>
        (value, ty) match {
          case (nir.Val.Byte(v), nir.Type.Float)   => nir.Val.Float(v.toFloat)
          case (nir.Val.Byte(v), nir.Type.Double)  => nir.Val.Double(v.toDouble)
          case (nir.Val.Short(v), nir.Type.Float)  => nir.Val.Float(v.toFloat)
          case (nir.Val.Short(v), nir.Type.Double) => nir.Val.Double(v.toDouble)
          case (nir.Val.Int(v), nir.Type.Float)    => nir.Val.Float(v.toFloat)
          case (nir.Val.Int(v), nir.Type.Double)   => nir.Val.Double(v.toDouble)
          case (nir.Val.Long(v), nir.Type.Float)   => nir.Val.Float(v.toFloat)
          case (nir.Val.Long(v), nir.Type.Double)  => nir.Val.Double(v.toDouble)
          case (nir.Val.Size(v), nir.Type.Float)   => nir.Val.Float(v.toFloat)
          case (nir.Val.Size(v), nir.Type.Double)  => nir.Val.Double(v.toDouble)
          case _                                   => bailOut
        }
      case nir.Conv.Ptrtoint =>
        (value, ty) match {
          case (nir.Val.Null, nir.Type.Long) => nir.Val.Long(0L)
          case (nir.Val.Null, nir.Type.Int)  => nir.Val.Int(0)
          case (nir.Val.Null, nir.Type.Size) => nir.Val.Size(0)
          case _                             => bailOut
        }
      case nir.Conv.Inttoptr =>
        (value, ty) match {
          case (nir.Val.Long(0L), nir.Type.Ptr) => nir.Val.Null
          case (nir.Val.Int(0L), nir.Type.Ptr)  => nir.Val.Null
          case (nir.Val.Size(0L), nir.Type.Ptr) => nir.Val.Null
          case _                                => bailOut
        }
      case nir.Conv.Bitcast =>
        (value, ty) match {
          case (value, ty) if value.ty == ty =>
            value
          case (nir.Val.Char(value), nir.Type.Short) =>
            nir.Val.Short(value.toShort)
          case (nir.Val.Short(value), nir.Type.Char) =>
            nir.Val.Char(value.toChar)
          case (nir.Val.Int(value), nir.Type.Float) =>
            nir.Val.Float(java.lang.Float.intBitsToFloat(value))
          case (nir.Val.Long(value), nir.Type.Double) =>
            nir.Val.Double(java.lang.Double.longBitsToDouble(value))
          case (nir.Val.Float(value), nir.Type.Int) =>
            nir.Val.Int(java.lang.Float.floatToRawIntBits(value))
          case (nir.Val.Double(value), nir.Type.Long) =>
            nir.Val.Long(java.lang.Double.doubleToRawLongBits(value))
          case (nir.Val.Size(value), nir.Type.Int) if platform.is32Bit =>
            nir.Val.Int(value.toInt)
          case (nir.Val.Int(value), nir.Type.Size) if platform.is32Bit =>
            nir.Val.Size(value.toLong)
          case (nir.Val.Size(value), nir.Type.Long) if !platform.is32Bit =>
            nir.Val.Long(value)
          case (nir.Val.Long(value), nir.Type.Size) if !platform.is32Bit =>
            nir.Val.Size(value)
          case (nir.Val.Null, nir.Type.Ptr) =>
            nir.Val.Null
          case _ =>
            bailOut
        }
    }
  }

  def eval(value: nir.Val)(implicit
      state: State,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): nir.Val = {
    value match {
      case nir.Val.Local(local, _) if local.id >= 0 =>
        state.loadLocal(local) match {
          case value: nir.Val.Virtual => eval(value)
          case value                  => value
        }
      case nir.Val.Virtual(addr) if state.hasEscaped(addr) =>
        state.derefEscaped(addr).escapedValue
      case nir.Val.String(value) =>
        nir.Val.Virtual(state.allocString(value))
      case nir.Val.Global(name: nir.Global.Member, _) =>
        maybeOriginal(name).foreach {
          case defn if defn.attrs.isExtern =>
            visitRoot(defn.name)
          case _ =>
            ()
        }
        value
      case v @ nir.Val.ArrayValue(_, values) =>
        v.copy(values = values.map(eval(_)))
      case v @ nir.Val.StructValue(values) =>
        v.copy(values = values.map(eval(_)))
      case _ => value.canonicalize
    }
  }

  private def inBounds(values: Array[nir.Val], offset: Int): Boolean = {
    inBounds(values.length, offset)
  }

  private def inBounds(length: Int, offset: Int): Boolean = {
    offset >= 0 && offset < length
  }

  private def isPureModule(clsName: nir.Global.Top): Boolean = {
    var visiting = List[nir.Global.Top]()

    def isPureModule(clsName: nir.Global.Top): Boolean = {
      if (hasModulePurity(clsName)) {
        getModulePurity(clsName)
      } else {
        visiting = clsName :: visiting

        val init = clsName.member(nir.Sig.Ctor(Seq.empty))
        val isPure =
          !shallVisit(init) ||
            visitDuplicate(init, argumentTypes(init)).fold(false)(
              isPureModuleCtor
            )

        setModulePurity(clsName, isPure)
        isPure
      }
    }

    def isPureModuleCtor(defn: nir.Defn.Define): Boolean = {
      val nir.Inst.Label(_, nir.Val.Local(self, _) +: _) =
        defn.insts.head: @unchecked

      val canStoreTo = mutable.Set(self)
      val arrayLength = mutable.Map.empty[nir.Local, Int]

      defn.insts.foreach {
        case nir.Inst.Let(n, nir.Op.Arrayalloc(_, init, _), _) =>
          canStoreTo += n
          init match {
            case nir.Val.Int(size) =>
              arrayLength(n) = size
            case nir.Val.ArrayValue(_, elems) =>
              arrayLength(n) = elems.size
            case _ =>
              ()
          }
        case nir.Inst.Let(
              n,
              _: nir.Op.Classalloc | _: nir.Op.Box | _: nir.Op.Module,
              _
            ) =>
          canStoreTo += n
        case _ =>
          ()
      }

      def canStoreValue(v: nir.Val): Boolean = v match {
        case _ if v.isCanonical  => true
        case nir.Val.Local(n, _) => canStoreTo.contains(n)
        case _: nir.Val.String   => true
        case _                   => false
      }

      defn.insts.forall {
        case inst @ (_: nir.Inst.Throw | _: nir.Inst.Unreachable) =>
          false
        case _: nir.Inst.Label =>
          true
        case _: nir.Inst.Cf =>
          true
        case nir.Inst.Let(_, op, _) if op.isPure =>
          true
        case nir.Inst.Let(
              _,
              _: nir.Op.Classalloc | _: nir.Op.Arrayalloc | _: nir.Op.Box,
              _
            ) =>
          true
        case inst @ nir.Inst.Let(_, nir.Op.Module(name), _) =>
          if (!visiting.contains(name)) {
            isPureModule(name)
          } else {
            false
          }
        case nir.Inst.Let(_, nir.Op.Fieldload(_, nir.Val.Local(to, _), _), _)
            if canStoreTo.contains(to) =>
          true
        case inst @ nir.Inst.Let(
              _,
              nir.Op.Fieldstore(_, nir.Val.Local(to, _), _, value),
              _
            ) if canStoreTo.contains(to) =>
          canStoreValue(value)
        case nir.Inst.Let(
              _,
              nir.Op.Arrayload(_, nir.Val.Local(to, _), nir.Val.Int(idx)),
              _
            )
            if canStoreTo.contains(to)
              && inBounds(arrayLength.getOrElse(to, -1), idx) =>
          true
        case nir.Inst.Let(
              _,
              nir.Op.Arraystore(
                _,
                nir.Val.Local(to, _),
                nir.Val.Int(idx),
                value
              ),
              _
            )
            if canStoreTo.contains(to)
              && inBounds(arrayLength.getOrElse(to, -1), idx) =>
          canStoreValue(value)
        case nir.Inst.Let(_, nir.Op.Arraylength(nir.Val.Local(to, _)), _)
            if canStoreTo.contains(to) =>
          true
        case inst =>
          false
      }
    }

    isPureModule(clsName)
  }
}
