package scala.scalanative
package checker

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._
import scalanative.util.partitionBy
import scalanative.compat.CompatParColls.Converters._

final class Check(implicit linked: linker.Result) {
  val errors = mutable.UnrolledBuffer.empty[Check.Error]

  val labels = mutable.Map.empty[Local, Seq[Type]]
  val env = mutable.Map.empty[Local, Type]

  var name: Global = Global.None
  var retty: Type = Type.Unit
  var ctx: List[String] = Nil

  def in[T](entry: String)(f: => T): T = {
    try {
      ctx = entry :: ctx
      f
    } finally {
      ctx = ctx.tail
    }
  }

  def ok: Unit = ()

  def error(msg: String): Unit =
    errors += Check.Error(name, ctx, msg)

  def expect(expected: Type, got: Val): Unit =
    expect(expected, got.ty)

  def expect(expected: Type, got: Type): Unit =
    if (!Sub.is(got, expected)) {
      error(s"expected ${expected.show}, but got ${got.show}")
    }

  def run(infos: Seq[Info]): Unit =
    infos.foreach { info =>
      name = info.name
      checkInfo(info)
    }

  def checkInfo(info: Info): Unit = info match {
    case meth: Method =>
      checkMethod(meth)
    case _ =>
      ok
  }

  def checkMethod(meth: Method): Unit = {
    val Type.Function(_, methRetty) = meth.ty: @unchecked
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

  def enterInst(inst: Inst): Unit = {
    def enterParam(value: Val.Local) = {
      val Val.Local(local, ty) = value
      env(local) = ty
    }

    def enterUnwind(unwind: Next) = unwind match {
      case Next.Unwind(param, _) =>
        enterParam(param)
      case _ =>
        ok
    }

    inst match {
      case Inst.Let(n, op, unwind) =>
        env(n) = op.resty
        enterUnwind(unwind)
      case Inst.Label(name, params) =>
        labels(name) = params.map(_.ty)
        params.foreach(enterParam)
      case _: Inst.Ret | _: Inst.Jump | _: Inst.If | _: Inst.Switch =>
        ok
      case Inst.Throw(_, unwind) =>
        enterUnwind(unwind)
      case Inst.Unreachable(unwind) =>
        enterUnwind(unwind)
      case _: Inst.LinktimeCf => util.unreachable
    }
  }

  def checkInst(inst: Inst): Unit = inst match {
    case _: Inst.Label =>
      ok
    case Inst.Let(name, op, unwind) =>
      checkOp(op)
      in("unwind")(checkUnwind(unwind))
    case Inst.Ret(v) =>
      in("return value")(expect(retty, v))
    case Inst.Jump(next) =>
      in("jump")(checkNext(next))
    case Inst.If(value, thenp, elsep) =>
      in("condition")(expect(Type.Bool, value))
      in("then")(checkNext(thenp))
      in("else")(checkNext(elsep))
    case Inst.Switch(value, default, cases) =>
      in("default")(checkNext(default))
      cases.zipWithIndex.foreach {
        case (caseNext, idx) =>
          in("case #" + (idx + 1))(checkNext(caseNext))
      }
    case Inst.Throw(value, unwind) =>
      in("thrown value")(expect(Rt.Object, value))
      in("unwind")(checkUnwind(unwind))
    case Inst.Unreachable(unwind) =>
      in("unwind")(checkUnwind(unwind))
    case _: Inst.LinktimeCf => util.unreachable
  }

  def checkOp(op: Op): Unit = op match {
    case Op.Call(ty, ptr, args) =>
      expect(Type.Ptr, ptr)
      ty match {
        case ty: Type.Function =>
          checkCallArgs(ty, args)
        case _ =>
          error("call type must be a function type")
      }
    case Op.Load(ty, ptr, _) =>
      expect(Type.Ptr, ptr)
    case Op.Store(ty, ptr, value, _) =>
      expect(Type.Ptr, ptr)
      expect(ty, value)
    case Op.Elem(ty, ptr, indexes) =>
      expect(Type.Ptr, ptr)
      checkAggregateOp(Type.ArrayValue(ty, 0), indexes, None)
    case Op.Extract(aggr, indexes) =>
      aggr.ty match {
        case ty: Type.AggregateKind =>
          checkAggregateOp(ty, indexes.map(Val.Int(_)), None)
        case _ =>
          error(s"extract is only defined on aggregate types, not ${aggr.ty}")
      }
    case Op.Insert(aggr, value, indexes) =>
      aggr.ty match {
        case ty: Type.AggregateKind =>
          checkAggregateOp(ty, indexes.map(Val.Int(_)), Some(value.ty))
        case _ =>
          error(s"insert is only defined on aggregate types, not ${aggr.ty}")
      }
    case Op.Stackalloc(ty, n) =>
      ok
    case Op.Bin(bin, ty, l, r) =>
      checkBinOp(bin, ty, l, r)
    case Op.Comp(comp, ty, l, r) =>
      checkCompOp(comp, ty, l, r)
    case Op.Conv(conv, ty, value) =>
      checkConvOp(conv, ty, value)
    case Op.Fence(_) => ok
    case Op.Classalloc(name) =>
      linked.infos
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
    case Op.Fieldload(ty, obj, name) =>
      checkFieldOp(ty, obj, name, None)
    case Op.Fieldstore(ty, obj, name, value) =>
      checkFieldOp(ty, obj, name, Some(value))
    case Op.Field(obj, name) =>
      obj.ty match {
        case ScopeRef(scope) =>
          scope.implementors.foreach { cls =>
            if (cls.fields.exists(_.name == name)) ok
            else error(s"can't acces field '${name.show}' in ${cls.name.show}")
          }
        case ty =>
          error(s"can't access fields of a non-class type ${ty.show}")
      }
    case Op.Method(obj, sig) =>
      expect(Rt.Object, obj)
      sig match {
        case sig if sig.isMethod || sig.isCtor || sig.isGenerated =>
          ok
        case _ =>
          error(s"method must take a method signature, not ${sig.show}")
      }

      def checkCallable(cls: Class): Unit = {
        if (cls.allocated) {
          if (cls.resolve(sig).isEmpty) {
            error(s"can't call ${sig.show} on ${cls.name.show}")
          }
        }
      }

      obj.ty match {
        case Type.Null =>
          ok
        case ScopeRef(info) if sig.isVirtual =>
          info.implementors.foreach(checkCallable)
        case ClassRef(info) =>
          checkCallable(info)
        case ty =>
          error(s"can't resolve method on ${ty.show}")
      }
    case Op.Dynmethod(obj, sig) =>
      expect(Rt.Object, obj)
      sig match {
        case sig if sig.isProxy =>
          ok
        case _ =>
          error(s"dynmethod must take a proxy signature, not ${sig.show}")
      }
    case Op.Module(name) =>
      linked.infos
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
    case Op.As(ty, obj) =>
      ty match {
        case ty: Type.RefKind =>
          ok
        case ty =>
          error(s"can't cast to non-ref type ${ty.show}")
      }
      expect(Rt.Object, obj)
    case Op.Is(ty, obj) =>
      ty match {
        case ty: Type.RefKind =>
          ok
        case ty =>
          error(s"can't check instance of non-ref type ${ty.show}")
      }
      expect(Rt.Object, obj)
    case Op.Copy(value) =>
      ok
    case Op.SizeOf(ty) =>
      ty match {
        case _: Type.ValueKind                               => ok
        case Type.Ptr | Type.Nothing | Type.Null | Type.Unit => ok
        case ScopeRef(kind) =>
          kind match {
            case _: Class => ok
            case _: Trait => error(s"can't calculate size of a trait")
          }
        case _ => error(s"can't calucate size of ${ty.show}")
      }
      ok
    case Op.AlignmentOf(ty) =>
      ty match {
        case _: Type.ValueKind                               => ok
        case Type.Ptr | Type.Nothing | Type.Null | Type.Unit => ok
        case ScopeRef(kind) =>
          kind match {
            case _: Class => ok
            case _: Trait => error(s"can't calculate alignment of a trait")
          }
        case _ => error(s"can't calucate alignment of ${ty.show}")
      }
      ok
    case Op.Box(ty, value) =>
      Type.unbox
        .get(ty)
        .fold {
          error(s"uknown box type ${ty.show}")
        } { unboxedty => expect(unboxedty, value) }
    case Op.Unbox(ty, obj) =>
      expect(Rt.Object, obj)
    case Op.Var(ty) =>
      ok
    case Op.Varload(slot) =>
      slot.ty match {
        case Type.Var(ty) =>
          ok
        case _ =>
          error(s"can't varload from a non-var ${slot.show}")
      }
    case Op.Varstore(slot, value) =>
      slot.ty match {
        case Type.Var(ty) =>
          expect(ty, value)
        case _ =>
          error(s"can't varstore into non-var ${slot.show}")
      }
    case Op.Arrayalloc(ty, init) =>
      init match {
        case v if v.ty == Type.Int =>
          ok
        case Val.ArrayValue(elemty, elems) =>
          expect(ty, elemty)
        case _ =>
          error(s"can't initialize array with ${init.show}")
      }
    case Op.Arrayload(ty, arr, idx) =>
      val arrty = Type.Ref(Type.toArrayClass(ty))
      expect(arrty, arr)
      expect(Type.Int, idx)
    case Op.Arraystore(ty, arr, idx, value) =>
      val arrty = Type.Ref(Type.toArrayClass(ty))
      expect(arrty, arr)
      expect(Type.Int, idx)
      expect(ty, value)
    case Op.Arraylength(arr) =>
      expect(Rt.GenericArray, arr)
  }

  def checkAggregateOp(
      ty: Type.AggregateKind,
      indexes: Seq[Val],
      stores: Option[Type]
  ): Unit = {
    if (indexes.isEmpty) {
      error("index path must contain at least one index")
    }

    indexes.zipWithIndex.foreach {
      case (v, idx) =>
        v.ty match {
          case _: Type.I =>
            ok
          case _ =>
            in("index #" + (idx + 1)) {
              error("elem indexes must be integer values")
            }
        }
    }

    def loop(ty: Type, indexes: Seq[Val]): Unit =
      indexes match {
        case Seq() =>
          stores.foreach { v => expect(ty, v) }
        case value +: rest =>
          ty match {
            case Type.StructValue(tys) =>
              val idx = value match {
                case Val.Int(n) =>
                  n
                case Val.Long(n) =>
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
            case Type.ArrayValue(elemty, _) =>
              loop(elemty, rest)
            case _ =>
              error(s"can't index non-aggregate type ${ty.show}")
          }
      }

    loop(ty, indexes)
  }

  def checkCallArgs(ty: Type.Function, args: Seq[Val]): Unit = {
    def checkNoVarargs(argtys: Seq[Type]): Unit = {
      argtys.zipWithIndex.foreach {
        case (Type.Vararg, idx) =>
          in("arg #" + (idx + 1)) {
            error("vararg type can only appear as last argumen")
          }
        case _ =>
          ok
      }
    }

    def checkArgTypes(argtys: Seq[Type], args: Seq[Val]): Unit = {
      argtys.zip(args).zipWithIndex.foreach {
        case ((ty, value), idx) =>
          in("arg #" + (idx + 1))(expect(ty, value))
      }
    }

    ty match {
      case Type.Function(argtys :+ Type.Vararg, _) =>
        checkNoVarargs(argtys)
        if (args.size < argtys.size) {
          error(s"expected at least ${argtys.size} but got ${args.size}")
        }
        checkArgTypes(argtys, args.take(argtys.size))
      case Type.Function(argtys, _) =>
        checkNoVarargs(argtys)
        if (argtys.size != args.size) {
          error(s"expected ${argtys.size} arguments but got ${args.size}")
        }
        checkArgTypes(argtys, args)
    }
  }

  def checkFieldOp(
      ty: Type,
      obj: Val,
      name: Global,
      value: Option[Val]
  ): Unit = {

    obj.ty match {
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

  def checkBinOp(bin: Bin, ty: Type, l: Val, r: Val): Unit = {
    bin match {
      case Bin.Iadd => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Fadd => checkFloatOp(bin.show, ty, l, r)
      case Bin.Isub => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Fsub => checkFloatOp(bin.show, ty, l, r)
      case Bin.Imul => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Fmul => checkFloatOp(bin.show, ty, l, r)
      case Bin.Sdiv => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Udiv => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Fdiv => checkFloatOp(bin.show, ty, l, r)
      case Bin.Srem => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Urem => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Frem => checkFloatOp(bin.show, ty, l, r)
      case Bin.Shl  => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Lshr => checkIntegerOp(bin.show, ty, l, r)
      case Bin.Ashr => checkIntegerOp(bin.show, ty, l, r)
      case Bin.And  => checkIntegerOrBoolOp(bin.show, ty, l, r)
      case Bin.Or   => checkIntegerOrBoolOp(bin.show, ty, l, r)
      case Bin.Xor  => checkIntegerOrBoolOp(bin.show, ty, l, r)
    }
  }

  def checkCompOp(comp: Comp, ty: Type, l: Val, r: Val): Unit = comp match {
    case Comp.Ieq => checkIntegerOrBoolOrRefOp(comp.show, ty, l, r)
    case Comp.Ine => checkIntegerOrBoolOrRefOp(comp.show, ty, l, r)
    case Comp.Ugt => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Uge => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Ult => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Ule => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Sgt => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Sge => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Slt => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Sle => checkIntegerOp(comp.show, ty, l, r)
    case Comp.Feq => checkFloatOp(comp.show, ty, l, r)
    case Comp.Fne => checkFloatOp(comp.show, ty, l, r)
    case Comp.Fgt => checkFloatOp(comp.show, ty, l, r)
    case Comp.Fge => checkFloatOp(comp.show, ty, l, r)
    case Comp.Flt => checkFloatOp(comp.show, ty, l, r)
    case Comp.Fle => checkFloatOp(comp.show, ty, l, r)
  }

  def checkConvOp(conv: Conv, ty: Type, value: Val): Unit = conv match {
    case Conv.ZSizeCast | Conv.SSizeCast =>
      (value.ty, ty) match {
        case (lty: Type.FixedSizeI, Type.Size) => ok
        case (Type.Size, rty: Type.FixedSizeI) => ok
        case _ =>
          error(
            s"can't cast size from ${value.ty.show} to ${ty.show}"
          )
      }
    case Conv.Trunc =>
      (value.ty, ty) match {
        case (lty: Type.FixedSizeI, rty: Type.FixedSizeI)
            if lty.width > rty.width =>
          ok
        case _ =>
          error(s"can't trunc from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Zext =>
      (value.ty, ty) match {
        case (lty: Type.FixedSizeI, rty: Type.FixedSizeI)
            if lty.width < rty.width =>
          ok
        case _ =>
          error(s"can't zext from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Sext =>
      (value.ty, ty) match {
        case (lty: Type.FixedSizeI, rty: Type.FixedSizeI)
            if lty.width < rty.width =>
          ok
        case _ =>
          error(s"can't sext from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Fptrunc =>
      (value.ty, ty) match {
        case (Type.Double, Type.Float) =>
          ok
        case _ =>
          error(s"can't fptrunc from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Fpext =>
      (value.ty, ty) match {
        case (Type.Float, Type.Double) =>
          ok
        case _ =>
          error(s"can't fpext from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Fptoui =>
      (value.ty, ty) match {
        case (Type.Float | Type.Double, ity: Type.I) =>
          ok
        case _ =>
          error(s"can't fptoui from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Fptosi =>
      (value.ty, ty) match {
        case (Type.Float | Type.Double, ity: Type.I) if ity.signed =>
          ok
        case _ =>
          error(s"can't fptosi from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Uitofp =>
      (value.ty, ty) match {
        case (ity: Type.I, Type.Float | Type.Double) =>
          ok
        case _ =>
          error(s"can't uitofp from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Sitofp =>
      (value.ty, ty) match {
        case (ity: Type.I, Type.Float | Type.Double) if ity.signed =>
          ok
        case _ =>
          error(s"can't sitofp from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Ptrtoint =>
      (value.ty, ty) match {
        case (Type.Ptr | _: Type.RefKind, _: Type.I) =>
          ok
        case _ =>
          error(s"can't ptrtoint from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Inttoptr =>
      (value.ty, ty) match {
        case (_: Type.I, Type.Ptr | _: Type.RefKind) =>
          ok
        case _ =>
          error(s"can't inttoptr from ${value.ty.show} to ${ty.show}")
      }
    case Conv.Bitcast =>
      def fail =
        error(s"can't bitcast from ${value.ty.show} to ${ty.show}")
      (value.ty, ty) match {
        case (lty, rty) if lty == rty =>
          ok
        case (_: Type.I, Type.Ptr) | (Type.Ptr, _: Type.I) =>
          fail
        case (lty: Type.PrimitiveKind, rty: Type.PrimitiveKind)
            if lty.width == rty.width =>
          ok
        case (_: Type.RefKind, Type.Ptr) | (Type.Ptr, _: Type.RefKind) |
            (_: Type.RefKind, _: Type.RefKind) =>
          ok
        case _ =>
          fail
      }
  }

  def checkIntegerOp(op: String, ty: Type, l: Val, r: Val): Unit = {
    ty match {
      case ty: Type.I =>
        expect(ty, l)
        expect(ty, r)
      case _ =>
        error(s"$op is only defined on integer types, not ${ty.show}")
    }
  }

  def checkIntegerOrBoolOp(op: String, ty: Type, l: Val, r: Val): Unit = {
    ty match {
      case ty @ (_: Type.I | Type.Bool) =>
        expect(ty, l)
        expect(ty, r)
      case _ =>
        error(s"$op is only defined on integer types and bool, not ${ty.show}")
    }
  }

  def checkIntegerOrBoolOrRefOp(op: String, ty: Type, l: Val, r: Val): Unit = {
    ty match {
      case ty @ (_: Type.I | Type.Bool | Type.Null | Type.Ptr) =>
        expect(ty, l)
        expect(ty, r)
      case ty: Type.RefKind =>
        expect(Rt.Object, l)
        expect(Rt.Object, r)
      case _ =>
        error(
          s"$op is only defined on integer types, bool and reference types, not ${ty.show}"
        )
    }
  }

  def checkFloatOp(op: String, ty: Type, l: Val, r: Val): Unit = {
    ty match {
      case ty: Type.F =>
        expect(ty, l)
        expect(ty, r)
      case _ =>
        error(s"$op is only defined on floating types, not ${ty.show}")
    }
  }

  def checkUnwind(next: Next): Unit = next match {
    case Next.None =>
      ok
    case Next.Unwind(_, next) =>
      next match {
        case next: Next.Label =>
          checkNext(next)
        case _ =>
          error(s"unwind's destination has to be a label, not ${next.show}")
      }
    case _ =>
      error(s"unwind next can not be ${next.show}")
  }

  def checkNext(next: Next): Unit = next match {
    case Next.None =>
      error("can't use none next in non-unwind context")
    case _: Next.Unwind =>
      error("can't use unwind next in non-unwind context")
    case Next.Case(_, next) =>
      checkNext(next)
    case Next.Label(name, args) =>
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

object Check {
  final case class Error(name: Global, ctx: List[String], msg: String)

  def apply(linked: linker.Result): Seq[Error] =
    partitionBy(linked.infos.values.toSeq)(_.name).par
      .map {
        case (_, infos) =>
          val check = new Check()(linked)
          check.run(infos)
          check.errors
      }
      .seq
      .flatten
      .toSeq
}
