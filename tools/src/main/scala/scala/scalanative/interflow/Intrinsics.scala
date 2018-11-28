package scala.scalanative
package interflow

import java.util.Arrays
import scalanative.nir._
import scalanative.linker._

trait Intrinsics { self: Interflow =>
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
  )

  def intrinsic(local: Local,
                ty: Type,
                name: Global,
                args: Seq[Val],
                unwind: Next)(implicit state: State): Val = {
    val Global.Member(_, sig) = name

    def emit = {
      if (unwind ne Next.None) {
        throw BailOut("try-catch")
      }
      state.emit.call(ty,
                      Val.Global(name, Type.Ptr),
                      args.map(state.materialize(_)),
                      unwind)
    }

    sig match {
      case Rt.GetClassSig =>
        args match {
          case Seq(Val.Virtual(thisAddr)) =>
            val cls = state.deref(thisAddr).cls
            val addr =
              state.allocClass(
                linked.infos(Global.Top("java.lang.Class")).asInstanceOf[Class])
            val instance = state.derefVirtual(addr)
            instance.values(0) = Val.Global(cls.name, Type.Ptr)
            Val.Virtual(addr)
          case _ =>
            emit
        }
      case Rt.IsArraySig =>
        args match {
          case Seq(Val.Virtual(addr)) =>
            val Val.Global(clsName, _) = state.derefVirtual(addr).values(0)
            Val.Bool(Type.isArray(clsName))
          case _ =>
            emit
        }
      case Rt.IsAssignableFromSig =>
        args match {
          case Seq(Val.Virtual(leftAddr), Val.Virtual(rightAddr)) =>
            val Val.Global(leftName, _) = state.derefVirtual(leftAddr).values(0)
            val Val.Global(rightName, _) =
              state.derefVirtual(rightAddr).values(0)
            val left  = linked.infos(leftName).asInstanceOf[Class]
            val right = linked.infos(rightName).asInstanceOf[Class]
            Val.Bool(right.subclasses.contains(left))
          case _ =>
            emit
        }
      case Rt.GetNameSig =>
        args match {
          case Seq(Val.Virtual(addr)) =>
            val Val.Global(name: Global.Top, _) =
              state.derefVirtual(addr).values(0)
            eval(Val.String(name.id))(state)
          case _ =>
            emit
        }
      case Rt.BitCountSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Val.Int(java.lang.Integer.bitCount(v))
          case _ =>
            emit
        }
      case Rt.ReverseBytesSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Val.Int(java.lang.Integer.reverseBytes(v))
          case _ =>
            emit
        }
      case Rt.NumberOfLeadingZerosSig =>
        args match {
          case Seq(_, Val.Int(v)) =>
            Val.Int(java.lang.Integer.numberOfLeadingZeros(v))
          case _ =>
            emit
        }
      case Rt.CosSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Val.Double(java.lang.Math.cos(v))
          case _ =>
            emit
        }
      case Rt.SinSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Val.Double(java.lang.Math.sin(v))
          case _ =>
            emit
        }
      case Rt.PowSig =>
        args match {
          case Seq(_, Val.Double(v1), Val.Double(v2)) =>
            Val.Double(java.lang.Math.pow(v1, v2))
          case _ =>
            emit
        }
      case Rt.SqrtSig =>
        args match {
          case Seq(_, Val.Double(v)) =>
            Val.Double(java.lang.Math.sqrt(v))
          case _ =>
            emit
        }
      case Rt.MaxSig =>
        args match {
          case Seq(_, Val.Double(v1), Val.Double(v2)) =>
            Val.Double(java.lang.Math.max(v1, v2))
          case _ =>
            emit
        }
    }
  }
}
