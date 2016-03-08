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
 *          #Type_type,
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
class TraitLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh) extends Pass {
  private def itables(): Seq[Defn] =
    todo("trait table generation")

  override def preAssembly = { case defns =>
    defns ++ itables()
  }

  override def preDefn = {
    case Defn.Trait(_, name @ TraitRef(iface), _, members) =>
      val typeId    = Val.I32(iface.id)
      val typeName  = Val.String(name.parts.head)
      val typeVal   = Val.Struct(Nrt.Type.name, Seq(Nrt.Type_type, typeId, typeName))
      val typeConst = Defn.Const(Seq(), name + "const", Nrt.Type, typeVal)

      val methods: Seq[Defn.Define] = members.collect {
        case defn: Defn.Define =>
          todo("trait default methods")
      }

      typeConst +: methods
  }

  override def preInst =  {
    case Inst(n, Op.Method(sig, obj, VirtualTraitMethodRef(meth))) =>
      todo("trait method dispatch")

    case Inst(n, Op.Method(sig, obj, StaticTraitMethodRef(meth))) =>
      todo("trait default methods")

    case Inst(n, Op.As(TraitRef(iface), v)) =>
      Seq(
        Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(TraitRef(iface), obj)) =>
      todo("interface instance check")

    case Inst(n, Op.TypeOf(TraitRef(iface))) =>
      Seq(
        Inst(n, Op.Copy(Val.Global(iface.name + "const", Type.Ptr(Nrt.Type))))
      )
  }

  object TraitRef {
    def unapply(ty: Type): Option[ClassHierarchy.Trait] = ty match {
      case Type.Trait(name) => unapply(name)
      case _                => None
    }
    def unapply(name: Global): Option[ClassHierarchy.Trait] =
      chg.nodes.get(name).collect {
        case iface: ClassHierarchy.Trait => iface
      }
  }

  object VirtualTraitMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] =
      chg.nodes.get(name).collect {
        case meth: ClassHierarchy.Method
          if meth.isVirtual
          && meth.in.isInstanceOf[ClassHierarchy.Trait] => meth
      }
  }

  object StaticTraitMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] =
      chg.nodes.get(name).collect {
        case meth: ClassHierarchy.Method
          if meth.isStatic
          && meth.in.isInstanceOf[ClassHierarchy.Trait] => meth
      }
  }
}
