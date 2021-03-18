package scala.scalanative.linker

import scala.collection.mutable
import scala.scalanative.build.BuildException
import scala.scalanative.nir._

trait LinktimeValueResolver { self: Reach =>
  import LinktimeValueResolver._

  private val resolvedValues = mutable.Map.empty[Global, LinktimeValue]
  // For compat with 2.13 where mapValues is deprecated
  def resolvedNirValues: mutable.Map[Global, Val] = resolvedValues.map {
    case (k, v) => k -> v.nirValue
  }

  protected def resolveLinktimeDefine(defn: Defn.Define): Defn.Define = {
    implicit def position: Position = defn.pos

    if (!defn.insts.exists(shouldResolveInst)) defn
    else {
      val resolvedInsts = ControlFlow.removeDeadBlocks {
        defn.insts.map {
          case inst: Inst.LinktimeIf => resolveLinktimeIf(inst)
          case inst @ Inst.Let(_, ReferencedPropertyOp(propertyName), _) =>
            resolveReferencedProperty(inst, propertyName)
          case inst => inst
        }
      }

      defn.copy(insts = resolvedInsts)
    }
  }

  protected def shouldResolveInst(inst: Inst): Boolean = inst match {
    case _: Inst.LinktimeIf                      => true
    case Inst.Let(_, ReferencedPropertyOp(_), _) => true
    case _                                       => false
  }

  private def resolveLinktimeProperty(name: Global)(
      implicit pos: Position): LinktimeValue =
    resolvedValues.getOrElseUpdate(name, lookupLinktimeProperty(name))

  private def lookupLinktimeProperty(name: Global)(
      implicit pos: Position): LinktimeValue = {
    fieldInfo(name).fold[LinktimeValue] {
      addMissing(name, pos)
      ComparableVal(null, Val.Null).asAny
    } { field =>
      require(field.isConst, "Linktime property was not const")
      require(field.ty.isInstanceOf[Type.StructValue],
              "Linktime property was not struct")

      val Type.StructValue(defaultTy +: _) = field.ty
      val Val.StructValue(Seq(default, Val.String(propertyName))) =
        field.init

      def defaultValue = ComparableVal.fromNir(default)

      def propertyValue =
        config.compilerConfig.linktimeProperties
          .get(propertyName)
          .map(ComparableVal.fromAny(_, defaultTy))

      propertyValue.getOrElse(defaultValue).asAny
    }
  }

  private def resolveCondition(cond: LinktimeCondition)(
      implicit pos: Position): Boolean = {
    import LinktimeCondition._

    cond match {
      case ComplexCondition(Bin.And, left, right) =>
        resolveCondition(left) && resolveCondition(right)

      case ComplexCondition(Bin.Or, left, right) =>
        resolveCondition(left) || resolveCondition(right)

      case SimpleCondition(name, comparison, condVal) =>
        val resolvedValue = resolveLinktimeProperty(name)

        (ComparableVal.fromNir(condVal), resolvedValue) match {
          case t @ ComparableTuple(ordering, condition, resolved) =>
            val comparsionFn = comparison match {
              case Comp.Ieq | Comp.Feq            => ordering.equiv _
              case Comp.Ine | Comp.Fne            => !ordering.equiv(_: Any, _: Any)
              case Comp.Sgt | Comp.Ugt | Comp.Fgt => ordering.gt _
              case Comp.Sge | Comp.Uge | Comp.Fge => ordering.gteq _
              case Comp.Slt | Comp.Ult | Comp.Flt => ordering.lt _
              case Comp.Sle | Comp.Ule | Comp.Fle => ordering.lteq _
            }

            comparsionFn(resolved.value, condition.value)

          // In case if we cannot get common Ordering that can be used, eg.: comparison with Null
          case (ComparableVal(condition, _), ComparableVal(resolved, _)) =>
            comparison match {
              case Comp.Ieq | Comp.Feq => resolved == condition
              case Comp.Ine | Comp.Fne => resolved != condition
              case _ =>
                throw new BuildException(
                  s"Unsupported link-time comparison types: ${condVal.ty} and ${resolvedValue.nirValue.ty}")
            }
        }
      case _ => throw new BuildException(s"Unknown link-time condition: $cond")
    }
  }

  private def resolveLinktimeIf(inst: Inst.LinktimeIf)(
      implicit pos: Position): Inst = {
    val Inst.LinktimeIf(cond, thenp, elsep) = inst

    val matchesCondition = resolveCondition(cond)
    if (matchesCondition) Inst.Jump(thenp)
    else Inst.Jump(elsep)
  }

