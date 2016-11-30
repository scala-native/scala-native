package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import nir._

/** Eliminates:
 *  - Op.Dynmethod
 */
class DynmethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import DynmethodLowering._

  override def preInst = {
    case Inst.Let(n, dyn @ Op.Dynmethod(obj, signature)) =>
      val proxySignature      = signature + "_proxy"
      val typeptr             = Val.Local(fresh(), Type.Ptr)
      val dyndispatchTablePtr = Val.Local(fresh(), Type.Ptr)
      val methptrptr          = Val.Local(fresh(), Type.Ptr)

      val tpe = Type.Struct(
        Global.None,
        Seq(Rt.Type.tys, Seq(Type.Struct(Global.None, Seq(Type.Ptr)))).flatten)

      Seq(
        Inst.Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Inst.Let(
          dyndispatchTablePtr.name,
          Op.Elem(tpe, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(0)))),
        Inst.Let(
          methptrptr.name,
          Op.Call(dyndispatchSig,
                  dyndispatch,
                  Seq(dyndispatchTablePtr,
                      Val.Const(Val.Chars(proxySignature)),
                      Val.I32(proxySignature.length)),
                  Next.None)
        ),
        Inst.Let(n, Op.Load(Type.Ptr, methptrptr))
      )

  }
}

object DynmethodLowering extends PassCompanion {
  def apply(config: tools.Config, top: Top): Pass =
    new DynmethodLowering()(top.fresh, top)

  val dyndispatchName = Global.Top("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.Ptr, Type.I32), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  override val injects = Seq(
    Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig))
}
