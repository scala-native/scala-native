package scala.scalanative
package compiler
package analysis

import ClassHierarchy._
import nir._

object ClassHierarchyExtractors {
  trait Extractor[N <: Node] {
    def unapply(ty: Type)(implicit chg: Graph): Option[N] = ty match {
      case ty: Type.Named => unapply(ty.name)
      case _              => None
    }
    def unapply(name: Global)(implicit chg: Graph): Option[N]
  }

  object Ref extends Extractor[Node] {
    def unapply(name: Global)(implicit chg: Graph): Option[Node] =
      chg.nodes.get(name)
  }

  object StructRef extends Extractor[Struct] {
    def unapply(name: Global)(implicit chg: Graph): Option[Struct] =
      chg.nodes.get(name).collect {
        case node: Struct => node
      }
  }

  object ClassRef extends Extractor[Class] {
    def unapply(name: Global)(implicit chg: Graph): Option[Class] =
      chg.nodes.get(name).collect {
        case node: Class => node
      }
  }

  object TraitRef extends Extractor[Trait] {
    def unapply(name: Global)(implicit chg: Graph): Option[Trait] =
      chg.nodes.get(name).collect {
        case node: Trait => node
      }
  }

  object MethodRef extends Extractor[Method] {
    def unapply(name: Global)(implicit chg: Graph): Option[Method] =
      chg.nodes.get(name).collect {
        case node: Method => node
      }
  }

  object FieldRef extends Extractor[Field] {
    def unapply(name: Global)(implicit chg: Graph): Option[Field] =
      chg.nodes.get(name).collect {
        case node: Field => node
      }
  }
}
