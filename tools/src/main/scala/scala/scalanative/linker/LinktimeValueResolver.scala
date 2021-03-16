package scala.scalanative.linker

import scala.annotation.tailrec
import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.util

trait LinktimeValueResolver { self: Reach =>
  import LinktimeValueResolver._

  val resolvedProperties = mutable.Map.empty[Global, Val]

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
      implicit pos: Position): Val =
    resolvedProperties.getOrElseUpdate(name, lookupLinktimeProperty(name))

  private def lookupLinktimeProperty(name: Global)(
      implicit pos: Position): Val = {
    fieldInfo(name).fold[Val] {
      addMissing(name, pos)
      Val.Null
    } { field =>
      require(field.isConst, "Linktime property was not const")
      require(field.ty.isInstanceOf[Type.StructValue],
              "Linktime property was not struct")

      val Type.StructValue(valType +: _) = field.ty
      val Val.StructValue(Seq(defaultValue, propertyName, envName)) =
        field.init

      def stringOrNone(value: Val): Option[String] = value match {
        case Val.String(str) => Some(str)
        case _               => None
      }

      // {property, env}Name can be Val.Null, in such case given property
      // should not be accessible through that channel
      def propertyValue =
        stringOrNone(propertyName)
          .flatMap(sys.props.get)
          .map(parseFromString(s"system property $propertyName"))

      def envValue =
        stringOrNone(envName)
          .flatMap(sys.env.get)
          .map(parseFromString(s"env variable $envName"))

      def parseFromString(context: => String)(str: String) =
        try {
          valType match {
            case Type.Bool  => Val.Bool(java.lang.Boolean.parseBoolean(str))
            case Type.Char  => Val.Char(str.head)
            case Type.Byte  => Val.Byte(java.lang.Byte.parseByte(str))
            case Type.Short => Val.Short(java.lang.Short.parseShort(str))
            case Type.Int   => Val.Int(java.lang.Integer.parseInt(str))
            case Type.Long  => Val.Long(java.lang.Long.parseLong(str))
            case Type.Float => Val.Float(java.lang.Float.parseFloat(str))
            case Type.Double =>
              Val.Double(java.lang.Double.parseDouble(str))
            case Type.Ref(Rt.StringName, _, _) => Val.String(str)
            case tpe =>
              util.unsupported(s"Unsupported type of linktime property: $tpe")
          }
        } catch {
          case exc: Throwable =>
            throw new LinkingException(
              s"Failed to parse `$str` from $context to value of $valType"
            ).initCause(exc)
        }

      propertyValue
        .orElse(envValue)
        .getOrElse(defaultValue)
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

        (condVal, resolvedValue) match {
          case ComperableValsTuple(ordering, condition, resolved) =>
            val comparsionFn = comparison match {
              case Comp.Ieq | Comp.Feq            => ordering.equiv _
              case Comp.Ine | Comp.Fne            => !ordering.equiv(_: Any, _: Any)
              case Comp.Sgt | Comp.Ugt | Comp.Fgt => ordering.gt _
              case Comp.Sge | Comp.Uge | Comp.Fge => ordering.gteq _
              case Comp.Slt | Comp.Ult | Comp.Flt => ordering.lt _
              case Comp.Sle | Comp.Ule | Comp.Fle => ordering.lteq _
            }
            comparsionFn(resolved, condition)

          case _ =>
            comparison match {
              case Comp.Ieq | Comp.Feq => resolvedValue == condVal
              case Comp.Ine | Comp.Fne => resolvedValue != condVal
              case _ =>
                util.unsupported(
                  s"Unsupported linktime comparison types: ${condVal.ty} and ${resolvedValue.ty}")
            }
        }
      case _ => util.unsupported(s"Unknown linktime condition: $cond")
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
    inst.copy(op = Op.Copy(resolveLinktimeProperty(propertyName)))
  }

}

object LinktimeValueResolver {
  object ReferencedPropertyOp {
    def unapply(op: Op): Option[Global] = op match {
      case Op.Call(_,
                   Val.Global(Linktime.PropertyResolveFunctionName, _),
                   Seq(Val.Global(propertyName, _))) =>
        Some(propertyName)
      case _ => None
    }
  }

  object ComparableVal {
    type AnyOrderingWithValues = (Ordering[Any], Any, Any)
    private def someAnyWithOrdering[T](v: T)(implicit ordering: Ordering[T]) =
      Some(v, ordering).asInstanceOf[Option[(Any, Ordering[Any])]]

    @tailrec
    def unapply(v: Val): Option[(Any, Ordering[Any])] = v match {
      case Val.Zero(_)       => unapply(v.canonicalize)
      case Val.True          => someAnyWithOrdering(true)
      case Val.False         => someAnyWithOrdering(false)
      case Val.Char(value)   => someAnyWithOrdering(value)
      case Val.Byte(value)   => someAnyWithOrdering(value)
      case Val.Short(value)  => someAnyWithOrdering(value)
      case Val.Int(value)    => someAnyWithOrdering(value)
      case Val.Long(value)   => someAnyWithOrdering(value)
      case Val.Float(value)  => someAnyWithOrdering(value)
      case Val.Double(value) => someAnyWithOrdering(value)
      case Val.String(value) => someAnyWithOrdering(value)
      case _                 => None
    }

    def unapply(vals: (Val, Val)): Option[AnyOrderingWithValues] =
      vals match {
        case (ComparableVal(l, lOrdering), ComparableVal(r, rOrdering))
            if lOrdering == rOrdering =>
          Some(lOrdering, l, r)
        case (ComparableVal(null, _), ComparableVal(r, rOrdering)) =>
          Some(rOrdering, null, r)
        case (ComparableVal(l, lOrdering), ComparableVal(null, _)) =>
          Some(lOrdering, l, null)
        case _ => None
      }

  }
  object ComperableValsTuple {
    def unapply(vals: (Val, Val)): Option[AnyOrderingWithValues] =
      vals match {
        case (ComparableVal(l, lOrdering), ComparableVal(r, rOrdering))
            if lOrdering == rOrdering =>
          Some(lOrdering, l, r)

        case (ComparableVal(l: Number, _), ComparableVal(r: Number, _)) =>
          Some(Ordering[Double], l.doubleValue(), r.doubleValue())
            .asInstanceOf[Option[AnyOrderingWithValues]]
        case (ComparableVal(Val.Null, _), ComparableVal(r, rOrdering)) =>
          Some(rOrdering, null, r)
        case (ComparableVal(l, lOrdering), ComparableVal(Val.Null, _)) =>
          Some(lOrdering, l, null)
        case _ => None
      }
  }
}
