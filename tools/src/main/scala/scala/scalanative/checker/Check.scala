package scala.scalanative
package checker

import scala.collection.mutable
import scalanative.linker.*
import scalanative.util.partitionBy
import scala.concurrent.*

private[scalanative] sealed abstract class NIRCheck(implicit
    analysis: ReachabilityAnalysis.Result
) {
  val errors = mutable.UnrolledBuffer.empty[Check.Error]
  var name: nir.Global = nir.Global.None
  var ctx: List[String] = Nil

  def ok: Unit = ()

  def error(msg: String): Unit =
    errors += Check.Error(name, ctx, msg)

  def expect(expected: nir.Type, got: nir.Val): Unit =
    expect(expected, got.ty)

  def expectOneOf(got: nir.Type)(candidates: nir.Type*): Unit = {
    if (candidates.exists(ty => Sub.is(got, ty))) () // ok
    else
      error(
        s"expected one of [${candidates.map(_.show).mkString(",")}], but got ${got.show}"
      )
  }

  def expect(expected: nir.Type, got: nir.Type): Unit =
    if (!Sub.is(got, expected)) {
      error(s"expected ${expected.show}, but got ${got.show}")
    }

  def run(
      infos: Seq[Info]
  )(implicit ec: ExecutionContext): Future[Seq[Check.Error]] = Future {
    infos.foreach { info =>
      name = info.name
      checkInfo(info)
    }
    errors.toSeq
  }

  def checkInfo(info: Info): Unit = info match {
    case meth: Method => checkMethod(meth)
    case _            => ok
  }

  def checkMethod(meth: Method): Unit

  protected final def checkFieldOp(op: nir.Op.Field): Unit = {
    val nir.Op.Field(obj, name) = op
    obj.ty match {
      case ScopeRef(scope) =>
        scope.implementors.foreach { cls =>
          if (cls.fields.exists(_.name == name)) ok
          else error(s"can't acces field '${name.show}' in ${cls.name.show}")
        }
      case ty => error(s"can't access fields of a non-class type ${ty.show}")
    }
  }

  protected final def checkMethodOp(op: nir.Op.Method): Unit = {
    val nir.Op.Method(obj, sig) = op
    expect(nir.Rt.Object, obj)
    sig match {
      case sig if sig.isMethod || sig.isCtor || sig.isGenerated => ok
      case _ => error(s"method must take a method signature, not ${sig.show}")
    }

    def checkCallable(cls: Class): Unit =
      if (cls.allocated && cls.resolve(sig).isEmpty) {
        error(s"can't call ${sig.show} on ${cls.name.show}")
      }

    obj.ty match {
      case nir.Type.Null                   => ok
      case ScopeRef(info) if sig.isVirtual =>
        info.implementors.foreach(checkCallable)
      case ClassRef(info) =>
        checkCallable(info)
      case ty => error(s"can't resolve method on ${ty.show}")
    }
  }
}

