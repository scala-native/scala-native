package scala.scalanative
package interflow

import scalanative.codegen.Lower

import java.util.Arrays
import scalanative.linker._

private[interflow] trait Intrinsics { self: Interflow =>

  val arrayApplyIntrinsics = Lower.arrayApply.values.toSet[nir.Global]
  val arrayUpdateIntrinsics = Lower.arrayUpdate.values.toSet[nir.Global]
  val arrayLengthIntrinsic = Lower.arrayLength
  val arrayIntrinsics =
    arrayApplyIntrinsics ++ arrayUpdateIntrinsics + arrayLengthIntrinsic

  val intrinsics = Set[nir.Global](
    nir.Global.Member(nir.Global.Top("java.lang.Object"), nir.Rt.GetClassSig),
    nir.Global.Member(nir.Global.Top("java.lang.Class"), nir.Rt.IsArraySig),
    nir.Global
      .Member(nir.Global.Top("java.lang.Class"), nir.Rt.IsAssignableFromSig),
    nir.Global.Member(nir.Global.Top("java.lang.Class"), nir.Rt.GetNameSig),
    nir.Global.Member(nir.Global.Top("java.lang.Integer$"), nir.Rt.BitCountSig),
    nir.Global
      .Member(nir.Global.Top("java.lang.Integer$"), nir.Rt.ReverseBytesSig),
    nir.Global.Member(
      nir.Global.Top("java.lang.Integer$"),
      nir.Rt.NumberOfLeadingZerosSig
    ),
    nir.Global.Member(nir.Global.Top("java.lang.Math$"), nir.Rt.CosSig),
    nir.Global.Member(nir.Global.Top("java.lang.Math$"), nir.Rt.SinSig),
    nir.Global.Member(nir.Global.Top("java.lang.Math$"), nir.Rt.PowSig),
    nir.Global.Member(nir.Global.Top("java.lang.Math$"), nir.Rt.MaxSig),
    nir.Global.Member(nir.Global.Top("java.lang.Math$"), nir.Rt.SqrtSig),
    nir.Global.Member(nir.Rt.Runtime.name, nir.Rt.FromRawPtrSig),
    nir.Global.Member(nir.Rt.Runtime.name, nir.Rt.ToRawPtrSig)
  ) ++ arrayIntrinsics

  def intrinsic(
      ty: nir.Type.Function,
      name: nir.Global.Member,
      rawArgs: Seq[nir.Val]
  )(implicit
      state: State,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): Option[nir.Val] = {
    val nir.Global.Member(_, sig) = name

    val args = rawArgs.map(eval)

    def emit =
      state.emit(
        nir.Op.Call(
          ty,
          nir.Val.Global(name, nir.Type.Ptr),
          args.map(state.materialize(_))
        )
      )

    sig match {
      case nir.Rt.GetClassSig =>
        args match {
          case Seq(VirtualRef(_, cls, _)) =>
            Some(nir.Val.Global(cls.name, nir.Rt.Class))
          case Seq(value) =>
            val ty = value match {
              case InstanceRef(ty) => ty
              case _               => value.ty
            }
            ty match {
              case refty: nir.Type.RefKind if refty.isExact =>
                Some(nir.Val.Global(refty.className, nir.Rt.Class))
              case _ =>
                Some(emit)
            }
          case _ =>
            Some(emit)
        }
      case nir.Rt.IsArraySig =>
        args match {
          case Seq(nir.Val.Global(clsName: nir.Global.Top, ty))
              if ty == nir.Rt.Class =>
            Some(nir.Val.Bool(nir.Type.isArray(clsName)))
          case _ =>
            None
        }
      case nir.Rt.IsAssignableFromSig =>
        args match {
          case Seq(
                nir.Val.Global(ScopeRef(linfo), lty),
                nir.Val.Global(ScopeRef(rinfo), rty)
              ) if lty == nir.Rt.Class && rty == nir.Rt.Class =>
            Some(nir.Val.Bool(rinfo.is(linfo)))
          case _ =>
            None
        }
      case nir.Rt.GetNameSig =>
        args match {
          case Seq(nir.Val.Global(name: nir.Global.Top, ty))
              if ty == nir.Rt.Class =>
            Some(eval(nir.Val.String(name.id)))
          case _ =>
            None
        }
      case nir.Rt.BitCountSig =>
        args match {
          case Seq(_, nir.Val.Int(v)) =>
            Some(nir.Val.Int(java.lang.Integer.bitCount(v)))
          case _ =>
            None
        }
      case nir.Rt.ReverseBytesSig =>
        args match {
          case Seq(_, nir.Val.Int(v)) =>
            Some(nir.Val.Int(java.lang.Integer.reverseBytes(v)))
          case _ =>
            None
        }
      case nir.Rt.NumberOfLeadingZerosSig =>
        args match {
          case Seq(_, nir.Val.Int(v)) =>
            Some(nir.Val.Int(java.lang.Integer.numberOfLeadingZeros(v)))
          case _ =>
            None
        }
      case nir.Rt.CosSig =>
        args match {
          case Seq(_, nir.Val.Double(v)) =>
            Some(nir.Val.Double(java.lang.Math.cos(v)))
          case _ =>
            None
        }
      case nir.Rt.SinSig =>
        args match {
          case Seq(_, nir.Val.Double(v)) =>
            Some(nir.Val.Double(java.lang.Math.sin(v)))
          case _ =>
            None
        }
      case nir.Rt.PowSig =>
        args match {
          case Seq(_, nir.Val.Double(v1), nir.Val.Double(v2)) =>
            Some(nir.Val.Double(java.lang.Math.pow(v1, v2)))
          case _ =>
            None
        }
      case nir.Rt.SqrtSig =>
        args match {
          case Seq(_, nir.Val.Double(v)) =>
            Some(nir.Val.Double(java.lang.Math.sqrt(v)))
          case _ =>
            None
        }
      case nir.Rt.MaxSig =>
        args match {
          case Seq(_, nir.Val.Double(v1), nir.Val.Double(v2)) =>
            Some(nir.Val.Double(java.lang.Math.max(v1, v2)))
          case _ =>
            None
        }
      case _ if arrayApplyIntrinsics.contains(name) =>
        val Seq(arr, idx) = rawArgs
        val nir.Type.Function(_, elemty) = ty
        Some(eval(nir.Op.Arrayload(elemty, arr, idx)))
      case _ if arrayUpdateIntrinsics.contains(name) =>
        val Seq(arr, idx, value) = rawArgs
        val nir.Type.Function(Seq(_, _, elemty), _) = ty: @unchecked
        Some(eval(nir.Op.Arraystore(elemty, arr, idx, value)))
      case _ if name == arrayLengthIntrinsic =>
        val Seq(arr) = rawArgs
        Some(eval(nir.Op.Arraylength(arr)))
      case nir.Rt.FromRawPtrSig =>
        val Seq(_, value) = rawArgs
        Some(eval(nir.Op.Box(nir.Rt.BoxedPtr, value)))
      case nir.Rt.ToRawPtrSig =>
        val Seq(_, value) = rawArgs
        Some(eval(nir.Op.Unbox(nir.Rt.BoxedPtr, value)))
    }
  }

  object BoxesRunTimeCall {

    /** The optimizer can remove redundant box/unbox calls, but only do if they
     *  are nir.Op.Box and nir.Op.Unbox NIR definitions. This converts calls in
     *  scala.runtime.BoxesRunTime into the equivalent NIR instructions.
     */
    def unapply(op: nir.Op)(implicit
        state: State,
        srcPosition: nir.SourcePosition,
        scopeId: nir.ScopeId
    ): Option[nir.Val] = {
      op match {
        case nir.Op.Call(
              _,
              nir.Val.Global(
                nir.Global.Member(
                  nir.Global.Top("scala.runtime.BoxesRunTime"),
                  sig
                ),
                _
              ),
              args
            ) =>
          def box(tpe: String) =
            eval(nir.Op.Box(nir.Type.Ref(nir.Global.Top(tpe)), args.head))
          def unbox(tpe: String) =
            eval(nir.Op.Unbox(nir.Type.Ref(nir.Global.Top(tpe)), args.head))

          sig.unmangled match {
            case nir.Sig.Method(name, _, _) =>
              name match {
                case "boxToBoolean"   => Some(box("java.lang.Boolean"))
                case "boxToCharacter" => Some(box("java.lang.Character"))
                case "boxToByte"      => Some(box("java.lang.Byte"))
                case "boxToShort"     => Some(box("java.lang.Short"))
                case "boxToInteger"   => Some(box("java.lang.Integer"))
                case "boxToLong"      => Some(box("java.lang.Long"))
                case "boxToFloat"     => Some(box("java.lang.Float"))
                case "boxToDouble"    => Some(box("java.lang.Double"))

                case "unboxToBoolean" => Some(unbox("java.lang.Boolean"))
                case "unboxToChar"    => Some(unbox("java.lang.Character"))
                case "unboxToByte"    => Some(unbox("java.lang.Byte"))
                case "unboxToShort"   => Some(unbox("java.lang.Short"))
                case "unboxToInt"     => Some(unbox("java.lang.Integer"))
                case "unboxToLong"    => Some(unbox("java.lang.Long"))
                case "unboxToFloat"   => Some(unbox("java.lang.Float"))
                case "unboxToDouble"  => Some(unbox("java.lang.Double"))
                case _                => None
              }

            case _ => None
          }
        case _ => None

      }
    }
  }

}
