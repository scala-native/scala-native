package scala.scalanative
package optimizer
package analysis

import ClassHierarchy._
import nir._

object ClassHierarchyExtractors {
  trait Extractor[T] {
    def unapply(ty: nir.Type)(implicit top: Top): Option[T] = ty match {
      case ty: Type.Named => unapply(ty.name)
      case _              => None
    }
    def unapply(name: Global)(implicit top: Top): Option[T]
  }

  object Ref extends Extractor[Node] {
    def unapply(name: Global)(implicit top: Top): Option[Node] =
      top.nodes.get(name)
  }

  object ScopeRef extends Extractor[Scope] {
    def unapply(name: Global)(implicit top: Top): Option[Scope] =
      top.nodes.get(name).collect {
        case node: Scope => node
      }
  }

  object StructRef extends Extractor[Struct] {
    def unapply(name: Global)(implicit top: Top): Option[Struct] =
      top.nodes.get(name).collect {
        case node: Struct => node
      }
  }

  object ClassRef extends Extractor[Class] {
    def unapply(name: Global)(implicit top: Top): Option[Class] =
      top.nodes.get(name).collect {
        case node: Class => node
      }
  }

  object TraitRef extends Extractor[Trait] {
    def unapply(name: Global)(implicit top: Top): Option[Trait] =
      top.nodes.get(name).collect {
        case node: Trait => node
      }
  }

  object MethodRef extends Extractor[(Scope, Method)] {
    def unapply(name: Global)(implicit top: Top): Option[(Scope, Method)] =
      top.nodes.get(name).collect {
        case node: Method => (node.in, node)
      }
  }

  object FieldRef extends Extractor[(Scope, Field)] {
    def unapply(name: Global)(implicit top: Top): Option[(Scope, Field)] =
      top.nodes.get(name).collect {
        case node: Field => (node.in, node)
      }
  }
}
