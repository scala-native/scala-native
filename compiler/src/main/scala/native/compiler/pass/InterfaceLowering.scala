package native
package compiler
package pass

import native.nir._
import native.util.todo
import native.compiler.analysis.ClassHierarchy

/** Lowers interfaces and operations on them.
 *
 *  For example an interface:
 *
 *      interface $name: .. $ifaces {
 *        .. def $declname: $declty
 *        .. def $defnname: $defnty = $body
 *      }
 *
 *  Gets lowered to:
 *
 *      const $name_const: struct #type =
 *        struct #type {
 *          #type_type,
 *          ${iface.name},
 *          ${iface.id}
 *        }
 *
 *      .. def $defnname: $defnty = $body
 *
 *  Additionally two dispatch tables are generated:
 *
 *      const __iface_instance: [[bool x C] x I] = ...
 *      const __iface_dispatch: [[ptr i8 x C] x M] = ...
 *
 *  Tables are indexed by either class id (where C is total number of classes),
 *  method id (where M is total number of inteface methods) or inteface id
 *  (where I is total number of interfaces).
 *
 *  In the future we'd probably compact this arrays with one of the
 *  well-known compression techniques like row displacement tables.
 */
class InterfaceLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh) extends Pass {
  private def itables(): Seq[Defn] =
    todo("interface table generation")

  override def preAssembly = { case defns =>
    defns ++ itables()
  }

  override def preDefn = {
    case Defn.Interface(_, name @ InterfaceRef(iface), _, members) =>
      val typeId    = Val.I32(iface.id)
      val typeName  = Val.String(name.parts.head)
      val typeVal   = Val.Struct(Intr.type_.name, Seq(Intr.type_type, typeId, typeName))
      val typeConst = Defn.Const(Seq(), name + "const", Intr.type_, typeVal)

      val methods: Seq[Defn.Define] = members.collect {
        case defn: Defn.Define =>
          todo("interface default methods")
      }

      typeConst +: methods
  }

  override def preInst =  {
    case Inst(n, Op.Method(sig, obj, VirtualInterfaceMethodRef(meth))) =>
      todo("inteface method dispatch")

    case Inst(n, Op.Method(sig, obj, StaticInterfaceMethodRef(meth))) =>
      todo("interface default methods")

    case Inst(n, Op.As(InterfaceRef(iface), v)) =>
      Seq(
        Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(InterfaceRef(iface), obj)) =>
      todo("interface instance check")

    case Inst(n, Op.TypeOf(InterfaceRef(iface))) =>
      Seq(
        Inst(n, Op.Copy(Val.Global(iface.name + "const", Type.Ptr(Intr.type_))))
      )
  }

  object InterfaceRef {
    def unapply(ty: Type): Option[ClassHierarchy.Interface] = ty match {
      case Type.InterfaceClass(name) => unapply(name)
      case _                         => None
    }
    def unapply(name: Global): Option[ClassHierarchy.Interface] =
      chg.nodes.get(name).collect {
        case iface: ClassHierarchy.Interface => iface
      }
  }

  object VirtualInterfaceMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] =
      chg.nodes.get(name).collect {
        case meth: ClassHierarchy.Method
          if meth.isVirtual
          && meth.in.isInstanceOf[ClassHierarchy.Interface] => meth
      }
  }

  object StaticInterfaceMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] =
      chg.nodes.get(name).collect {
        case meth: ClassHierarchy.Method
          if meth.isStatic
          && meth.in.isInstanceOf[ClassHierarchy.Interface] => meth
      }
  }
}
