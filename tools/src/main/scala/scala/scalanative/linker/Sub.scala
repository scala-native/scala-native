package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.util.unreachable

/** Our subtyping can be described by a following diagram:
 *
 *    value kind        ref kind         special kind
 *    |     \           |    \
 *    |     |           |      \
 *    prim  aggr        class  trait
 *    |     |           |      /
 *    |     |           |    /
 *    |     |           null
 *    |     |           /
 *    |     |       /
 *    |     |   /
 *    nothing
 *
 *  Primitive and aggregate types don't participate in
 *  subtyping and they have to be explicitly boxed to become
 *  compatible with a reference type.
 *
 *  Reference types form a simple lattice with java.lang.Object
 *  at the top and null type at the bottom. Subtyping between
 *  traits and classes is based on linearization of the all
 *  transitive parents, similarly to scalac.
 *
 *  Nothing is the common bottom type between reference and value
 *  types. It represents computations that may never complete
 *  normally (either loops forever or throws an exception).
 */
object Sub {

  def is(l: Type, r: Type)(implicit linked: linker.Result): Boolean = {
    (l, r) match {
      case (l, r) if l == r =>
        true
      case (Type.Null, (Type.Ptr | _: Type.RefKind)) =>
        true
      case (Type.Nothing, (_: Type.ValueKind | _: Type.RefKind)) =>
        true
      case (_: Type.RefKind, Rt.Object) =>
        true
      case (ScopeRef(linfo), ScopeRef(rinfo)) =>
        linfo.is(rinfo)
      case _ =>
        false
    }
  }

  def is(info: ScopeInfo, ty: Type.RefKind)(
      implicit linked: linker.Result): Boolean = {
    ty match {
      case ScopeRef(other) =>
        info.is(other)
      case _ =>
        util.unreachable
    }
  }

  def lub(tys: Seq[Type])(implicit linked: linker.Result): Type = {
    tys match {
      case Seq() =>
        unreachable
      case head +: tail =>
        tail.foldLeft[Type](head)(lub)
    }
  }

  def lub(lty: Type, rty: Type)(implicit linked: linker.Result): Type = {
    (lty, rty) match {
      case _ if lty == rty =>
        lty
      case (ty, Type.Nothing) =>
        ty
      case (Type.Nothing, ty) =>
        ty
      case (Type.Ptr, Type.Null) =>
        Type.Ptr
      case (Type.Null, Type.Ptr) =>
        Type.Ptr
      case (refty: Type.RefKind, Type.Null) =>
        Type.Ref(refty.className, refty.isExact, nullable = true)
      case (Type.Null, refty: Type.RefKind) =>
        Type.Ref(refty.className, refty.isExact, nullable = true)
      case (lty: Type.RefKind, rty: Type.RefKind) =>
        val ScopeRef(linfo) = lty
        val ScopeRef(rinfo) = rty
        val lubinfo         = lub(linfo, rinfo)
        val exact =
          lubinfo.name == rinfo.name && rty.isExact &&
            lubinfo.name == linfo.name && lty.isExact
        val nullable =
          lty.isNullable || rty.isNullable
        Type.Ref(lub(linfo, rinfo).name, exact, nullable)
      case _ =>
        util.unsupported(s"lub(${lty.show}, ${rty.show})")
    }
  }

  def lub(linfo: ScopeInfo, rinfo: ScopeInfo)(
      implicit linked: linker.Result): ScopeInfo = {
    if (linfo == rinfo) {
      linfo
    } else if (linfo.is(rinfo)) {
      rinfo
    } else if (rinfo.is(linfo)) {
      linfo
    } else {
      val candidates = linfo.linearized.filter(i => rinfo.is(i))

      candidates match {
        case Seq() =>
          linked.infos(Rt.Object.name).asInstanceOf[ScopeInfo]
        case Seq(cand) =>
          cand
        case _ =>
          def inhabitants(info: ScopeInfo): Int =
            info.implementors.size

          val min = candidates.map(inhabitants).min

          val minimums = candidates.collect {
            case cand if inhabitants(cand) == min =>
              cand
          }

          minimums.headOption.getOrElse {
            linked.infos(Rt.Object.name).asInstanceOf[ScopeInfo]
          }
      }
    }
  }
}
