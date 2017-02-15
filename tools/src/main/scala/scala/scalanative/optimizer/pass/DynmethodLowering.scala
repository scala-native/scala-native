package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import nir._

/** Translates high-level structural-type method calls into
 *  low-level dispatch based on a dynmethodtable
 */
class DynmethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import DynmethodLowering._

  override def onInsts(insts: Seq[Inst]) = {
    val buf = mutable.UnrolledBuffer.empty[Inst]

    def let(n: Local, op: Op) = buf += Inst.Let(n, op)

    insts.foreach {
      case Inst.Let(n, dyn @ Op.Dynmethod(obj, signature)) =>
        val typeptr             = Val.Local(fresh(), Type.Ptr)
        val methodCountPtr      = Val.Local(fresh(), Type.Ptr)
        val methodCount         = Val.Local(fresh(), Type.I32)
        val dyndispatchTablePtr = Val.Local(fresh(), Type.Ptr)
        val methptrptr          = Val.Local(fresh(), Type.Ptr)

        val rtiType = Type.Struct(
          Global.None,
          Seq(Type.I32,
              Type.Ptr,
              Type.Struct(Global.None, Seq(Type.I32, Type.Ptr, Type.Ptr))))

        def throwInstrs(): Seq[Inst] = {

          val excptn = Val.Local(fresh(), Type.Class(excptnGlobal))
          val unit   = Val.Local(fresh(), Type.Unit)
          val init   = Val.Local(fresh(), Type.Ptr)

          let(excptn.name, Op.Classalloc(excptnGlobal))
          let(
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
          )
          let(fresh(), Op.Throw(excptn, Next.None))
          buf += Inst.Unreachable
        }

        def throwIfCond(cond: Op.Comp): Seq[Inst] = {
          val condNull     = Val.Local(fresh(), Type.Bool)
          val labelIsNull  = Next(fresh())
          val labelEndNull = Next(fresh())

          let(condNull.name, cond)
          buf += Inst.If(condNull, labelIsNull, labelEndNull)
          buf += Inst.Label(labelIsNull.name, Seq())
          throwInstrs()
          buf += Inst.Label(labelEndNull.name, Seq())
        }

        def throwIfNull(local: Val.Local) =
          throwIfCond(Op.Comp(Comp.Ieq, Type.Ptr, local, Val.Null))

        val methodIndex = top.dyns.zipWithIndex.find(_._1 == signature).get._2

        // Load the type information pointer
        let(typeptr.name, Op.Load(Type.Ptr, obj))
        // Load the pointer of the table size
        let(methodCountPtr.name,
            Op.Elem(rtiType, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(0))))
        // Load the table size
        let(methodCount.name, Op.Load(Type.I32, methodCountPtr))
        throwIfCond(Op.Comp(Comp.Ieq, Type.I32, methodCount, Val.I32(0)))
        // If the size is greater than 0,
        // call the C function "scalanative_dyndispatch"
        let(dyndispatchTablePtr.name,
            Op.Elem(rtiType, typeptr, Seq(Val.I32(0), Val.I32(2), Val.I32(0))))
        let(methptrptr.name,
            Op.Call(dyndispatchSig,
                    dyndispatch,
                    Seq(dyndispatchTablePtr, Val.I32(methodIndex)),
                    Next.None))
        throwIfNull(methptrptr)
        let(n, Op.Load(Type.Ptr, methptrptr))

      case inst =>
        buf += inst
    }

    buf
  }
}

object DynmethodLowering extends PassCompanion {
  def apply(config: tools.Config, top: Top): Pass =
    new DynmethodLowering()(top.fresh, top)

  val dyndispatchName = Global.Top("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.I32), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    Global.Member(excptnGlobal, "init_class.java.lang.String")

  override val injects = Seq(
    Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig)
  )

  override def depends: Seq[Global] = Seq(excptnGlobal, excptnInitGlobal)
}
