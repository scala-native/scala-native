package native
package compiler
package pass

import native.nir._
import native.compiler.analysis.ClassHierarchy

/** Lowers interfaces and operations on them.
 */
class InterfaceLowering(implicit chg: ClassHierarchy.Graph) extends Pass {
  override def preDefn = {
    case _: Defn.Interface =>
      ???
  }

  override def preInst =  {
    case Inst(n, Op.Method(sig, obj, InterfaceMethodRef(meth))) =>
      ???

    case Inst(_, Op.Is(InterfaceRef(iface), _)) =>
      ???

    case Inst(_, Op.As(InterfaceRef(iface), _)) =>
      ???

    case Inst(n, Op.TypeOf(InterfaceRef(iface))) =>
      ???
  }

  object InterfaceRef {
    def unapply(ty: Type): Option[ClassHierarchy.Interface] = None
    def unapply(name: Global): Option[ClassHierarchy.Interface] = None
  }

  object InterfaceMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] = None
  }
}
