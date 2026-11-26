package scala.scalanative
package linker

import scala.collection.mutable

import scalanative.util.unreachable

/** Our subtyping can be described by a following diagram:
 *
 *  {{{
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
 *  }}}
 *
 *  Primitive and aggregate types don't participate in subtyping and they have
 *  to be explicitly boxed to become compatible with a reference type.
 *
 *  Reference types form a simple lattice with java.lang.Object at the top and
 *  null type at the bottom. Subtyping between traits and classes is based on
 *  linearization of the all transitive parents, similarly to scalac.
 *
 *  Nothing is the common bottom type between reference and value types. It
 *  represents computations that may never complete normally (either loops
 *  forever or throws an exception).
 */
object Sub {

  def is(l: nir.Type, r: nir.Type)(implicit
      analysis: ReachabilityAnalysis.Result
  ): Boolean = {
    (l, r) match {
      case (l, r) if l == r =>
        true
      case (nir.Type.NullType(_), (nir.Type.Ptr | _: nir.Type.RefKind)) =>
        true
      case (
            nir.Type.NothingType(_),
            (_: nir.Type.ValueKind | _: nir.Type.RefKind)
          ) =>
        true
      case (_: nir.Type.RefKind, nir.Rt.Object) =>
        true
      // A linker class of an array names the array type
      case (_, nir.Type.Ref(name, _, nullable))
          if nir.Type.isSyntheticArrayClass(name) =>
        is(
          l,
          nir.Type
            .classOfType(r)
            .asInstanceOf[nir.Type.Array]
            .copy(nullable = nullable)
        )
      case (nir.Type.Ref(name, _, nullable), _)
          if nir.Type.isSyntheticArrayClass(name) =>
        is(
          nir.Type
            .classOfType(l)
            .asInstanceOf[nir.Type.Array]
            .copy(nullable = nullable),
          r
        )
      case (ScopeRef(linfo), ScopeRef(rinfo)) =>
        linfo.is(rinfo)
      // FIXME: Workaround for method/return types not being defined as scala.runtime.Nothing$|Null$
      case (nir.Type.NothingType(_), nir.Type.NothingType(_)) => true
      case (nir.Type.NullType(_), nir.Type.NullType(_))       => true
      case _                                                  => false
    }
  }

  /** Tests against `ty` use `isArray`, see
   *  `SemanticsConfig.strictArrayTypeChecks`.
   */
  def isStrictArrayTest(ty: nir.Type, config: build.NativeConfig): Boolean =
    ty match {
      case nir.Type.Array(elem, _) =>
        config.semanticsConfig.strictArrayTypeChecks &&
          nir.Type.hasSyntheticArrayClass(elem)
      case _ => false
    }

  /** Exact array subtyping, covariant for references (JLS 4.10.3). `Sub.is`
   *  stays lenient for the NIR checker.
   */
  def isArray(l: nir.Type, r: nir.Type.Array)(implicit
      analysis: ReachabilityAnalysis.Result
  ): Boolean = {
    val relem = nir.Type.canonicalArrayElement(r.ty)
    l match {
      case nir.Type.NullType(_) | nir.Type.NothingType(_) =>
        true
      case nir.Type.Array(lelem, _) =>
        isArrayElement(nir.Type.canonicalArrayElement(lelem), relem)
      case nir.Type.Ref(name, exact, _) =>
        nir.Type.fromArrayClass(name) match {
          // Any array of references
          case Some(lelem) if exact || name != nir.Rt.ObjectArray.name =>
            name != nir.Rt.BlobArray.name && isArrayElement(lelem, relem)
          case Some(_) => relem == nir.Rt.Object
          case None    => false
        }
      case _ =>
        false
    }
  }

  private def isArrayElement(l: nir.Type, r: nir.Type)(implicit
      analysis: ReachabilityAnalysis.Result
  ): Boolean = (l, r) match {
    case _ if l == r                                          => true
    case (_: nir.Type.RefKind, nir.Rt.Object)                 => true
    case (nir.Type.Array(lelem, _), nir.Type.Array(relem, _)) =>
      isArrayElement(lelem, relem)
    // Arrays reach Cloneable and Serializable through their array class
    case (ScopeRef(linfo), ScopeRef(rinfo))
        if !r.isInstanceOf[nir.Type.Array] =>
      linfo.is(rinfo)
    case _ => false
  }

  def is(info: ScopeInfo, ty: nir.Type.RefKind)(implicit
      analysis: ReachabilityAnalysis.Result
  ): Boolean = {
    ty match {
      case ScopeRef(other) =>
        info.is(other)
      case _ =>
        util.unreachable
    }
  }

  def lub(tys: Seq[nir.Type], bound: Option[nir.Type])(implicit
      analysis: ReachabilityAnalysis.Result
  ): nir.Type = {
    tys match {
      case Seq() =>
        unreachable
      case head +: tail =>
        tail.foldLeft[nir.Type](head)(lub(_, _, bound))
    }
  }

  def lub(lty: nir.Type, rty: nir.Type, bound: Option[nir.Type])(implicit
      analysis: ReachabilityAnalysis.Result
  ): nir.Type = {
    (lty, rty) match {
      case _ if lty == rty =>
        lty
      case (ty, nir.Type.NothingType(_)) =>
        ty
      case (nir.Type.NothingType(_), ty) =>
        ty
      case (nir.Type.Ptr, nir.Type.NullType(_)) =>
        nir.Type.Ptr
      case (nir.Type.NullType(_), nir.Type.Ptr) =>
        nir.Type.Ptr
      case (refty: nir.Type.RefKind, nir.Type.NullType(_)) =>
        nir.Type.Ref(refty.className, refty.isExact, nullable = true)
      case (nir.Type.NullType(_), refty: nir.Type.RefKind) =>
        nir.Type.Ref(refty.className, refty.isExact, nullable = true)
      case (lty: nir.Type.RefKind, rty: nir.Type.RefKind) =>
        val ScopeRef(linfo) = lty: @unchecked
        val ScopeRef(rinfo) = rty: @unchecked
        val binfo = bound.flatMap(ScopeRef.unapply)
        val lubinfo = lub(linfo, rinfo, binfo)
        val exact =
          lubinfo.name == rinfo.name && rty.isExact &&
            lubinfo.name == linfo.name && lty.isExact
        val nullable =
          lty.isNullable || rty.isNullable
        nir.Type.Ref(lubinfo.name, exact, nullable)
      case _ =>
        util.unsupported(s"lub(${lty.show}, ${rty.show})")
    }
  }

  def lub(linfo: ScopeInfo, rinfo: ScopeInfo, boundInfo: Option[ScopeInfo])(
      implicit analysis: ReachabilityAnalysis.Result
  ): ScopeInfo = {
    if (linfo == rinfo) {
      linfo
    } else if (linfo.is(rinfo)) {
      rinfo
    } else if (rinfo.is(linfo)) {
      linfo
    } else {
      // If bound is not a type of linfo or rinfo
      // it should be ignored. Otherwise java.lang.Object
      // would be returned, which may not be correct
      val correctedBoundInfo = boundInfo.filterNot { bound =>
        (!linfo.is(bound) || !rinfo.is(bound))
      }

      val candidates =
        linfo.linearized.filter { i =>
          rinfo.is(i) && correctedBoundInfo.forall(i.is)
        }

      candidates match {
        case Seq() =>
          analysis.infos(nir.Rt.Object.name).asInstanceOf[ScopeInfo]
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
            analysis.infos(nir.Rt.Object.name).asInstanceOf[ScopeInfo]
          }
      }
    }
  }
}
