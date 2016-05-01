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
    case defn: Defn.Trait =>
      Seq()

    // TODO: hoisting of trait methods
    // case _: Defn.Define => ???
  }

  override def preInst = {
    case Inst(n, Op.Method(sig, obj, VirtualTraitMethodRef(meth))) =>
      ???

    case Inst(n, Op.Method(sig, obj, StaticTraitMethodRef(meth))) =>
      ???
  }
}
