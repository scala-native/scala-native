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
    Global.Member(Global.Top("java.lang.Object"), Rt.GetClassSig),
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
      implicit state: State): Val = {
    val Global.Member(_, sig) = name

    val args = rawArgs.map(eval)

    def fallback =
      state.emit(
        Op.Call(ty, Val.Global(name, Type.Ptr), args.map(state.materialize(_))))

    sig match {
      case Rt.GetClassSig =>
        args match {
          case Seq(VirtualRef(_, cls, _)) =>
            val addr =
              state.allocClass(
                linked.infos(Global.Top("java.lang.Class")).asInstanceOf[Class])
            val instance = state.derefVirtual(addr)
            instance.values(0) = Val.Global(cls.name, Type.Ptr)
            Val.Virtual(addr)
          case _ =>
            fallback
        }
      case Rt.IsArraySig =>
        args match {
          case Seq(VirtualRef(_, _, Array(Val.Global(clsName, _)))) =>
            Val.Bool(Type.isArray(clsName))
          case _ =>
            fallback
        }
      case Rt.IsAssignableFromSig =>
        args match {
          case Seq(VirtualRef(_, _, Array(Val.Global(ScopeRef(linfo), _))),
                   VirtualRef(_, _, Array(Val.Global(ScopeRef(rinfo), _)))) =>
            Val.Bool(rinfo.is(linfo))
          case _ =>
            fallback
        }
      case Rt.GetNameSig =>
        args match {
          case Seq(VirtualRef(_, _, Array(Val.Global(name: Global.Top, _)))) =>
            eval(Val.String(name.id))(state)
          case _ =>
            fallback
        }
      case Rt.BitCountSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Val.Int(java.lang.Integer.bitCount(v))
          case _ =>
            fallback
        }
      case Rt.ReverseBytesSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Val.Int(java.lang.Integer.reverseBytes(v))
          case _ =>
            fallback
        }
      case Rt.NumberOfLeadingZerosSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Val.Int(java.lang.Integer.numberOfLeadingZeros(v))
          case _ =>
            fallback
        }
      case Rt.CosSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Val.Double(java.lang.Math.cos(v))
          case _ =>
            fallback
        }
      case Rt.SinSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Val.Double(java.lang.Math.sin(v))
          case _ =>
            fallback
        }
      case Rt.PowSig =>
        args match {
          case Seq(_, Val.Double(v1), Val.Double(v2)) =>
            Val.Double(java.lang.Math.pow(v1, v2))
          case _ =>
            fallback
        }
      case Rt.SqrtSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Val.Double(java.lang.Math.sqrt(v))
          case _ =>
            fallback
        }
      case Rt.MaxSig =>
        args match {
          case Seq(_, Val.Double(v1), Val.Double(v2)) =>
            Val.Double(java.lang.Math.max(v1, v2))
          case _ =>
            fallback
        }
      case _ if arrayApplyIntrinsics.contains(name) =>
        val Seq(arr, idx)            = rawArgs
        val Type.Function(_, elemty) = ty
        eval(Op.Arrayload(elemty, arr, idx))
      case _ if arrayUpdateIntrinsics.contains(name) =>
        val Seq(arr, idx, value)                = rawArgs
        val Type.Function(Seq(_, _, elemty), _) = ty
        eval(Op.Arraystore(elemty, arr, idx, value))
      case _ if name == arrayLengthIntrinsic =>
        val Seq(arr) = rawArgs
        eval(Op.Arraylength(arr))
    }
  }
}
