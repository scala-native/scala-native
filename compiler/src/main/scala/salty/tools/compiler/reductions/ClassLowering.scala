package salty.tools.compiler.reductions

import salty.ir._, Reduction._

/** Lowers classes, methods and fields down to
 *  structs with accompanying vtables.
 *
 *  TODO: interfaces
 *  TODO: value dependencies on classes
 *  TODO: NPE
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
  private final case class ClassData(data: Node,
                                     index: Map[Node, Int]) extends TransientAttr
  private final case class ClassVtable(vtable: Node, vtableConstant: Node, func: Map[Node, Node],
                                       index: Map[Node, Int]) extends TransientAttr

  private def dataAttr(node: Node): Option[ClassData] =
    node.attrs.collectFirst { case dm: ClassData => dm }
  private def vtableAttr(node: Node): Option[ClassVtable] =
    node.attrs.collectFirst { case vt: ClassVtable => vt }

  def reduce = {
    case cls @ Defn.Class.deps(parent, _) =>
      after(parent) {
        val parentData        = dataAttr(parent.dep).map(_.data)
        val parentVtableAttr  = vtableAttr(parent.dep)
        val parentVtable      = parentVtableAttr.map(_.vtable)
        val parentVtableValue = parentVtableAttr.map { _.vtableConstant match {
          case Defn.Constant(_, value) => value
        }}
        val fields = cls.uses.collect {
          case Use(field @ Defn.Field(_, _)) => field
        }.toSeq
        val methods = cls.uses.collect {
          case Use(meth @ Defn.Method(_, _, _, _)) => meth
        }.toSeq
        val funcs = methods.map {
          case meth @ Defn.Method(retty, params, end, _) =>
            meth -> Defn.Define(retty, params, end, meth.name)
        }.toMap
        val vtableIndex = methods.zipWithIndex.toMap
        val vtable =
          Defn.Struct(
            parentVtable ++: methods.map {
              case Defn.Method(retty, params, _, _) =>
                Defn.Function(retty, params.map {
                  case Param(ty) => ty
                })
            },
            Name.Vtable(cls.name))
        val vtableConstant =
          Defn.Constant(
            vtable,
            Lit.Struct(parentVtableValue ++: funcs.values.toSeq),
            Name.VtableConstant(cls.name))
        val dataIndex = fields.zipWithIndex.toMap
        val data =
          Defn.Struct(
            parentData ++: cls.uses.collect {
              case Use(Defn.Field(ty, _)) => ty
            }.toSeq,
            Name.ClassData(cls.name))
        val ref =
          Defn.Struct(
            Seq(Defn.Ptr(vtable), Defn.Ptr(data)),
            cls.name,
            ClassData(data, dataIndex),
            ClassVtable(vtable, vtableConstant, funcs, vtableIndex))

        replaceAll(ref)
      }

    case meth @ Defn.Method.deps(_, _, _, cls) =>
      after(cls) {
        replaceAll(vtableAttr(cls.dep).get.func(meth))
      }

    case ClassAlloc.deps(cls) =>
      after(cls) {
        val data       = dataAttr(cls.dep).get.data
        val vtableData = vtableAttr(cls.dep).get.vtableConstant

        replaceAll(Lit.Struct(Seq(vtableData, Alloc(data))))
      }

    case MethodElem(ef, instance, meth @ Defn.Method.deps(_, _, _, cls)) =>
      after(cls) {
        val methindex = vtableAttr(cls.dep).get.index
        val meth_** = Elem(instance, Seq(Lit.I32(0), Lit.I32(0), Lit.I32(methindex(meth))))
        val meth_* = Load(Empty, meth_**)

        replace {
          case u if u.isEf => ef
          case _           => meth_*
        }
      }

    case FieldElem(ef, instance, field @ Defn.Field.deps(_, cls)) =>
      after(cls) {
        val fieldindex = dataAttr(cls.dep).get.index
        val field_* = Elem(instance, Seq(Lit.I32(1), Lit.I32(0), Lit.I32(fieldindex(field))))

        replace {
          case u if u.isEf => ef
          case _           => field_*
        }
      }
  }
}
