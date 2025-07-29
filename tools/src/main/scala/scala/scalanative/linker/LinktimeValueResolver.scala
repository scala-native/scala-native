package scala.scalanative
package linker

import scala.collection.mutable
import scala.scalanative.build._
import scala.scalanative.util.unsupported

private[linker] trait LinktimeValueResolver { self: Reach =>

  import LinktimeValueResolver._

  private final val linktimeInfo =
    "scala.scalanative.meta.linktimeinfo"
  private final val contendedPaddingWidth =
    s"$linktimeInfo.contendedPaddingWidth"

  private lazy val linktimeProperties = {
    val conf = config.compilerConfig
    val triple = conf.configuredOrDetectedTriple
    val predefined: NativeConfig.LinktimeProperites = Map(
      s"$linktimeInfo.debugMetadata.enabled" -> conf.sourceLevelDebuggingConfig.enabled,
      s"$linktimeInfo.debugMetadata.generateFunctionSourcePositions" -> conf.sourceLevelDebuggingConfig.generateFunctionSourcePositions,
      s"$linktimeInfo.debugMode" -> (conf.mode == Mode.debug),
      s"$linktimeInfo.releaseMode" -> (conf.mode == Mode.releaseFast || conf.mode == Mode.releaseFull || conf.mode == Mode.releaseSize),
      s"$linktimeInfo.isMultithreadingEnabled" -> conf.multithreadingSupport,
      s"$linktimeInfo.isWeakReferenceSupported" -> {
        conf.gc == GC.Immix ||
        conf.gc == GC.Commix
      },
      s"$linktimeInfo.is32BitPlatform" -> conf.is32BitPlatform,
      s"$linktimeInfo.enabledSanitizer" -> conf.sanitizer
        .map(_.name)
        .getOrElse(""),
      s"$linktimeInfo.isMsys" -> config.targetsMsys,
      s"$linktimeInfo.isCygwin" -> config.targetsCygwin,
      s"$linktimeInfo.runtimeVersion" -> nir.Versions.current,
      s"$linktimeInfo.garbageCollector" -> conf.gc.name,
      s"$linktimeInfo.target.arch" -> triple.arch,
      s"$linktimeInfo.target.vendor" -> triple.vendor,
      s"$linktimeInfo.target.os" -> triple.os,
      s"$linktimeInfo.target.env" -> triple.env,
      contendedPaddingWidth -> 64 // bytes; can be overriten
    )
    NativeConfig.checkLinktimeProperties(predefined)
    predefined ++ conf.linktimeProperties
  }

  private val resolvedValues = mutable.Map.empty[String, LinktimeValue]

  // required for @scala.scalanative.annotation.align(), always resolve
  resolveLinktimeProperty(contendedPaddingWidth)(nir.SourcePosition.NoPosition)

  // For compat with 2.13 where mapValues is deprecated
  def resolvedNirValues: mutable.Map[String, nir.Val] = resolvedValues.map {
    case (k, v) => k -> v.nirValue
  }

  protected def resolveLinktimeDefine(
      defn: nir.Defn.Define
  ): nir.Defn.Define = {
    implicit def position: nir.SourcePosition = defn.pos

    def evaluated() = {
      implicit val fresh = nir.Fresh()
      lazy val buf = {
        val buf = new nir.InstructionBuilder()
        buf += defn.insts.head
        buf
      }

      defn.insts match {
        case Seq(_, nir.Inst.Ret(_)) => defn

        case Seq(
              _,
              nir.Inst.Let(_, ReferencedPropertyOp(propertyName), _),
              nir.Inst.Ret(_)
            ) =>
          val value = resolveLinktimeProperty(propertyName)
          resolvedValues.getOrElseUpdate(propertyName, value)
          buf.ret(value.nirValue)
          defn.copy(insts = buf.toSeq)

        case _ =>
          val mangledName = nir.Mangle(defn.name)
          val value = resolveLinktimeProperty(mangledName)
          buf.ret(value.nirValue)
          resolvedValues.getOrElseUpdate(mangledName, value)
          defn.copy(insts = buf.toSeq)
      }

    }

    def partiallyEvaluated() = {
      val resolvedInsts = nir.ControlFlow.removeDeadBlocks {
        defn.insts.map {
          case inst: nir.Inst.LinktimeIf => resolveLinktimeIf(inst)
          case inst @ nir.Inst.Let(_, ReferencedPropertyOp(propertyName), _) =>
            val resolvedVal = resolveLinktimeProperty(propertyName).nirValue
            inst.copy(op = nir.Op.Copy(resolvedVal))(inst.pos, inst.scopeId)
          case inst => inst
        }
      }

      defn.copy(insts = resolvedInsts)
    }

    def isRuntimeOnly(inst: nir.Inst): Boolean = inst match {
      case nir.Inst.Label(_, _)             => false
      case nir.Inst.LinktimeIf(_, _, _)     => false
      case nir.Inst.Jump(_: nir.Next.Label) => false
      case nir.Inst.Ret(_)                  => false
      case nir.Inst.Let(_, op, nir.Next.None) =>
        op match {
          case nir.Op.Call(_, nir.Val.Global(name, _), _) =>
            track(name)(inst.pos)
            name != nir.Linktime.PropertyResolveFunctionName &&
              !lookup(name).exists(_.attrs.isLinktimeResolved)
          case _: nir.Op.Comp => false
          case _              => true
        }
      case _ => true
    }

    def canBeEvauluated =
      !defn.insts.exists(isRuntimeOnly) && {
        val nir.Type.Function(_, retty) = defn.ty
        retty match {
          case _: nir.Type.ValueKind    => true
          case nir.Type.Ref(name, _, _) => name == nir.Rt.String.name
          case nir.Type.Null            => true
          case _                        => false
        }
      }

    if (defn.attrs.isLinktimeResolved)
      if (canBeEvauluated) evaluated()
      else partiallyEvaluated()
    else defn
  }

  private def resolveLinktimeProperty(name: String)(implicit
      pos: nir.SourcePosition
  ): LinktimeValue =
    resolvedValues.getOrElseUpdate(name, lookupLinktimeProperty(name))

  private def lookupLinktimeProperty(
      propertyName: String
  )(implicit pos: nir.SourcePosition): LinktimeValue = {
    def fromProvidedValue =
      linktimeProperties
        .get(propertyName)
        .map(ComparableVal.fromAny(_).asAny)

    def fromCalculatedValue =
      scala.util
        .Try(nir.Unmangle.unmangleGlobal(propertyName))
        .toOption
        .flatMap(lookup(_))
        .collect {
          case defn: nir.Defn.Define if defn.attrs.isLinktimeResolved =>
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
      cond: nir.LinktimeCondition
  )(implicit pos: nir.SourcePosition): Boolean = {
    import nir.LinktimeCondition._

    cond match {
      case ComplexCondition(nir.Bin.And, left, right) =>
        resolveCondition(left) && resolveCondition(right)

      case ComplexCondition(nir.Bin.Or, left, right) =>
        resolveCondition(left) || resolveCondition(right)

      case SimpleCondition(name, comparison, condVal) =>
        val resolvedValue = resolveLinktimeProperty(name)

        (ComparableVal.fromNir(condVal), resolvedValue) match {
          case ComparableTuple(ordering, condition, resolved) =>
            val comparsionFn = comparison match {
              case nir.Comp.Ieq | nir.Comp.Feq => ordering.equiv _
              case nir.Comp.Ine | nir.Comp.Fne =>
                !ordering.equiv(_: Any, _: Any)
              case nir.Comp.Sgt | nir.Comp.Ugt | nir.Comp.Fgt => ordering.gt _
              case nir.Comp.Sge | nir.Comp.Uge | nir.Comp.Fge => ordering.gteq _
              case nir.Comp.Slt | nir.Comp.Ult | nir.Comp.Flt => ordering.lt _
              case nir.Comp.Sle | nir.Comp.Ule | nir.Comp.Fle => ordering.lteq _
            }

            comparsionFn(resolved.value, condition.value)

          // In case if we cannot get common Ordering that can be used, e.g.: comparison with Null
          case (ComparableVal(condition, _), ComparableVal(resolved, _)) =>
            comparison match {
              case nir.Comp.Ieq | nir.Comp.Feq => resolved == condition
              case nir.Comp.Ine | nir.Comp.Fne => resolved != condition
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
      inst: nir.Inst.LinktimeIf
  )(implicit pos: nir.SourcePosition): nir.Inst.Jump = {
    val nir.Inst.LinktimeIf(cond, thenp, elsep) = inst

    val matchesCondition = resolveCondition(cond)
    if (matchesCondition) nir.Inst.Jump(thenp)
    else nir.Inst.Jump(elsep)
  }

  private def interpretLinktimeDefn(defn: nir.Defn.Define): nir.Val = {
    require(defn.attrs.isLinktimeResolved)
    val cf = nir.ControlFlow.Graph(defn.insts)
    val locals = scala.collection.mutable.Map.empty[nir.Val.Local, nir.Val]

    def resolveLocalVal(local: nir.Val.Local): nir.Val = locals(local) match {
      case v: nir.Val.Local => resolveLocalVal(v)
      case value            => value
    }

    def interpretBlock(block: nir.ControlFlow.Block): nir.Val = {
      def interpret(inst: nir.Inst): nir.Val = inst match {
        case nir.Inst.Ret(value) =>
          value match {
            case v: nir.Val.Local => resolveLocalVal(v)
            case _                => value
          }

        case nir.Inst.Jump(next) =>
          val nextBlock = cf.find(next.id)
          next match {
            case nir.Next.Label(_, values) =>
              locals ++= nextBlock.params.zip(values).toMap
            case _ =>
              scalanative.util.unsupported(
                "Only normal labels are expected in linktime resolved methods"
              )
          }
          interpretBlock(nextBlock)

        case nir.Inst.Label(next, params) =>
          val insts = cf.find(next).insts
          assert(insts.size == 1)
          interpret(insts.head)

        case branch: nir.Inst.LinktimeIf =>
          interpret(resolveLinktimeIf(branch)(branch.pos))

        case _: nir.Inst.If | _: nir.Inst.Let | _: nir.Inst.Switch |
            _: nir.Inst.Throw | _: nir.Inst.Unreachable =>
          scalanative.util.unsupported(
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
    def unapply(op: nir.Op): Option[String] = op match {
      case nir.Op.Call(
            _,
            nir.Val.Global(nir.Linktime.PropertyResolveFunctionName, _),
            Seq(nir.Val.String(propertyName))
          ) =>
        Some(propertyName)
      case _ => None
    }
  }

  case class ComparableVal[T: Ordering](value: T, nirValue: nir.Val)(implicit
      val ordering: Ordering[T]
  ) {
    def asAny: ComparableVal[Any] = this.asInstanceOf[ComparableVal[Any]]
  }

  object ComparableVal {
    def fromAny(value: Any): ComparableVal[_] = {
      value match {
        case v: Boolean =>
          ComparableVal(v, if (v) nir.Val.True else nir.Val.False)
        case v: Byte   => ComparableVal(v, nir.Val.Byte(v))
        case v: Char   => ComparableVal(v, nir.Val.Char(v))
        case v: Short  => ComparableVal(v, nir.Val.Short(v))
        case v: Int    => ComparableVal(v, nir.Val.Int(v))
        case v: Long   => ComparableVal(v, nir.Val.Long(v))
        case v: Float  => ComparableVal(v, nir.Val.Float(v))
        case v: Double => ComparableVal(v, nir.Val.Double(v))
        case v: String => ComparableVal(v, nir.Val.String(v))
        case other =>
          throw new LinkingException(
            s"Unsupported value for link-time resolving: $other"
          )
      }
    }

    def fromNir(v: nir.Val): LinktimeValue = {
      v match {
        case nir.Val.String(value) => ComparableVal(value, v)
        case nir.Val.True          => ComparableVal(true, v)
        case nir.Val.False         => ComparableVal(false, v)
        case nir.Val.Byte(value)   => ComparableVal(value, v)
        case nir.Val.Char(value)   => ComparableVal(value, v)
        case nir.Val.Short(value)  => ComparableVal(value, v)
        case nir.Val.Int(value)    => ComparableVal(value, v)
        case nir.Val.Long(value)   => ComparableVal(value, v)
        case nir.Val.Float(value)  => ComparableVal(value, v)
        case nir.Val.Double(value) => ComparableVal(value, v)
        case nir.Val.Null          => ComparableVal(null, v)
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
