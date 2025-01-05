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

  private val boxIntrinsicType = Lower.BoxTo.collect {
    case (tpe, nir.Global.Member(nir.Global.Top(className), method)) =>
      val nir.Sig.Method(methodName, _, _) = method.unmangled
      ((className, methodName), tpe)
  }
  private val unboxIntrinsicType = Lower.UnboxTo.collect {
    case (tpe, nir.Global.Member(nir.Global.Top(className), method)) =>
      val nir.Sig.Method(methodName, _, _) = method.unmangled
      ((className, methodName), tpe)
  }

  private val Object = nir.Global.Top("java.lang.Object")
  private val Class = nir.Global.Top("java.lang.Class")
  private val Integer = nir.Global.Top("java.lang.Integer$")
  private val Math = nir.Global.Top("java.lang.Math$")

  def intrinsic(
      ty: nir.Type.Function,
      name: nir.Global.Member,
      rawArgs: Seq[nir.Val]
  )(implicit
      state: State,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): Option[nir.Val] = {
    val nir.Global.Member(owner, sig) = name

    def emit(args: Seq[nir.Val]) =
      state.emit(
        nir.Op.Call(
          ty,
          nir.Val.Global(name, nir.Type.Ptr),
          args.map(state.materialize(_))
        )
      )

    name match {
      case nir.Global.Member(Class, nir.Rt.GetClassSig) =>
        val args = rawArgs.map(eval)
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
                Some(emit(args))
            }
          case _ =>
            Some(emit(args))
        }
      case nir.Global.Member(Class, nir.Rt.IsArraySig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(nir.Val.Global(clsName: nir.Global.Top, ty))
              if ty == nir.Rt.Class =>
            Some(nir.Val.Bool(nir.Type.isArray(clsName)))
          case _ =>
            None
        }
      case nir.Global.Member(Class, nir.Rt.IsAssignableFromSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(
                nir.Val.Global(ScopeRef(linfo), lty),
                nir.Val.Global(ScopeRef(rinfo), rty)
              ) if lty == nir.Rt.Class && rty == nir.Rt.Class =>
            Some(nir.Val.Bool(rinfo.is(linfo)))
          case _ =>
            None
        }
      case nir.Global.Member(Class, nir.Rt.GetNameSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(nir.Val.Global(name: nir.Global.Top, ty))
              if ty == nir.Rt.Class =>
            Some(eval(nir.Val.String(name.id)))
          case _ =>
            None
        }
      case nir.Global.Member(Integer, nir.Rt.BitCountSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Int(v)) =>
            Some(nir.Val.Int(java.lang.Integer.bitCount(v)))
          case _ =>
            None
        }
      case nir.Global.Member(Integer, nir.Rt.ReverseBytesSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Int(v)) =>
            Some(nir.Val.Int(java.lang.Integer.reverseBytes(v)))
          case _ =>
            None
        }
      case nir.Global.Member(Integer, nir.Rt.NumberOfLeadingZerosSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Int(v)) =>
            Some(nir.Val.Int(java.lang.Integer.numberOfLeadingZeros(v)))
          case _ =>
            None
        }
      case nir.Global.Member(Math, nir.Rt.CosSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Double(v)) =>
            Some(nir.Val.Double(java.lang.Math.cos(v)))
          case _ =>
            None
        }
      case nir.Global.Member(Math, nir.Rt.SinSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Double(v)) =>
            Some(nir.Val.Double(java.lang.Math.sin(v)))
          case _ =>
            None
        }
      case nir.Global.Member(Math, nir.Rt.PowSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Double(v1), nir.Val.Double(v2)) =>
            Some(nir.Val.Double(java.lang.Math.pow(v1, v2)))
          case _ =>
            None
        }
      case nir.Global.Member(Math, nir.Rt.SqrtSig) =>
        val args = rawArgs.map(eval)
        args match {
          case Seq(_, nir.Val.Double(v)) =>
            Some(nir.Val.Double(java.lang.Math.sqrt(v)))
          case _ =>
            None
        }
      case nir.Global.Member(Math, nir.Rt.MaxSig) =>
        val args = rawArgs.map(eval)
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
      case nir.Global.Member(nir.Rt.Runtime.name, nir.Rt.FromRawPtrSig) =>
        val Seq(_, value) = rawArgs
        Some(eval(nir.Op.Box(nir.Rt.BoxedPtr, value)))
      case nir.Global.Member(nir.Rt.Runtime.name, nir.Rt.ToRawPtrSig) =>
        val Seq(_, value) = rawArgs
        Some(eval(nir.Op.Unbox(nir.Rt.BoxedPtr, value)))
      case nir.Global.Member(nir.Global.Top(className), method) =>
        method.unmangled match {
          case nir.Sig.Method(methodName, _, _) =>
            // Lower.BoxTo and Lower.UnboxTo maps use the companion object name,
            // but java static methods on the class are also present
            // and we need to handle them.
            val isCompanionObject = className.last == '$'
            def param = if (isCompanionObject) {
              val Seq(_, param) = rawArgs
              param
            } else {
              val Seq(param) = rawArgs
              param
            }
            val companionObjectName = if (isCompanionObject) {
              className
            } else {
              className + '$'
            }

            val boxIntrinsic = boxIntrinsicType
              .get((companionObjectName, methodName))
              .map(tpe => eval(nir.Op.Box(tpe, param)))

            val unboxIntrinsic = unboxIntrinsicType
              .get((companionObjectName, methodName))
              .map(tpe => eval(nir.Op.Unbox(tpe, param)))

            boxIntrinsic.orElse(unboxIntrinsic)
          case _ => None
        }
    }
  }
}
