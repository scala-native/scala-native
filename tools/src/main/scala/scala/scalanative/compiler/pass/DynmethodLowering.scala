package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import nir._, Inst.Let
import nir.Type._
import util._

/** Eliminates:
 *  - Op.Dynmethod
 */
class DynmethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import DynmethodLowering._

  override def preInst = {
    case Let(n, dyn @ Op.Dynmethod(obj, signature)) =>
      val typeptr             = Val.Local(fresh(), Type.Ptr)
      val dyndispatchTablePtr = Val.Local(fresh(), Type.Ptr)
      val methptrptr          = Val.Local(fresh(), Type.Ptr)
      

      val tt: Seq[Type] = Seq(Rt.Type.tys, Seq(Type.Struct(Global.None, Seq(Type.Ptr)))).flatten
      val tpe = Struct(Global.None, tt)

      Seq(
        Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Let(dyndispatchTablePtr.name,
          Op.Elem(tpe,
                  typeptr,
                  Seq(Val.I32(0),
                      Val.I32(2),
                      Val.I32(0)))),
        Let(methptrptr.name, Op.Call(dyndispatchSig, dyndispatch, Seq(dyndispatchTablePtr, Val.Const(Val.Chars(signature)), Val.I32(signature.size)))),
        Let(n, Op.Load(Type.Ptr, methptrptr))
      )


  }
}

object DynmethodLowering extends PassCompanion {
  def apply(ctx: Ctx) = new DynmethodLowering()(ctx.fresh, ctx.top)
  
  val dyndispatchName = Global.Top("scalanative_dyndispatch")
  val dyndispatchSig  = Type.Function(Seq(Arg(Type.Ptr), Arg(Type.Ptr), Arg(Type.I32)), Type.Ptr)
  val dyndispatch     = Val.Global(dyndispatchName, dyndispatchSig)

  override val injects = Seq(Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig))

}
