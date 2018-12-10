package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.util.unreachable

object Sema {

  def is(l: Type, r: Type)(implicit linked: linker.Result): Boolean = {
    (l, r) match {
      case (l, r) if l == r =>
        true
      case (Type.Null, Type.Ptr) =>
        true
      case (Type.Null, _: Type.RefKind) =>
        true
      case (_: Type.RefKind, Rt.Object) =>
        true
      case (TraitRef(l), TraitRef(r)) =>
        l.is(r)
      case (ClassRef(cls), refty: Type.RefKind) =>
        is(cls, refty)
      case _ =>
        false
    }
  }

  def is(cls: Class, ty: Type.RefKind)(
      implicit linked: linker.Result): Boolean = {
    ty match {
      case ClassRef(othercls) =>
        cls.is(othercls)
      case TraitRef(trt) =>
        cls.is(trt)
      case _ =>
        util.unreachable
    }
  }

  def lub(tys: Seq[Type])(implicit linked: linker.Result): Type = tys match {
    case Seq() =>
      unreachable
    case head +: tail =>
      tail.foldLeft[Type](head)(lub)
  }

  def lub(lty: Type, rty: Type)(implicit linked: linker.Result): Type =
    (lty, rty) match {
      case _ if lty == rty =>
        lty
      case (ClassRef(lcls), ClassRef(rcls)) =>
        if (rcls.is(lcls)) {
          lcls.ty
        } else {
          val lparent =
            lcls.parent.getOrElse(
              linked.infos(Rt.Object.name).asInstanceOf[Class])
          lub(lparent.ty, rty)
        }
      case (ClassRef(cls), TraitRef(trt)) =>
        if (cls.is(trt)) {
          Type.Ref(trt.name)
        } else {
          Rt.Object
        }
      case (TraitRef(trt), ClassRef(cls)) =>
        lub(rty, lty)
      case (TraitRef(ltrt), TraitRef(rtrt)) =>
        if (ltrt.is(rtrt)) {
          Type.Ref(rtrt.name)
        } else if (rtrt.is(ltrt)) {
          Type.Ref(ltrt.name)
        } else {
          Rt.Object
        }
      case (lty @ (_: Type.RefKind | Type.Ptr), Type.Null) =>
        lty
      case (Type.Null, rty @ (_: Type.RefKind | Type.Ptr)) =>
        rty
      case (ty, Type.Nothing) =>
        ty
      case (Type.Nothing, ty) =>
        ty
      case (Type.Short, Type.Char) | (Type.Char, Type.Short) =>
        Type.Short
      case _ =>
        util.unsupported(s"lub(${lty.show}, ${rty.show})")
    }

  def glb(tys: (Type, Type))(implicit linked: linker.Result): Option[Type] =
    glb(tys._1, tys._2)

  def glb(lty: Type, rty: Type)(implicit linked: linker.Result): Option[Type] =
    (lty, rty) match {
      case _ if lty == rty =>
        Some(lty)
      case (ClassRef(lcls), ClassRef(rcls)) =>
        if (lcls.is(rcls)) {
          Some(lcls.ty)
        } else if (rcls.is(lcls)) {
          Some(rcls.ty)
        } else {
          None
        }
      case (ClassRef(cls), TraitRef(trt)) =>
        if (cls.is(trt)) {
          Some(cls.ty)
        } else if (cls.ty == Rt.Object) {
          Some(Type.Ref(trt.name))
        } else {
          None
        }
      case (TraitRef(trt), ClassRef(cls)) =>
        glb(rty, lty)
      case (TraitRef(ltrt), TraitRef(rtrt)) =>
        if (ltrt.is(rtrt)) {
          Some(Type.Ref(ltrt.name))
        } else if (rtrt.is(ltrt)) {
          Some(Type.Ref(rtrt.name))
        } else {
          None
        }
      case (_: Type.RefKind | Type.Ptr, Type.Null) |
          (Type.Null, _: Type.RefKind | Type.Ptr) =>
        Some(Type.Null)
      case (_, Type.Nothing) | (Type.Nothing, _) =>
        Some(Type.Nothing)
      case (Type.Short, Type.Char) | (Type.Char, Type.Short) =>
        Some(Type.Short)
      case _ =>
        util.unsupported(s"glb(${lty.show}, ${rty.show})")
    }

  def resolve(cls: Class, sig: Sig)(implicit linked: linker.Result): Global =
    cls.resolve(sig).get

  def targets(ty: Type, sig: Sig)(
      implicit linked: linker.Result): mutable.Set[Global] =
    ty match {
      case ExactClassRef(cls, _) =>
        val out = mutable.Set.empty[Global]
        out ++= cls.resolve(sig)
        out
      case ScopeRef(scope) =>
        scope.targets(sig)
      case _ =>
        mutable.Set.empty
    }
}
