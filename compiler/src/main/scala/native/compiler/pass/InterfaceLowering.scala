package native
package compiler
package pass

import native.nir._
import native.compiler.analysis.ClassHierarchy

/** Lowers interfaces and operations on them.
 */
class InterfaceLowering extends Pass {
  override def preDefn = {
    case _: Defn.Interface =>
      ???
  }

  override def preInst =  {
    case Inst(n, Op.Method(sig, obj, InterfaceMethod(meth))) =>
      ???

    case Inst(_, Op.Is(_: Type.InterfaceClass, _)) =>
      ???

    case Inst(_, Op.As(_: Type.InterfaceClass, _)) =>
      ???

    case Inst(n, Op.TypeOf(_: Type.InterfaceClass)) =>
      ???
  }

  object InterfaceMethod {
    def unapply(name: Global): Option[ClassHierarchy.Method] = None
  }
}
