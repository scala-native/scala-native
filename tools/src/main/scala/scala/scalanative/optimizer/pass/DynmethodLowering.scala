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
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case Inst.Let(n, dyn @ Op.Dynmethod(obj, signature)) =>
        def throwInstrs(): Unit = {
          val exc = let(Op.Classalloc(excptnGlobal))
          let(
            Op.Call(excInitSig,
                    excInit,
                    Seq(exc, Val.String(signature)),
                    Next.None))
          raise(exc, Next.None)
        }

        def throwIfCond(cond: Op.Comp): Unit = {
          val labelIsNull, labelEndNull = Next(fresh())

          val condNull = let(cond)
          branch(condNull, labelIsNull, labelEndNull)
          label(labelIsNull.name)
          throwInstrs()
          label(labelEndNull.name)
        }

        def throwIfNull(local: Val.Local) =
          throwIfCond(Op.Comp(Comp.Ieq, Type.Ptr, local, Val.Null))

        val methodIndex =
          top.dyns.zipWithIndex.find(_._1 == signature).get._2

        // Load the type information pointer
        val typeptr = let(Op.Load(Type.Ptr, obj))
        // Load the pointer of the table size
        val methodCountPtr = let(
          Op.Elem(rtiType, typeptr, Seq(Val.Int(0), Val.Int(3), Val.Int(0))))
        // Load the table size
        val methodCount = let(Op.Load(Type.Int, methodCountPtr))
        throwIfCond(Op.Comp(Comp.Ieq, Type.Int, methodCount, Val.Int(0)))
        // If the size is greater than 0, call the dyndispatch runtime function
        val dyndispatchTablePtr = let(
          Op.Elem(rtiType, typeptr, Seq(Val.Int(0), Val.Int(3), Val.Int(0))))
        val methptrptr = let(
          Op.Call(dyndispatchSig,
                  dyndispatch,
                  Seq(dyndispatchTablePtr, Val.Int(methodIndex)),
                  Next.None))
        throwIfNull(methptrptr)
        let(n, Op.Load(Type.Ptr, methptrptr))

      case inst =>
        buf += inst
    }

    buf.toSeq
  }
}

object DynmethodLowering extends PassCompanion {
  def apply(config: tools.Config, top: Top): Pass =
    new DynmethodLowering()(top.fresh, top)

  val rtiType = Type.Struct(
    Global.None,
    Seq(Type.Int,
        Type.Ptr,
        Type.Long,
        Type.Struct(Global.None, Seq(Type.Int, Type.Ptr, Type.Ptr))))

  val dyndispatchName = Global.Top("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.Int), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    Global.Member(excptnGlobal, "init_class.java.lang.String")

  val excInitSig = Type.Function(
    Seq(Type.Class(excptnGlobal), Type.Class(Global.Top("java.lang.String"))),
    Type.Unit)
  val excInit = Val.Global(excptnInitGlobal, Type.Ptr)

  override val injects = Seq(
    Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig)
  )

  override def depends: Seq[Global] = Seq(excptnGlobal, excptnInitGlobal)
}
