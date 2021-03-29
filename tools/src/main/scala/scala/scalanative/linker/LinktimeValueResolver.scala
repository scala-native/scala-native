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
      require(field.ty == Rt.String, "Linktime property was a string")

      val Val.String(propertyName) = field.init

      config.compilerConfig.linktimeProperties
        .get(propertyName)
        .fold(throw new BuildException(
          s"Link-time property $propertyName not defined in config")) {
          ComparableVal.fromAny(_).asAny
        }
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
          case ComparableTuple(ordering, condition, resolved) =>
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
                  s"Unsupported link-time comparison ${comparison} between types ${condVal.ty} and ${resolvedValue.nirValue.ty}")
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
    def fromAny(value: Any): ComparableVal[_] = {
      value match {
        case v: Boolean => ComparableVal(v, if (v) Val.True else Val.False)
        case v: Byte    => ComparableVal(v, Val.Byte(v))
        case v: Char    => ComparableVal(v, Val.Char(v))
        case v: Short   => ComparableVal(v, Val.Short(v))
        case v: Int     => ComparableVal(v, Val.Int(v))
        case v: Long    => ComparableVal(v, Val.Long(v))
        case v: Float   => ComparableVal(v, Val.Float(v))
        case v: Double  => ComparableVal(v, Val.Double(v))
        case v: String  => ComparableVal(v, Val.String(v))
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
