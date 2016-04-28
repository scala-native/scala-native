package scala.scalanative
package compiler
package analysis

import ClassHierarchy._
import nir._

object ClassHierarchyExtractors {
  object ClassRef {
    def unapply(ty: Type)(implicit chg: Graph): Option[Class] =
      ty match {
        case Type.Class(name) => unapply(name)
        case _                => None
      }

    def unapply(name: Global)(implicit chg: Graph): Option[Class] =
      chg.nodes.get(name).collect {
        case cls: Class => cls
      }
  }

  object VirtualClassMethodRef {
    def unapply(name: Global)(implicit chg: Graph): Option[Method] =
      chg.nodes.get(name).collect {
        case meth: Method if meth.isVirtual && meth.in.isInstanceOf[Method] =>
          meth
      }
  }

  object StaticClassMethodRef {
    def unapply(name: Global)(implicit chg: Graph): Option[Method] =
      chg.nodes.get(name).collect {
        case meth: Method if meth.isStatic && meth.in.isInstanceOf[Class] =>
          meth
      }
  }

  object ClassFieldRef {
    def unapply(name: Global)(implicit chg: Graph): Option[Field] =
      chg.nodes.get(name).collect {
        case fld: Field => fld
      }
  }

  object ExternalRef {
    def unapply(name: Global)(implicit chg: Graph): Option[Node] =
      chg.nodes.get(name).collect {
        case node if node.attrs.exists(_ == Attr.External) =>
          node
      }
  }
}
