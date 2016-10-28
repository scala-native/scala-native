package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import nir._, Inst.Let
import nir.Type._
import util._

/** Eliminates:
 *  - Op.DynMethod
 */
class DynMethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import DynMethodLowering._

  override def preInst = {
    case Let(n, dyn @ Op.DynMethod(obj, sign)) =>
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
        Let(methptrptr.name, Op.Call(dyndispatchSig, dyndispatch, Seq(dyndispatchTablePtr, Val.Const(Val.Chars(sign)), Val.I32(sign.size)))),
        Let(n, Op.Load(Type.Ptr, methptrptr))
      )


  }
}

object DynMethodLowering extends PassCompanion {
  def apply(ctx: Ctx) = new DynMethodLowering()(ctx.fresh, ctx.top)
  
  val dyndispatchName = Global.Top("scalanative_dyndispatch")
  val dyndispatchSig  = Type.Function(Seq(Arg(Type.Ptr), Arg(Type.Ptr), Arg(Type.I32)), Type.Ptr)
  val dyndispatch     = Val.Global(dyndispatchName, dyndispatchSig)

  override val injects = Seq(Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig))

}