  private def resolveReferencedProperty(inst: Inst.Let, propertyName: Global)(
      implicit pos: Position): Inst = {
    reachGlobal(propertyName)
    inst.copy(op = Op.Copy(resolveLinktimeProperty(propertyName).nirValue))
  }

}

private[linker] object LinktimeValueResolver {
  type LinktimeValue = ComparableVal[Any]

  object ReferencedPropertyOp {
    def unapply(op: Op): Option[Global] = op match {
      case Op.Call(_,
                   Val.Global(Linktime.PropertyResolveFunctionName, _),
                   Seq(Val.Global(propertyName, _))) =>
        Some(propertyName)
      case _ => None
    }
  }

  case class ComparableVal[T: Ordering](value: T, nirValue: Val)(
      implicit val ordering: Ordering[T]) {
    def asAny: ComparableVal[Any] = this.asInstanceOf[ComparableVal[Any]]
  }

  object ComparableVal {
    def fromAny(value: Any, expectedNirType: Type): ComparableVal[_] = {
      def fromNumber[T: Ordering](number: Number, toConcrete: Number => T)(
          toNir: T => Val) = {
        val v = toConcrete(number)
        ComparableVal(v, toNir(v))
      }

      def parseString(str: String) = {
        val parsed = expectedNirType match {
          case Type.Bool                 => java.lang.Boolean.parseBoolean(str)
          case Type.Char if str.nonEmpty => str.head
          case _: Type.I                 => java.lang.Long.parseLong(str)
          case _: Type.F                 => java.lang.Double.parseDouble(str)
          case _ =>
            throw new BuildException(
              s"Cannot parse `$str` to expected NIR type $expectedNirType")
        }
        fromAny(parsed, expectedNirType)
      }

      (value, expectedNirType) match {
        case (v: Boolean, Type.Bool) =>
          ComparableVal(v, if (v) Val.True else Val.False)
        case (v: Char, Type.Char)   => ComparableVal(v, Val.Char(v))
        case (v: Number, Type.Byte) => fromNumber(v, _.byteValue)(Val.Byte)
        case (v: Number, Type.Char) =>
          fromNumber(v, _.shortValue.toChar)(Val.Char)
        case (v: Number, Type.Short) => fromNumber(v, _.shortValue)(Val.Short)
        case (v: Number, Type.Int)   => fromNumber(v, _.intValue())(Val.Int)
        case (v: Number, Type.Long)  => fromNumber(v, _.longValue())(Val.Long)
        case (v: Number, Type.Float) => fromNumber(v, _.floatValue())(Val.Float)
        case (v: Number, Type.Double) =>
          fromNumber(v, _.doubleValue())(Val.Double)
        case (v: String, Type.Ref(Rt.StringName, _, _)) =>
          ComparableVal(v, Val.String(v))
        case (v: String, _) => parseString(v)
        case (got, expected) =>
          throw new BuildException(
            s"Unsupported type of linktime value, got $got, expected: $expected")
      }
    }

    def fromNir(v: Val): LinktimeValue = {
      v match {
        case Val.String(value) => ComparableVal(value, v)
        case Val.True          => ComparableVal(true, v)
        case Val.False         => ComparableVal(false, v)
        case Val.Byte(value)   => ComparableVal(value, v)
        case Val.Char(value)   => ComparableVal(value, v)
        case Val.Short(value)  => ComparableVal(value, v)
        case Val.Int(value)    => ComparableVal(value, v)
        case Val.Long(value)   => ComparableVal(value, v)
        case Val.Float(value)  => ComparableVal(value, v)
        case Val.Double(value) => ComparableVal(value, v)
        case Val.Null          => ComparableVal(null, v)
        case other =>
          throw new BuildException(
            s"Unsupported NIR value for link-time resolving: $other")
      }
    }.asAny
  }

  object ComparableTuple {
    type ComparableTupleType =
      (Ordering[Any], ComparableVal[Any], ComparableVal[Any])
    def unapply(vals: (ComparableVal[_], ComparableVal[_])) = {
      vals match {
        case (l: ComparableVal[_], r: ComparableVal[_])
            if l.ordering == r.ordering =>
          Some((l.ordering, l, r))

        case (ComparableVal(lValue: Number, lNir),
              ComparableVal(rValue: Number, rNir)) =>
          Some {
            (implicitly[Ordering[Double]],
             ComparableVal(lValue.doubleValue(), lNir).asAny,
             ComparableVal(rValue.doubleValue(), rNir))
          }

        case _ => None
      }
    }.map(_.asInstanceOf[ComparableTupleType])
  }

}