private[scalanative] final class Check(implicit
    analysis: ReachabilityAnalysis.Result
) extends NIRCheck {
  val labels = mutable.Map.empty[nir.Local, Seq[nir.Type]]
  val env = mutable.Map.empty[nir.Local, nir.Type]

  var retty: nir.Type = nir.Type.Unit

  def in[T](entry: String)(f: => T): T = {
    try {
      ctx = entry :: ctx
      f
    } finally {
      ctx = ctx.tail
    }
  }
  override def checkMethod(meth: Method): Unit = {
    val nir.Type.Function(_, methRetty) = meth.ty
    retty = methRetty

    val insts = meth.insts

    insts.zipWithIndex.foreach {
      case (inst, idx) =>
        in(s"inst #${idx + 1}") {
          enterInst(inst)
        }
    }

    insts.zipWithIndex.foreach {
      case (inst, idx) =>
        in(s"inst #${idx + 1}") {
          checkInst(inst)
        }
    }

    env.clear()
    labels.clear()
  }

  def enterInst(inst: nir.Inst): Unit = {
    def enterParam(value: nir.Val.Local) = {
      val nir.Val.Local(local, ty) = value
      env(local) = ty
    }

    def enterUnwind(unwind: nir.Next) = unwind match {
      case nir.Next.Unwind(param, _) =>
        enterParam(param)
      case _ =>
        ok
    }

    inst match {
      case nir.Inst.Let(n, op, unwind) =>
        env(n) = op.resty
        enterUnwind(unwind)
      case nir.Inst.Label(name, params) =>
        labels(name) = params.map(_.ty)
        params.foreach(enterParam)
      case _: nir.Inst.Ret | _: nir.Inst.Jump | _: nir.Inst.If |
          _: nir.Inst.Switch =>
        ok
      case nir.Inst.Throw(_, unwind) =>
        enterUnwind(unwind)
      case nir.Inst.Unreachable(unwind) =>
        enterUnwind(unwind)
      case _: nir.Inst.LinktimeCf => util.unreachable
    }
  }

  def checkInst(inst: nir.Inst): Unit = inst match {
    case _: nir.Inst.Label =>
      ok
    case nir.Inst.Let(_, op, unwind) =>
      checkOp(op)
      in("unwind")(checkUnwind(unwind))
    case nir.Inst.Ret(v) =>
      in("return value")(expect(retty, v))
    case nir.Inst.Jump(next) =>
      in("jump")(checkNext(next))
    case nir.Inst.If(value, thenp, elsep) =>
      in("condition")(expect(nir.Type.Bool, value))
      in("then")(checkNext(thenp))
      in("else")(checkNext(elsep))
    case nir.Inst.Switch(value, default, cases) =>
      in("default")(checkNext(default))
      cases.zipWithIndex.foreach {
        case (caseNext, idx) =>
          in("case #" + (idx + 1))(checkNext(caseNext))
      }
    case nir.Inst.Throw(value, unwind) =>
      in("thrown value")(expect(nir.Rt.Object, value))
      in("unwind")(checkUnwind(unwind))
    case nir.Inst.Unreachable(unwind) =>
      in("unwind")(checkUnwind(unwind))
    case _: nir.Inst.LinktimeCf => util.unreachable
  }

  def checkOp(op: nir.Op): Unit = op match {
    case nir.Op.Call(ty, ptr, args) =>
      expect(nir.Type.Ptr, ptr)
      checkCallArgs(ty, args)
    case nir.Op.Load(ty, ptr, _) =>
      expect(nir.Type.Ptr, ptr)
    case nir.Op.Store(ty, ptr, value, _) =>
      expect(nir.Type.Ptr, ptr)
      expect(ty, value)
    case nir.Op.Elem(ty, ptr, indexes) =>
      expect(nir.Type.Ptr, ptr)
      checkAggregateOp(nir.Type.ArrayValue(ty, 0), indexes, None)
    case nir.Op.Extract(aggr, indexes) =>
      aggr.ty match {
        case ty: nir.Type.AggregateKind =>
          checkAggregateOp(ty, indexes.map(nir.Val.Int(_)), None)
        case _ =>
          error(s"extract is only defined on aggregate types, not ${aggr.ty}")
      }
    case nir.Op.Insert(aggr, value, indexes) =>
      aggr.ty match {
        case ty: nir.Type.AggregateKind =>
          checkAggregateOp(ty, indexes.map(nir.Val.Int(_)), Some(value.ty))
        case _ =>
          error(s"insert is only defined on aggregate types, not ${aggr.ty}")
      }
    case nir.Op.Stackalloc(ty, n) =>
      ok
    case nir.Op.Bin(bin, ty, l, r) =>
      checkBinOp(bin, ty, l, r)
    case nir.Op.Comp(comp, ty, l, r) =>
      checkCompOp(comp, ty, l, r)
    case nir.Op.Conv(conv, ty, value) =>
      checkConvOp(conv, ty, value)
    case nir.Op.Fence(_)               => ok
    case nir.Op.Classalloc(name, zone) =>
      analysis.infos
        .get(name)
        .fold {
          error(s"no info for ${name.show}")
        } {
          case info: Class =>
            if (info.isModule) {
              error(
                s"can't instantiate module class ${info.name.show} with classalloc"
              )
            } else if (info.attrs.isAbstract) {
              error(s"can't instantiate abstract class ${info.name.show}")
            } else {
              ok
            }
          case _ =>
            error(s"can't instantiate ${name.show} with clasalloc")
        }
      zone.foreach(checkZone)

    case nir.Op.Fieldload(ty, obj, name) =>
      checkFieldOp(ty, obj, name, None)
    case nir.Op.Fieldstore(ty, obj, name, value) =>
      checkFieldOp(ty, obj, name, Some(value))
    case op: nir.Op.Field           => checkFieldOp(op)
    case op: nir.Op.Method          => checkMethodOp(op)
    case nir.Op.Dynmethod(obj, sig) =>
      expect(nir.Rt.Object, obj)
      sig match {
        case sig if sig.isProxy =>
          ok
        case _ =>
          error(s"dynmethod must take a proxy signature, not ${sig.show}")
      }
    case nir.Op.Module(name) =>
      analysis.infos
        .get(name)
        .fold {
          error(s"no info for $name")
        } {
          case info: Class =>
            if (!info.isModule) {
              error(
                s"can't instantiate non-module class ${info.name.show} as module"
              )
            } else if (info.attrs.isAbstract) {
              error(s"can't instantiate abstract class ${info.name.show}")
            } else {
              ok
            }
          case _ =>
            error(s"can't instantiate ${name.show} as a module class")
        }
    case nir.Op.As(ty, obj) =>
      ty match {
        case ty: nir.Type.RefKind =>
          ok
        case ty =>
          error(s"can't cast to non-ref type ${ty.show}")
      }
      expect(nir.Rt.Object, obj)
    case nir.Op.Is(ty, obj) =>
      ty match {
        case ty: nir.Type.RefKind =>
          ok
        case ty =>
          error(s"can't check instance of non-ref type ${ty.show}")
      }
      expect(nir.Rt.Object, obj)
    case nir.Op.Copy(value) =>
      ok
    case nir.Op.SizeOf(ty) =>
      ty match {
        case _: nir.Type.ValueKind =>
          ok
        case nir.Type.Ptr | nir.Type.Nothing | nir.Type.Null | nir.Type.Unit =>
          ok
        case ScopeRef(kind) =>
          kind match {
            case _: Class => ok
            case _: Trait => error(s"can't calculate size of a trait")
          }
        case _ => error(s"can't calucate size of ${ty.show}")
      }
      ok
    case nir.Op.AlignmentOf(ty) =>
      ty match {
        case _: nir.Type.ValueKind =>
          ok
        case nir.Type.Ptr | nir.Type.Nothing | nir.Type.Null | nir.Type.Unit =>
          ok
        case ScopeRef(kind) =>
          kind match {
            case _: Class => ok
            case _: Trait => error(s"can't calculate alignment of a trait")
          }
        case _ => error(s"can't calucate alignment of ${ty.show}")
      }
      ok
    case nir.Op.Box(ty, value) =>
      nir.Type.unbox
        .get(ty)
        .fold {
          error(s"uknown box type ${ty.show}")
        } { unboxedty => expect(unboxedty, value) }
    case nir.Op.Unbox(ty, obj) =>
      expect(nir.Rt.Object, obj)
    case nir.Op.Var(ty) =>
      ok
    case nir.Op.Varload(slot) =>
      slot.ty match {
        case nir.Type.Var(ty) =>
          ok
        case _ =>
          error(s"can't varload from a non-var ${slot.show}")
      }
    case nir.Op.Varstore(slot, value) =>
      slot.ty match {
        case nir.Type.Var(ty) =>
          expect(ty, value)
        case _ =>
          error(s"can't varstore into non-var ${slot.show}")
      }
    case nir.Op.Arrayalloc(ty, init, zone) =>
      init match {
        case v if v.ty == nir.Type.Int =>
          ok
        case nir.Val.ArrayValue(elemty, elems) =>
          expect(ty, elemty)
        case _ =>
          error(s"can't initialize array with ${init.show}")
      }
      zone.foreach(checkZone)
    case nir.Op.Arrayload(ty, arr, idx) =>
      val arrty = nir.Type.Ref(nir.Type.toArrayClass(ty))
      if (ty == nir.Type.Byte)
        expectOneOf(arr.ty)(arrty, nir.Rt.BlobArray)
      else
        expect(arrty, arr)
      expect(nir.Type.Int, idx)
    case nir.Op.Arraystore(ty, arr, idx, value) =>
      val arrty = nir.Type.Ref(nir.Type.toArrayClass(ty))
      if (ty == nir.Type.Byte)
        expectOneOf(arr.ty)(arrty, nir.Rt.BlobArray)
      else
        expect(arrty, arr)
      expect(nir.Type.Int, idx)
      expect(ty, value)
    case nir.Op.Arraylength(arr) =>
      expect(nir.Rt.GenericArray, arr)
  }

  def checkZone(zone: nir.Val): Unit = zone match {
    case nir.Val.Null | nir.Val.Unit =>
      error(s"zone defined with null or unit")
    case v =>
      v.ty match {
        case nir.Type.Ptr | _: nir.Type.RefKind => ()
        case _ => error(s"zone defind with non reference type")
      }
  }

  def checkAggregateOp(
      ty: nir.Type.AggregateKind,
      indexes: Seq[nir.Val],
      stores: Option[nir.Type]
  ): Unit = {
    if (indexes.isEmpty) {
      error("index path must contain at least one index")
    }

    indexes.zipWithIndex.foreach {
      case (v, idx) =>
        v.ty match {
          case _: nir.Type.I =>
            ok
          case _ =>
            in("index #" + (idx + 1)) {
              error("elem indexes must be integer values")
            }
        }
    }

    def loop(ty: nir.Type, indexes: Seq[nir.Val]): Unit =
      indexes match {
        case Seq() =>
          stores.foreach { v => expect(ty, v) }
        case value +: rest =>
          ty match {
            case nir.Type.StructValue(tys) =>
              val idx = value match {
                case nir.Val.Int(n) =>
                  n
                case nir.Val.Long(n) =>
                  n.toInt
                case value =>
                  error(s"can't index into struct with ${value.show}")
                  return
              }
              if (idx >= 0 && idx <= tys.length) {
                loop(tys(idx), rest)
              } else {
                error(s"can't index $idx into ${ty.show}")
              }
            case nir.Type.ArrayValue(elemty, _) =>
              loop(elemty, rest)
            case _ =>
              error(s"can't index non-aggregate type ${ty.show}")
          }
      }

    loop(ty, indexes)
  }

  def checkCallArgs(ty: nir.Type.Function, args: Seq[nir.Val]): Unit = {
    def checkNoVarargs(argtys: Seq[nir.Type]): Unit = {
      argtys.zipWithIndex.foreach {
        case (nir.Type.Vararg, idx) =>
          in("arg #" + (idx + 1)) {
            error("vararg type can only appear as last argumen")
          }
        case _ =>
          ok
      }
    }

    def checkArgTypes(argtys: Seq[nir.Type], args: Seq[nir.Val]): Unit = {
      argtys.zip(args).zipWithIndex.foreach {
        case ((ty, value), idx) =>
          in("arg #" + (idx + 1))(expect(ty, value))
      }
    }

    ty match {
      case nir.Type.Function(argtys :+ nir.Type.Vararg, _) =>
        checkNoVarargs(argtys)
        if (args.size < argtys.size) {
          error(s"expected at least ${argtys.size} but got ${args.size}")
        }
        checkArgTypes(argtys, args.take(argtys.size))
      case nir.Type.Function(argtys, _) =>
        checkNoVarargs(argtys)
        if (argtys.size != args.size) {
          error(s"expected ${argtys.size} arguments but got ${args.size}")
        }
        checkArgTypes(argtys, args)
    }
  }

  def checkFieldOp(
      ty: nir.Type,
      obj: nir.Val,
      name: nir.Global,
      value: Option[nir.Val]
  ): Unit = {

    obj.ty match {
      case nir.Type.Null   => ok
      case ScopeRef(scope) =>
        scope.implementors.foreach { cls =>
          val field = cls.fields.collectFirst {
            case fld: Field if fld.name == name =>
              in("field declared type") {
                expect(ty, fld.ty)
              }
              value.foreach { v =>
                in("stored value") {
                  expect(fld.ty, v)
                }
              }
          }
          if (field.isEmpty) {
            error(
              s"class ${scope.name.show} does not define field ${name.show}"
            )
          }
        }
      case ty =>
        error(s"can't access fields of a non-class type ${ty.show}")
    }
  }

  def checkBinOp(bin: nir.Bin, ty: nir.Type, l: nir.Val, r: nir.Val): Unit = {
    bin match {
      case nir.Bin.Iadd => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Fadd => checkFloatOp(bin.show, ty, l, r)
      case nir.Bin.Isub => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Fsub => checkFloatOp(bin.show, ty, l, r)
      case nir.Bin.Imul => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Fmul => checkFloatOp(bin.show, ty, l, r)
      case nir.Bin.Sdiv => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Udiv => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Fdiv => checkFloatOp(bin.show, ty, l, r)
      case nir.Bin.Srem => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Urem => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Frem => checkFloatOp(bin.show, ty, l, r)
      case nir.Bin.Shl  => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Lshr => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.Ashr => checkIntegerOp(bin.show, ty, l, r)
      case nir.Bin.And  => checkIntegerOrBoolOp(bin.show, ty, l, r)
      case nir.Bin.Or   => checkIntegerOrBoolOp(bin.show, ty, l, r)
      case nir.Bin.Xor  => checkIntegerOrBoolOp(bin.show, ty, l, r)
    }
  }

  def checkCompOp(comp: nir.Comp, ty: nir.Type, l: nir.Val, r: nir.Val): Unit =
    comp match {
      case nir.Comp.Ieq => checkIntegerOrBoolOrRefOp(comp.show, ty, l, r)
      case nir.Comp.Ine => checkIntegerOrBoolOrRefOp(comp.show, ty, l, r)
      case nir.Comp.Ugt => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Uge => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Ult => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Ule => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Sgt => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Sge => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Slt => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Sle => checkIntegerOp(comp.show, ty, l, r)
      case nir.Comp.Feq => checkFloatOp(comp.show, ty, l, r)
      case nir.Comp.Fne => checkFloatOp(comp.show, ty, l, r)
      case nir.Comp.Fgt => checkFloatOp(comp.show, ty, l, r)
      case nir.Comp.Fge => checkFloatOp(comp.show, ty, l, r)
      case nir.Comp.Flt => checkFloatOp(comp.show, ty, l, r)
      case nir.Comp.Fle => checkFloatOp(comp.show, ty, l, r)
    }

  def checkConvOp(conv: nir.Conv, ty: nir.Type, value: nir.Val): Unit =
    conv match {
      case nir.Conv.ZSizeCast | nir.Conv.SSizeCast =>
        (value.ty, ty) match {
          case (lty: nir.Type.FixedSizeI, nir.Type.Size) => ok
          case (nir.Type.Size, rty: nir.Type.FixedSizeI) => ok
          case _                                         =>
            error(
              s"can't cast size from ${value.ty.show} to ${ty.show}"
            )
        }
      case nir.Conv.Trunc =>
        (value.ty, ty) match {
          case (lty: nir.Type.FixedSizeI, rty: nir.Type.FixedSizeI)
              if lty.width > rty.width =>
            ok
          case _ =>
            error(s"can't trunc from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Zext =>
        (value.ty, ty) match {
          case (lty: nir.Type.FixedSizeI, rty: nir.Type.FixedSizeI)
              if lty.width < rty.width =>
            ok
          case _ =>
            error(s"can't zext from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Sext =>
        (value.ty, ty) match {
          case (lty: nir.Type.FixedSizeI, rty: nir.Type.FixedSizeI)
              if lty.width < rty.width =>
            ok
          case _ =>
            error(s"can't sext from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Fptrunc =>
        (value.ty, ty) match {
          case (nir.Type.Double, nir.Type.Float) =>
            ok
          case _ =>
            error(s"can't fptrunc from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Fpext =>
        (value.ty, ty) match {
          case (nir.Type.Float, nir.Type.Double) =>
            ok
          case _ =>
            error(s"can't fpext from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Fptoui =>
        (value.ty, ty) match {
          case (nir.Type.Float | nir.Type.Double, ity: nir.Type.I) =>
            ok
          case _ =>
            error(s"can't fptoui from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Fptosi =>
        (value.ty, ty) match {
          case (nir.Type.Float | nir.Type.Double, ity: nir.Type.I)
              if ity.signed =>
            ok
          case _ =>
            error(s"can't fptosi from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Uitofp =>
        (value.ty, ty) match {
          case (ity: nir.Type.I, nir.Type.Float | nir.Type.Double) =>
            ok
          case _ =>
            error(s"can't uitofp from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Sitofp =>
        (value.ty, ty) match {
          case (ity: nir.Type.I, nir.Type.Float | nir.Type.Double)
              if ity.signed =>
            ok
          case _ =>
            error(s"can't sitofp from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Ptrtoint =>
        (value.ty, ty) match {
          case (nir.Type.Ptr | _: nir.Type.RefKind, _: nir.Type.I) =>
            ok
          case _ =>
            error(s"can't ptrtoint from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Inttoptr =>
        (value.ty, ty) match {
          case (_: nir.Type.I, nir.Type.Ptr | _: nir.Type.RefKind) =>
            ok
          case _ =>
            error(s"can't inttoptr from ${value.ty.show} to ${ty.show}")
        }
      case nir.Conv.Bitcast =>
        def fail =
          error(s"can't bitcast from ${value.ty.show} to ${ty.show}")
        (value.ty, ty) match {
          case (lty, rty) if lty == rty =>
            ok
          case (_: nir.Type.I, nir.Type.Ptr) | (nir.Type.Ptr, _: nir.Type.I) =>
            fail
          case (lty: nir.Type.PrimitiveKind, rty: nir.Type.PrimitiveKind)
              if lty.width == rty.width =>
            ok
          case (_: nir.Type.RefKind, nir.Type.Ptr) |
              (nir.Type.Ptr, _: nir.Type.RefKind) |
              (_: nir.Type.RefKind, _: nir.Type.RefKind) =>
            ok
          case _ =>
            fail
        }
    }

  def checkIntegerOp(op: String, ty: nir.Type, l: nir.Val, r: nir.Val): Unit = {
    ty match {
      case ty: nir.Type.I =>
        expect(ty, l)
        expect(ty, r)
      case _ =>
        error(s"$op is only defined on integer types, not ${ty.show}")
    }
  }

  def checkIntegerOrBoolOp(
      op: String,
      ty: nir.Type,
      l: nir.Val,
      r: nir.Val
  ): Unit = {
    ty match {
      case ty @ (_: nir.Type.I | nir.Type.Bool) =>
        expect(ty, l)
        expect(ty, r)
      case _ =>
        error(s"$op is only defined on integer types and bool, not ${ty.show}")
    }
  }

  def checkIntegerOrBoolOrRefOp(
      op: String,
      ty: nir.Type,
      l: nir.Val,
      r: nir.Val
  ): Unit = {
    ty match {
      case ty @ (_: nir.Type.I | nir.Type.Bool | nir.Type.Null |
          nir.Type.Ptr) =>
        expect(ty, l)
        expect(ty, r)
      case ty: nir.Type.RefKind =>
        expect(nir.Rt.Object, l)
        expect(nir.Rt.Object, r)
      case _ =>
        error(
          s"$op is only defined on integer types, bool and reference types, not ${ty.show}"
        )
    }
  }

  def checkFloatOp(op: String, ty: nir.Type, l: nir.Val, r: nir.Val): Unit = {
    ty match {
      case ty: nir.Type.F =>
        expect(ty, l)
        expect(ty, r)
      case _ =>
        error(s"$op is only defined on floating types, not ${ty.show}")
    }
  }

  def checkUnwind(next: nir.Next): Unit = next match {
    case nir.Next.None =>
      ok
    case nir.Next.Unwind(_, next) =>
      next match {
        case next: nir.Next.Label =>
          checkNext(next)
        case _ =>
          error(s"unwind's destination has to be a label, not ${next.show}")
      }
    case _ =>
      error(s"unwind next can not be ${next.show}")
  }

  def checkNext(next: nir.Next): Unit = next match {
    case nir.Next.None =>
      error("can't use none next in non-unwind context")
    case _: nir.Next.Unwind =>
      error("can't use unwind next in non-unwind context")
    case nir.Next.Case(_, next) =>
      checkNext(next)
    case nir.Next.Label(name, args) =>
      labels
        .get(name)
        .fold {
          error(s"can't jump to unknown destination ${name.show}")
        } { tys =>
          if (tys.length != args.length) {
            error(
              s"expected ${tys.length} label arguments but got ${args.length}"
            )
          } else {
            tys.zip(args).zipWithIndex.foreach {
              case ((expected, v), idx) =>
                in("arg #" + (idx + 1)) {
                  expect(expected, v)
                }
            }
          }
        }
  }
}

private[scalanative] final class QuickCheck(implicit
    analysis: ReachabilityAnalysis.Result
) extends NIRCheck {
  override def checkMethod(meth: Method): Unit = {
    meth.insts.foreach(checkInst)
  }

  def checkInst(inst: nir.Inst): Unit = inst match {
    case nir.Inst.Let(_, op, _) =>
      checkOp(op)
    case _ =>
      ok
  }

  def checkOp(op: nir.Op): Unit = op match {
    case op: nir.Op.Field =>
      checkFieldOp(op)
    case op: nir.Op.Method =>
      checkMethodOp(op)
    case _ =>
      ok
  }

}

private[scalanative] object Check {
  final case class Error(name: nir.Global, ctx: List[String], msg: String)

  private def run(
      checkImpl: ReachabilityAnalysis.Result => NIRCheck
  )(
      analysis: ReachabilityAnalysis.Result
  )(implicit ec: ExecutionContext): Future[Seq[Error]] = {
    val partitions = partitionBy(analysis.infos.values.toSeq)(_.name)
      .map {
        case (_, infos) =>
          checkImpl(analysis).run(infos)
      }
    Future.reduceLeft(partitions)(_ ++ _)
  }

  def apply(analysis: ReachabilityAnalysis.Result)(implicit
      ec: ExecutionContext
  ): Future[Seq[Error]] =
    run(new Check()(_))(analysis)
  def quick(analysis: ReachabilityAnalysis.Result)(implicit
      ec: ExecutionContext
  ): Future[Seq[Error]] =
    run(new QuickCheck()(_))(analysis)
}
