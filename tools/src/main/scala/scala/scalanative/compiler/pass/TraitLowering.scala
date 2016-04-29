package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Lowers traits and operations on them.
  *
  * For example an trait:
  *
  *     trait $name: .. $traits {
  *       .. def $declname: $declty
  *       .. def $defnname: $defnty = $body
  *     }
  *
  * Gets lowered to:
  *
  *     const type.$name: struct #type =
  *       struct #type {
  *         ${trt.name},
  *         ${trt.id}
  *       }
  *
  *     .. def $defnname: $defnty = $body
  *
  * Additionally a dispatch table are generated:
  *
  *     const __trait_dispatch: [[ptr i8 x C] x T] = ...
  *
  * This table lets one find a trait vtable for given class.
  * Dispatch table is indexed by a pair of class id and a trait id
  * (where C is total number of classes and T is total number of
  * traits in the current compilation assembly.)
  *
  * In the future we'd probably compact this array with one of the
  * well-known compression techniques like row displacement tables.
  */
class TraitLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  private def traitDispatch(): Seq[Defn] = Seq()

  override def preAssembly = {
    case defns =>
      defns ++ traitDispatch()
  }

  override def preDefn = {
    case Defn.Trait(_, name @ TraitRef(trt), _) =>
      val typeId    = Val.I32(trt.id)
      val typeName  = Val.String(name.id)
      val typeVal   = Val.Struct(Rt.Type.name, Seq(typeId, typeName))
      val typeConst = Defn.Const(Seq(), name tag "const", Rt.Type, typeVal)

      Seq(typeConst)

    // TODO: hoisting of trait methods
    // case _: Defn.Define => ???
  }

  override def preInst = {
    case Inst(n, Op.Method(sig, obj, VirtualTraitMethodRef(meth))) =>
      ???

    case Inst(n, Op.Method(sig, obj, StaticTraitMethodRef(meth))) =>
      ???

    case Inst(n, Op.As(TraitRef(trt), v)) =>
      Seq(
          Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(TraitRef(trt), obj)) =>
      ???

    case Inst(n, Op.TypeOf(TraitRef(trt))) =>
      Seq(
          Inst(n, Op.Copy(Val.Global(trt.name tag "type", Type.Ptr)))
      )
  }
}
