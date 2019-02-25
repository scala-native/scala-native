package scala.scalanative
package linker

import scalanative.nir._

trait Extractor[T] {
  def unapply(ty: Type)(implicit linked: Result): Option[T] = ty match {
    case ty: Type.RefKind =>
      unapply(ty.className)
    case _ =>
      None
  }
  def unapply(name: Global)(implicit linked: Result): Option[T]
}

object Ref extends Extractor[Info] {
  def unapply(name: Global)(implicit linked: Result): Option[Info] =
    linked.infos.get(name)
}

object ScopeRef extends Extractor[ScopeInfo] {
  def unapply(name: Global)(implicit linked: Result): Option[ScopeInfo] =
    linked.infos.get(name).collect {
      case node: ScopeInfo => node
    }
}

object ClassRef extends Extractor[Class] {
  def unapply(name: Global)(implicit linked: Result): Option[Class] =
    linked.infos.get(name).collect {
      case node: Class => node
    }
}

object TraitRef extends Extractor[Trait] {
  def unapply(name: Global)(implicit linked: Result): Option[Trait] =
    linked.infos.get(name).collect {
      case node: Trait => node
    }
}

object MethodRef extends Extractor[(Info, Method)] {
  def unapply(name: Global)(implicit linked: Result): Option[(Info, Method)] =
    linked.infos.get(name).collect {
      case node: Method => (node.owner, node)
    }
}

object FieldRef extends Extractor[(Info, Field)] {
  def unapply(name: Global)(implicit linked: Result): Option[(Info, Field)] =
    linked.infos.get(name).collect {
      case node: Field => (node.owner, node)
    }
}

object ArrayRef {
  def unapply(ty: Type): Option[(Type, Boolean)] = ty match {
    case Type.Array(ty, nullable) =>
      Some((ty, nullable))
    case Type.Ref(name, _, nullable) =>
      Type.fromArrayClass(name).map(ty => (ty, nullable))
    case _ =>
      None
  }
}

object ExactClassRef {
  def unapply(ty: Type)(implicit linked: Result): Option[(Class, Boolean)] =
    ty match {
      case Type.Ref(ClassRef(cls), exact, nullable)
          if exact || cls.subclasses.isEmpty =>
        Some((cls, nullable))
      case UnitRef(nullable) =>
        Some((linked.infos(Rt.BoxedUnit.name).asInstanceOf[Class], nullable))
      case Type.Array(ty, nullable) =>
        Some(
          (linked.infos(Type.toArrayClass(ty)).asInstanceOf[Class], nullable))
      case _ =>
        None
    }
}

object UnitRef {
  def unapply(ty: Type): Option[Boolean] = ty match {
    case Type.Unit =>
      Some(false)
    case Type.Ref(name, _, nullable)
        if name == Rt.BoxedUnit.name
          || name == Rt.BoxedUnitModule.name =>
      Some(nullable)
    case _ =>
      None
  }
}

object BoxRef {
  def unapply(ty: Type): Option[(Type, Boolean)] = ty match {
    case Type.Ref(name, _, nullable) =>
      Type.unbox.get(Type.Ref(name)).map(ty => (ty, nullable))
    case _ =>
      None
  }
}
