package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Lowers traits and operations on them. */
class TraitLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  import TraitLowering._

  private val dispatchVal = Val.Global(dispatchName, Type.Ptr)
  private val dispatchTy =
    Type.Array(Type.Array(Type.Ptr, chg.traits.length), chg.classes.length)
  private val dispatchDefn = {
    val table = Val.Array(
        Type.Array(Type.Ptr, chg.traits.length),
        (1 to chg.classes.length).map { _ =>
          Val.Array(Type.Ptr, (1 to chg.traits.length).map(_ => Val.Null))
        })

    Defn.Const(Attrs.None, dispatchName, dispatchTy, table)
  }

  private val instanceVal = Val.Global(instanceName, Type.Ptr)
  private val instanceTy =
    Type.Array(Type.Array(Type.Bool, chg.traits.length), chg.classes.length)
  private val instanceDefn = {
    val columns = chg.classes.sortBy(_.id).map { cls =>
      val row = new Array[Boolean](chg.traits.length)
      cls.alltraits.foreach { trt =>
        row(trt.id) = true
      }
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, chg.traits.length), columns)

    Defn.Const(Attrs.None, instanceName, instanceTy, table)
  }

  override def preAssembly = {
    case assembly =>
      assembly :+ dispatchDefn :+ instanceDefn
  }

  override def preDefn = {
    case _: Defn.Trait =>
      Seq()

    case Defn.Declare(_, MethodRef(Some(TraitRef(_)), _), _) =>
      Seq()
  }

  override def preInst = {
    case Inst(n, Op.Method(sig, obj, MethodRef(Some(TraitRef(trt)), meth))) =>
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val idptr      = Val.Local(fresh(), Type.Ptr)
      val id         = Val.Local(fresh(), Type.I32)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr)),
          Inst(methptrptr.name,
               Op.Elem(dispatchTy,
                       dispatchVal,
                       Seq(Val.I32(0), id, Val.I32(trt.id)))),
          Inst(n, Op.Load(Type.Ptr, methptrptr))
      )

    case inst @ Inst(n, Op.Is(TraitRef(trt), obj)) =>
      val typeptr = Val.Local(fresh(), Type.Ptr)
      val idptr   = Val.Local(fresh(), Type.Ptr)
      val id      = Val.Local(fresh(), Type.I32)
      val boolptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr)),
          Inst(boolptr.name,
               Op.Elem(instanceTy,
                       instanceVal,
                       Seq(Val.I32(0), id, Val.I32(trt.id)))),
          Inst(n, Op.Load(Type.Bool, boolptr))
      )
  }
}

object TraitLowering extends PassCompanion {
  def apply(ctx: Ctx) = new TraitLowering()(ctx.chg, ctx.fresh)

  val dispatchName = Global.Top("__dispatch")
  val instanceName = Global.Top("__instance")
}
