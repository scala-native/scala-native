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
 *  Primitiva value and aggregate types don't participate in
 *  subtyping and they have to be explicitly boxed to become
 *  compatible with a reference type.
 *
 *  Reference types form a natural lattice with java.lang.Object
 *  at the top and null type at the bottom.
 *
 *  Nothing is the common bottom type between reference and value
 *  types. It represents computations that may never complete
 *  normally (either loops forever or throws an exception).
 */
object Sub {

  def is(l: Type, r: Type)(implicit linked: linker.Result): Boolean =
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

  def is(info: ScopeInfo, ty: Type.RefKind)(
      implicit linked: linker.Result): Boolean =
    ty match {
      case ScopeRef(other) =>
        info.is(other)
      case _ =>
        util.unreachable
    }

  def lub(tys: Seq[Type])(implicit linked: linker.Result): Type =
    tys match {
      case Seq() =>
        unreachable
      case head +: tail =>
        tail.foldLeft[Type](head)(lub)
    }

  def lub(lty: Type, rty: Type)(implicit linked: linker.Result): Type =
    (lty, rty) match {
      case _ if lty == rty =>
        lty
      case (lty @ (_: Type.RefKind | Type.Ptr), Type.Null) =>
        lty
      case (Type.Null, rty @ (_: Type.RefKind | Type.Ptr)) =>
        rty
      case (ty, Type.Nothing) =>
        ty
      case (Type.Nothing, ty) =>
        ty
      case (ScopeRef(linfo), ScopeRef(rinfo)) =>
        Type.Ref(lub(linfo, rinfo).name)
      case _ =>
        util.unsupported(s"lub(${lty.show}, ${rty.show})")
    }

  def lub(linfo: ScopeInfo, rinfo: ScopeInfo)(
      implicit linked: linker.Result): ScopeInfo =
    if (linfo == rinfo) {
      linfo
    } else if (linfo.is(rinfo)) {
      rinfo
    } else if (rinfo.is(linfo)) {
      linfo
    } else {
      val candidates = linearize(linfo).filter(i => rinfo.is(i))

      candidates match {
        case Seq() =>
          linked.infos(Rt.Object.name).asInstanceOf[ScopeInfo]
        case Seq(cand) =>
          cand
        case _ =>
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

  def inhabitants(info: ScopeInfo): Int = info match {
    case info: Class =>
      1 + info.subclasses.size
    case info: Trait =>
      info.implementors.size
  }

  def linearize(info: ScopeInfo)(
      implicit linked: linker.Result): Seq[ScopeInfo] = {
    val out = mutable.UnrolledBuffer.empty[ScopeInfo]

    def loop(info: ScopeInfo): Unit = info match {
      case info: Class =>
        out += info
        info.traits.reverse.foreach(loop)
        info.parent.foreach(loop)
      case info: Trait =>
        out += info
        info.traits.reverse.foreach(loop)
    }

    def overwrite(l: Seq[ScopeInfo]): Seq[ScopeInfo] = {
      val indexes = mutable.Map.empty[ScopeInfo, Int]
      l.zipWithIndex.foreach {
        case (v, idx) =>
          indexes(v) = idx
      }
      l.zipWithIndex.collect {
        case (v, idx) if indexes(v) == idx =>
          v
      }
    }

    loop(info)
    overwrite(out)
  }
}
