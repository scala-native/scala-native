package scala.scalanative
package linker

import scalanative.nir._

trait Extractor[T] {
  def unapply(ty: Type)(implicit linked: Result): Option[T] = ty match {
    case ty: Type.Named => unapply(ty.name)
    case Type.Array(ty) => unapply(Type.toArrayClass(ty))
    case _              => None
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
