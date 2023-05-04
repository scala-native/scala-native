package scala.scalanative.linker

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.build._
import scala.scalanative.util.unsupported

trait LinktimeValueResolver { self: Reach =>
  import LinktimeValueResolver._

  private lazy val linktimeProperties = {
    val conf = config.compilerConfig
    val triple = conf.configuredOrDetectedTriple
    val linktimeInfo = "scala.scalanative.meta.linktimeinfo"
    val predefined: NativeConfig.LinktimeProperites = Map(
      s"$linktimeInfo.debugMode" -> (conf.mode == Mode.debug),
      s"$linktimeInfo.releaseMode" -> (conf.mode == Mode.releaseFast || conf.mode == Mode.releaseFull || conf.mode == Mode.releaseSize),
      s"$linktimeInfo.isMultithreadingEnabled" -> conf.multithreadingSupport,
      s"$linktimeInfo.isWeakReferenceSupported" -> {
        conf.gc == GC.Immix ||
        conf.gc == GC.Commix
      },
      s"$linktimeInfo.is32BitPlatform" -> conf.is32BitPlatform,
      s"$linktimeInfo.asanEnabled" -> conf.asan,
      s"$linktimeInfo.isMsys" -> Platform.isMsys,
      s"$linktimeInfo.isCygwin" -> Platform.isCygwin,
      s"$linktimeInfo.target.arch" -> triple.arch,
      s"$linktimeInfo.target.vendor" -> triple.vendor,
      s"$linktimeInfo.target.os" -> triple.os,
      s"$linktimeInfo.target.env" -> triple.env
    )
    NativeConfig.checkLinktimeProperties(predefined)
    predefined ++ conf.linktimeProperties
  }

  private val resolvedValues = mutable.Map.empty[String, LinktimeValue]
  // For compat with 2.13 where mapValues is deprecated
  def resolvedNirValues: mutable.Map[String, Val] = resolvedValues.map {
    case (k, v) => k -> v.nirValue
  }

  protected def resolveLinktimeDefine(defn: Defn.Define): Defn.Define = {
    implicit def position: Position = defn.pos

    def evaluated() = {
      implicit val fresh = Fresh()
      lazy val buf = {
        val buf = new Buffer()
        buf += defn.insts.head
        buf
      }

      defn.insts match {
        case Seq(_, Inst.Ret(_)) => defn

        case Seq(
              _,
              Inst.Let(_, ReferencedPropertyOp(propertyName), _),
              Inst.Ret(_)
            ) =>
          val value = resolveLinktimeProperty(propertyName)
          resolvedValues.getOrElseUpdate(propertyName, value)
          buf.ret(value.nirValue)
          defn.copy(insts = buf.toSeq)

        case _ =>
          val mangledName = Mangle(defn.name)
          val value = resolveLinktimeProperty(mangledName)
          buf.ret(value.nirValue)
          resolvedValues.getOrElseUpdate(mangledName, value)
          defn.copy(insts = buf.toSeq)
      }

    }

    def partiallyEvaluated() = {
      val resolvedInsts = ControlFlow.removeDeadBlocks {
        defn.insts.map {
          case inst: Inst.LinktimeIf => resolveLinktimeIf(inst)
          case inst @ Inst.Let(_, ReferencedPropertyOp(propertyName), _) =>
            val resolvedVal = resolveLinktimeProperty(propertyName).nirValue
            inst.copy(op = Op.Copy(resolvedVal))
          case inst => inst
        }
      }

      defn.copy(insts = resolvedInsts)
    }

    def isRuntimeOnly(inst: Inst): Boolean = inst match {
      case Inst.Label(_, _)         => false
      case Inst.LinktimeIf(_, _, _) => false
      case Inst.Jump(_: Next.Label) => false
      case Inst.Ret(_)              => false
      case Inst.Let(_, op, Next.None) =>
        op match {
          case Op.Call(_, Val.Global(name, _), _) =>
            name != Linktime.PropertyResolveFunctionName &&
              !lookup(name).exists(_.attrs.isLinktimeResolved)
          case _: Op.Comp => false
          case _          => true
        }
      case _ => true
    }

    def canBeEvauluated =
      !defn.insts.exists(isRuntimeOnly) && {
        defn.ty match {
          case Type.Function(_, retty) =>
            retty match {
              case _: Type.ValueKind    => true
              case Type.Ref(name, _, _) => name == Rt.String.name
              case Type.Null            => true
              case _                    => false
            }
          case _ => false
        }
      }

    if (defn.attrs.isLinktimeResolved)
      if (canBeEvauluated) evaluated()
      else partiallyEvaluated()
    else defn
  }

  private def resolveLinktimeProperty(name: String)(implicit
      pos: Position
  ): LinktimeValue =
    resolvedValues.getOrElseUpdate(name, lookupLinktimeProperty(name))

