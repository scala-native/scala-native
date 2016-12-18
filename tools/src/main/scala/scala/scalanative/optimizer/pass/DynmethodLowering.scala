package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import nir._

/** Translates high-level structural-type method calls into
 *  low-level dispatch based on a dynmethodtable
 */
class DynmethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import DynmethodLowering._

  override def preInst = {
    case Inst.Let(n, dyn @ Op.Dynmethod(obj, signature)) =>
      val proxySignature = signature + "_proxy"

      val typeptr             = Val.Local(fresh(), Type.Ptr)
      val methodCountPtr      = Val.Local(fresh(), Type.Ptr)
      val methodCount         = Val.Local(fresh(), Type.I32)
      val cond                = Val.Local(fresh(), Type.Bool)
      val thenn               = Next(fresh())
      val elsee               = Next(fresh())
      val endifName           = fresh()
      val dyndispatchTablePtr = Val.Local(fresh(), Type.Ptr)
      val methptrptrThenn     = Val.Local(fresh(), Type.Ptr)
      val methptrptrElsee     = Val.Local(fresh(), Type.Ptr)
      val methptrptr          = Val.Local(fresh(), Type.Ptr)

      val tpe2 = Type.Struct(
        Global.None,
        Seq(Type.I32,
            Type.Ptr,
            Type.Struct(Global.None, Seq(Type.I32, Type.Ptr, Type.Ptr))))

      Seq(
        // Load the type information pointer
        Inst.Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        // Load the pointer of the table size
        Inst.Let(
          methodCountPtr.name,
          Op.Elem(tpe2, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(0)))),
        // Load the table size
        Inst.Let(methodCount.name, Op.Load(Type.I32, methodCountPtr)),
        // Test if size is 1
        Inst.Let(
          cond.name,
          Op.Comp(Comp.Ieq, Type.I32, methodCount, Val.I32(1))
        ),
        Inst.If(cond, thenn, elsee),
        Inst.Label(thenn.name, Seq()),
        // If size is 1, method pointer is in the second place of the struct, no need the call C function
        Inst.Let(
          methptrptrThenn.name,
          Op.Elem(tpe2, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(1)))),
        Inst.Jump(
          Next.Label(endifName,
                     Seq(Val.Local(methptrptrThenn.name, Type.Ptr)))),
        Inst.Label(elsee.name, Seq()),
        // If the size is greater than 1, call the C function "scalanative_dyndispatch"
        // with the signature and it's length as argument
        Inst.Let(
          dyndispatchTablePtr.name,
          Op.Elem(tpe2, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(0)))),
        Inst.Let(
          methptrptrElsee.name,
          Op.Call(dyndispatchSig,
                  dyndispatch,
                  Seq(dyndispatchTablePtr,
                      Val.Const(Val.Chars(proxySignature)),
                      Val.I32(proxySignature.length)),
                  Next.None)
        ),
        Inst.Jump(
          Next.Label(endifName,
                     Seq(Val.Local(methptrptrElsee.name, Type.Ptr)))),
        Inst.Label(endifName, Seq(methptrptr)),
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
