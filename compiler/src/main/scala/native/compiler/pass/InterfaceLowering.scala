package native
package compiler
package pass

import native.nir._
import native.util.unsupported
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
 *  Additionally a dispatch function is generated:
 *
 *      def __idispatch: (I32, I32) => ptr i8 { ... }
 *
 *  The function takes an class id and an interface method id and
 *  returns a dispatched method pointer or null if no match was found.
 *
 *  It's also used to check if an interface is implemented by a class.
 *  For this a zero is given as method id. Non-null result means that
 *  check is successful.
 */
class InterfaceLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh) extends Pass {
  private val idispatchName = Global("__idispatch")
  private val idispatchSig  = Type.Function(Seq(Type.I32, Type.I32), Type.Ptr(Type.I8))
  private val idispatchRef  = Val.Global(idispatchName, Type.Ptr(idispatchSig))

  private def idispatch(): Defn.Define = {
    val clsId  = Val.Local(fresh(), Type.I32)
    val methId = Val.Local(fresh(), Type.I32)

    Defn.Define(Seq(), idispatchName, idispatchSig,
      Seq(Block(fresh(), Seq(clsId, methId), Seq(), Cf.Unreachable)))
  }

  override def preAssembly = { case defns =>
    defns :+ idispatch()
  }

  override def preDefn = {
    case Defn.Interface(_, name @ InterfaceRef(iface), _, members) =>
      val typeId    = Val.I32(iface.id)
      val typeName  = Val.String(name.parts.head)
      val typeVal   = Val.Struct(Intr.type_.name, Seq(Intr.type_type, typeId, typeName))
      val typeConst = Defn.Const(Seq(), name + "const", Intr.type_, typeVal)

      val methods: Seq[Defn.Define] = members.collect {
        case defn: Defn.Define =>
          unsupported("interface default methods")
      }

      typeConst +: methods
  }

  override def preInst =  {
    case Inst(n, Op.Method(sig, obj, VirtualInterfaceMethodRef(meth))) =>
      val ty  = Val.Local(fresh(), Type.Ptr(Intr.type_))
      val id  = Val.Local(fresh(), Type.I32)
      val res = Val.Local(fresh(), Type.Ptr(Type.I8))

      Seq(
        Inst(ty.name,  Intr.call(Intr.object_getType, obj)),
        Inst(id.name,  Intr.call(Intr.type_getId, ty)),
        Inst(res.name, Op.Call(idispatchSig, idispatchRef, Seq(id, Val.I32(meth.id)))),
        Inst(n,        Op.Conv(Conv.Bitcast, Type.Ptr(meth.ty), res))
      )

    case Inst(n, Op.Method(sig, obj, StaticInterfaceMethodRef(meth))) =>
      unsupported("interface default methods")

    case Inst(n, Op.As(InterfaceRef(iface), v)) =>
      Seq(
        Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(InterfaceRef(iface), obj)) =>
      val ty  = Val.Local(fresh(), Type.Ptr(Intr.type_))
      val id  = Val.Local(fresh(), Type.I32)
      val res = Val.Local(fresh(), Type.Ptr(Type.I8))

      Seq(
        Inst(ty.name,  Intr.call(Intr.object_getType, obj)),
        Inst(id.name,  Intr.call(Intr.type_getId, ty)),
        Inst(res.name, Op.Call(idispatchSig, idispatchRef, Seq(id, Val.I32(0)))),
        Inst(n,        Op.Comp(Comp.Ine, Type.Ptr(Type.I8), res, Val.Zero(Type.Ptr(Type.I8))))
      )

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