  private def lookupLinktimeProperty(
      propertyName: String
  )(implicit pos: Position): LinktimeValue = {
    def fromProvidedValue =
      linktimeProperties
        .get(propertyName)
        .map(ComparableVal.fromAny(_).asAny)

    def fromCalculatedValue =
      util
        .Try(Unmangle.unmangleGlobal(propertyName))
        .toOption
        .flatMap(lookup(_))
        .collect {
          case defn: Defn.Define if defn.attrs.isLinktimeResolved =>
            try interpretLinktimeDefn(defn)
            catch {
              case ex: Exception =>
                throw new LinkingException(
                  s"Link-time method `$propertyName` cannot be interpreted at linktime"
                )
            }
        }
        .map(ComparableVal.fromNir)

    fromProvidedValue
      .orElse(fromCalculatedValue)
      .getOrElse {
        throw new LinkingException(
          s"Link-time property named `$propertyName` not defined in the config"
        )
      }
  }

  private def resolveCondition(
      cond: LinktimeCondition
  )(implicit pos: Position): Boolean = {
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
              case Comp.Ieq | Comp.Feq => ordering.equiv _
              case Comp.Ine | Comp.Fne => !ordering.equiv(_: Any, _: Any)
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
                throw new LinkingException(
                  s"Unsupported link-time comparison $comparison between types ${condVal.ty} and ${resolvedValue.nirValue.ty}"
                )
            }
        }
      case _ =>
        throw new LinkingException(s"Unknown link-time condition: $cond")
    }
  }

  private def resolveLinktimeIf(
      inst: Inst.LinktimeIf
  )(implicit pos: Position): Inst.Jump = {
    val Inst.LinktimeIf(cond, thenp, elsep) = inst

    val matchesCondition = resolveCondition(cond)
    if (matchesCondition) Inst.Jump(thenp)
    else Inst.Jump(elsep)
  }

  private def interpretLinktimeDefn(defn: Defn.Define): Val = {
    require(defn.attrs.isLinktimeResolved)
    val cf = ControlFlow.Graph(defn.insts)
    val locals = scala.collection.mutable.Map.empty[Val.Local, Val]

    def resolveLocalVal(local: Val.Local): Val = locals(local) match {
      case v: Val.Local => resolveLocalVal(v)
      case value        => value
    }

    def interpretBlock(block: ControlFlow.Block): Val = {
      def interpret(inst: Inst): Val = inst match {
        case Inst.Ret(value) =>
          value match {
            case v: Val.Local => resolveLocalVal(v)
            case _            => value
          }

        case Inst.Jump(next) =>
          val nextBlock = cf.find(next.name)
          next match {
            case Next.Label(_, values) =>
              locals ++= nextBlock.params.zip(values).toMap
            case _ =>
              unsupported(
                "Only normal labels are expected in linktime resolved methods"
              )
          }
          interpretBlock(nextBlock)

        case Inst.Label(next, params) =>
          val insts = cf.find(next).insts
          assert(insts.size == 1)
          interpret(insts.head)

        case branch: Inst.LinktimeIf =>
          interpret(resolveLinktimeIf(branch)(branch.pos))

        case _: Inst.If | _: Inst.Let | _: Inst.Switch | _: Inst.Throw |
            _: Inst.Unreachable =>
          unsupported(
            "Unexpected instruction found in linktime resolved method: " + inst
          )
      }

      // Linktime resolved values always have blocks of size 1
      assert(block.insts.size == 1)
      interpret(block.insts.head)
    }
    interpretBlock(cf.entry)
  }

}

private[linker] object LinktimeValueResolver {
  type LinktimeValue = ComparableVal[Any]

  object ReferencedPropertyOp {
    def unapply(op: Op): Option[String] = op match {
      case Op.Call(
            _,
            Val.Global(Linktime.PropertyResolveFunctionName, _),
            Seq(Val.String(propertyName))
          ) =>
        Some(propertyName)
      case _ => None
    }
  }

  case class ComparableVal[T: Ordering](value: T, nirValue: Val)(implicit
      val ordering: Ordering[T]
  ) {
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
        case other =>
          throw new LinkingException(
            s"Unsupported value for link-time resolving: $other"
          )
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
          throw new LinkingException(
            s"Unsupported NIR value for link-time resolving: $other"
          )
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

        case (
              ComparableVal(lValue: Number, lNir),
              ComparableVal(rValue: Number, rNir)
            ) =>
          Some {
            (
              implicitly[Ordering[Double]],
              ComparableVal(lValue.doubleValue(), lNir).asAny,
              ComparableVal(rValue.doubleValue(), rNir)
            )
          }

        case _ | null => None
      }
    }.map(_.asInstanceOf[ComparableTupleType])
  }

}
