package salty.tools.compiler.reductions

import salty.ir._, Reduction._

/** Lowers classes, methods and fields down to
 *  structs with accompanying vtables.
 *
 *  TODO: interfaces
 *  TODO: value dependencies on classes
 *
 *  For example a class w:
 *
 *      class $name: $parent
 *      .. method $name::$mname(%this: $name, .. %pname: $pty): $retty = $end
 *      .. field $name::$fname : $fty
 *
 *  Gets lowered to:
 *
 *      struct $name.vtable = { $parent.vtable, .. $retty(.. $pty)* }
 *      struct $name.data = { $parent.data, .. $fty }
 *      struct $name = { $name.vtable*, $name.data* }
 *
 *      .. define $name::$mname(%this: $name.ref, .. %pname: $ptype): $ret = $end
 *      constant $name.vtable.constant: $name.vtable = { .. $name::$mname }
 *
 *  Usages are rewritten as following:
 *
 *  * Type usages are lowered to $name.ref
 *
 *  * Allocations
 *
 *        class-alloc $name
 *
 *    Lowered to:
 *
 *        { $name.vtable.data, alloc $name.data }
 *
 *  * Method elems
 *
 *        method-elem %instance, $method
 *
 *    Lowered to:
 *
 *        %meth_** = elem %vtable, 0, 0, %{vtable.indexOf(method)}
 *        %meth_* = load %meth_**
 *        %meth_*
 *
 *  * Field elems
 *
 *        field-elem %instance, $field
 *
 *    Lowered to:
 *
 *        %field_* = elem %instance, 1, 0, ${data.indexOf(field)}
 *        %field_*
 */
object ClassLowering extends Reduction {
  def dataAttr(node: Node): Option[ClassData] =
    node.attrs.collectFirst { case dm: ClassData => dm }
  def vtableAttr(node: Node): Option[ClassVtable] =
    node.attrs.collectFirst { case vt: ClassVtable => vt }

  def reduce = {
    case meth @ Defn.Method(retty, params, cf, cls) =>
      After(cls) { _ =>
        Replace.all(Defn.Define(retty, params.nodes, cf, meth.name))
      }

    case cls @ Defn.Class(parent, _) =>
      After(parent) { parent =>
        val parentData = dataAttr(parent).map(_.data)
        val parentVtable = vtableAttr(parent).map(_.vtable)
        val methods = cls.uses.collect {
          case Use(meth @ Defn.Method(_, _, _, _)) => meth
        }
        val vtableIndex = methods.zipWithIndex.toMap
        val vtable =
          Defn.Struct(
            parentVtable ++: methods.map {
              case Defn.Method(retty, params, _, _) =>
                Defn.Function(retty, params.nodes.map {
                  case Param(Slot(ty)) => ty
                })
            },
            Name.Vtable(cls.name))
        val vtableConstant =
          Defn.Constant(
            vtable,
            Struct(Seq()), // TODO:
            Name.VtableConstant(cls.name))
        val fields = cls.uses.collect {
          case Use(field @ Defn.Field(_, _)) => field
        }
        val dataIndex = fields.zipWithIndex.toMap
        val data =
          Defn.Struct(
            parentData ++: cls.uses.collect {
              case Slot(Defn.Field(Slot(ty), _)) => ty
            },
            Name.ClassData(cls.name))
        val ref =
          Defn.Struct(
            Seq(Defn.Ptr(vtable), Defn.Ptr(data)),
            cls.name,
            ClassData(data, dataIndex),
            ClassVtable(vtable, vtableConstant, vtableIndex))

        Replace.all(ref)
      }

    case ClassAlloc(cls) =>
      After(cls) { cls =>
        val data       = dataAttr(cls).get.data
        val vtableData = vtableAttr(cls).get.vtableConstant

        Replace.all(Struct(Seq(vtableData, Alloc(data))))
      }

    case MethodElem(ef, instance, Slot(meth @ Defn.Method(_, _, _, cls))) =>
      After(cls) { cls =>
        val methindex = vtableAttr(cls).get.index
        val meth_** = Elem(instance, Seq(I32(0), I32(0), I32(methindex(meth))))
        val meth_* = Load(Empty, meth_**)

        Replace {
          case s if s.schema == Schema.Ef => ef
          case _                          => meth_*
        }
      }

    case FieldElem(ef, instance, Slot(field @ Defn.Field(_, cls))) =>
      After(cls) { cls =>
        val fieldindex = dataAttr(cls).get.index
        val field_* = Elem(instance, Seq(I32(1), I32(0), I32(fieldindex(field))))

        Replace {
          case s if s.schema == Schema.Ef => ef
          case _                          => field_*
        }
      }
  }
}
