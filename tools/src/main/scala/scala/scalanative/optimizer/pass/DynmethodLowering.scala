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
      val condZero            = Val.Local(fresh(), Type.Bool)
      val condOne             = Val.Local(fresh(), Type.Bool)
      val labelZero           = Next(fresh())
      val labelEndZero        = Next(fresh())
      val labelOne            = Next(fresh())
      val labelElse           = Next(fresh())
      val endifName           = fresh()
      val dyndispatchTablePtr = Val.Local(fresh(), Type.Ptr)
      val methptrptrThenn     = Val.Local(fresh(), Type.Ptr)
      val methptrptrElsee     = Val.Local(fresh(), Type.Ptr)
      val methptrptr          = Val.Local(fresh(), Type.Ptr)
      val condNull            = Val.Local(fresh(), Type.Bool)
      val labelIsNull         = Next(fresh())
      val labelEndNull        = Next(fresh())

      val tpe2 = Type.Struct(
        Global.None,
        Seq(Type.I32,
            Type.Ptr,
            Type.Struct(Global.None, Seq(Type.I32, Type.Ptr, Type.Ptr))))

      def throwInstrs(): Seq[Inst] = {

        val excptn = Val.Local(fresh(), Type.Class(excptnGlobal))
        val unit   = Val.Local(fresh(), Type.Unit)
        val init   = Val.Local(fresh(), Type.Ptr)

        Seq(
          Inst.Let(excptn.name, Op.Classalloc(excptnGlobal)),
          Inst.Let(
            unit.name,
            Op.Call(
              Type.Function(
                Seq(
                  Type.Class(excptnGlobal),
                  Type.Class(Global.Top("java.lang.String"))
                ),
                Type.Unit
              ),
              Val.Global(excptnInitGlobal, Type.Ptr),
              Seq(
                excptn,
                Val.String(signature)
              ),
              Next.None
            )
          ),
          Inst.Let(fresh(), Op.Throw(excptn, Next.None))
        )
      }

      Seq(
        Seq( // Load the type information pointer
          Inst.Let(typeptr.name, Op.Load(Type.Ptr, obj)),
          // Load the pointer of the table size
          Inst.Let(
            methodCountPtr.name,
            Op.Elem(tpe2, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(0)))),
          // Load the table size
          Inst.Let(methodCount.name, Op.Load(Type.I32, methodCountPtr)),
          Inst.Let(
            condZero.name,
            Op.Comp(Comp.Ieq, Type.I32, methodCount, Val.I32(0))
          ),
          Inst.If(condZero, labelZero, labelEndZero),
          Inst.Label(labelZero.name, Seq())
        ),
        throwInstrs(),
        Seq(
          Inst.Label(labelEndZero.name, Seq()),
          // Test if size is 1
          Inst.Let(
            condOne.name,
            Op.Comp(Comp.Ieq, Type.I32, methodCount, Val.I32(1))
          ),
          Inst.If(condOne, labelOne, labelElse),
          Inst.Label(labelOne.name, Seq()),
          // If size is 1, method pointer is in the second place of the struct, no need the call C function
          Inst.Let(
            methptrptrThenn.name,
            Op.Elem(tpe2, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(1)))),
          Inst.Jump(
            Next.Label(endifName,
                       Seq(Val.Local(methptrptrThenn.name, Type.Ptr)))),
          Inst.Label(labelElse.name, Seq()),
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
          Inst.Let(n, Op.Load(Type.Ptr, methptrptr)),
          Inst.Let(
            condNull.name,
            Op.Comp(Comp.Ieq, Type.Ptr, Val.Local(n, Type.Ptr), Val.Null)),
          Inst.If(condNull, labelIsNull, labelEndNull),
          Inst.Label(labelIsNull.name, Seq())
        ),
        throwInstrs(),
        Seq(
          Inst.Label(labelEndNull.name, Seq())
        )
      ).flatten

  }
}

object DynmethodLowering extends PassCompanion {
  def apply(config: tools.Config, top: Top): Pass =
    new DynmethodLowering()(top.fresh, top)

  val dyndispatchName = Global.Top("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.Ptr, Type.I32), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    Global.Member(excptnGlobal, "init_class.java.lang.String")

  override val injects = Seq(
    Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig)
  )

  override def depends: Seq[Global] = Seq(excptnGlobal, excptnInitGlobal)
}
