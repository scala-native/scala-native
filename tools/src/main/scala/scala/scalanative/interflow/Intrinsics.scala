package scala.scalanative
package interflow

import scalanative.codegen.Lower

import java.util.Arrays
import scalanative.nir._
import scalanative.linker._

trait Intrinsics { self: Interflow =>
  val arrayApplyIntrinsics  = Lower.arrayApply.values.toSet[Global]
  val arrayUpdateIntrinsics = Lower.arrayUpdate.values.toSet[Global]
  val arrayLengthIntrinsic  = Lower.arrayLength
  val arrayIntrinsics =
    arrayApplyIntrinsics ++ arrayUpdateIntrinsics + arrayLengthIntrinsic

  val intrinsics = Set[Global](
    Rt.GetRawTypeName,
    Global.Member(Global.Top("java.lang.Class"), Rt.IsArraySig),
    Global.Member(Global.Top("java.lang.Class"), Rt.IsAssignableFromSig),
    Global.Member(Global.Top("java.lang.Class"), Rt.GetNameSig),
    Global.Member(Global.Top("java.lang.Integer$"), Rt.BitCountSig),
    Global.Member(Global.Top("java.lang.Integer$"), Rt.ReverseBytesSig),
    Global.Member(Global.Top("java.lang.Integer$"), Rt.NumberOfLeadingZerosSig),
    Global.Member(Global.Top("java.lang.Math$"), Rt.CosSig),
    Global.Member(Global.Top("java.lang.Math$"), Rt.SinSig),
    Global.Member(Global.Top("java.lang.Math$"), Rt.PowSig),
    Global.Member(Global.Top("java.lang.Math$"), Rt.MaxSig),
    Global.Member(Global.Top("java.lang.Math$"), Rt.SqrtSig)
  ) ++ arrayIntrinsics

  def intrinsic(ty: Type, name: Global, rawArgs: Seq[Val])(
      implicit state: State): Option[Val] = {
    val Global.Member(_, sig) = name

    val args = rawArgs.map(eval)

    def emit =
      state.emit(
        Op.Call(ty, Val.Global(name, Type.Ptr), args.map(state.materialize(_))))

    sig match {
      case Rt.GetRawTypeSig =>
        args match {
          case Seq(_, VirtualRef(_, cls, _)) =>
            Some(Val.Global(cls.name, Type.Ptr))
          case Seq(_, value) =>
            val ty = value match {
              case InstanceRef(ty) => ty
              case _               => value.ty
            }
            ty match {
              case refty: Type.RefKind if refty.isExact && !refty.isNullable =>
                Some(Val.Global(refty.className, Type.Ptr))
              case _ =>
                Some(emit)
            }
          case _ =>
            Some(emit)
        }
      case Rt.IsArraySig =>
        args match {
          case Seq(VirtualRef(_, _, Array(Val.Global(clsName, _)))) =>
            Some(Val.Bool(Type.isArray(clsName)))
          case _ =>
            None
        }
      case Rt.IsAssignableFromSig =>
        args match {
          case Seq(VirtualRef(_, _, Array(Val.Global(ScopeRef(linfo), _))),
                   VirtualRef(_, _, Array(Val.Global(ScopeRef(rinfo), _)))) =>
            Some(Val.Bool(rinfo.is(linfo)))
          case _ =>
            None
        }
      case Rt.GetNameSig =>
        args match {
          case Seq(VirtualRef(_, _, Array(Val.Global(name: Global.Top, _)))) =>
            Some(eval(Val.String(name.id))(state))
          case _ =>
            None
        }
      case Rt.BitCountSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Some(Val.Int(java.lang.Integer.bitCount(v)))
          case _ =>
            None
        }
      case Rt.ReverseBytesSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Some(Val.Int(java.lang.Integer.reverseBytes(v)))
          case _ =>
            None
        }
      case Rt.NumberOfLeadingZerosSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Some(Val.Int(java.lang.Integer.numberOfLeadingZeros(v)))
          case _ =>
            None
        }
      case Rt.CosSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Some(Val.Double(java.lang.Math.cos(v)))
          case _ =>
            None
        }
      case Rt.SinSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Some(Val.Double(java.lang.Math.sin(v)))
          case _ =>
            None
        }
      case Rt.PowSig =>
        args match {
          case Seq(_, Val.Double(v1), Val.Double(v2)) =>
            Some(Val.Double(java.lang.Math.pow(v1, v2)))
          case _ =>
            None
        }
      case Rt.SqrtSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Some(Val.Double(java.lang.Math.sqrt(v)))
          case _ =>
            None
        }
      case Rt.MaxSig =>
        args match {
          case Seq(_, Val.Double(v1), Val.Double(v2)) =>
            Some(Val.Double(java.lang.Math.max(v1, v2)))
          case _ =>
            None
        }
      case _ if arrayApplyIntrinsics.contains(name) =>
        val Seq(arr, idx)            = rawArgs
        val Type.Function(_, elemty) = ty
        Some(eval(Op.Arrayload(elemty, arr, idx)))
      case _ if arrayUpdateIntrinsics.contains(name) =>
        val Seq(arr, idx, value)                = rawArgs
        val Type.Function(Seq(_, _, elemty), _) = ty
        Some(eval(Op.Arraystore(elemty, arr, idx, value)))
      case _ if name == arrayLengthIntrinsic =>
        val Seq(arr) = rawArgs
        Some(eval(Op.Arraylength(arr)))
    }
  }
}
