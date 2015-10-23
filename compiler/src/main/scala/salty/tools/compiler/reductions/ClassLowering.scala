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
 *      struct $name = { $name.vtable*, $name.data }
 *
 *      .. define $name::$mname(%this: i8*, .. %pname: $ptype): $ret = $end
 *      constant $name.vtable.constant: $name.vtable = { .. $name::$mname }
 *
 *  Usages are rewritten as following:
 *
 *  * Type usages become untyped pointers `i8*`
 *
 *  * Allocations
 *
 *        class-alloc $name
 *
 *    Lowered to:
 *
 *        %instance = alloc $name
 *        store (elem %instance 0, 0), $name.vtable.constant
 *        bitcast %instance to i8*
 *
 *  * Method elems
 *
 *        method-elem %instance, $method
 *
 *    Lowered to:
 *
 *        %typed    = bitcast %instance to $name*
 *        %vtable_* = load (elem %typed, 0, 0)
 *        %meth_**  = elem %vtable_*, 0, %{vtable.indexOf(method)}
 *        %meth_*   = load %meth_**
 *        %meth_*
 *
 *  * Field elems
 *
 *        field-elem %instance, $field
 *
 *    Lowered to:
 *
 *        %typed   = bitcast %instance to $name*
 *        %field_* = elem %typed, 0, ${fieldIndex + 1}
 *        %field_*
 */
object ClassLowering extends Reduction {
  private final case class ClassInfo(typed: Node, typedRef: Node,
                                     data: Node, dataIndex: Map[Node, Int],
                                     vtable: Node, vtableConstant: Node, func: Map[Node, Node],
                                     vtableIndex: Map[Node, Int]) extends TransientAttr

  private def info(node: Node): Option[ClassInfo] =
    node.attrs.collectFirst { case info: ClassInfo => info }

  def reduce = {
    case cls @ Defn.Class.deps(parent, _) =>
      after(parent) {
        val parentInfo          = info(parent.dep)
        val parentDataEntries   = parentInfo.toSeq.flatMap { info =>
          val Defn.Struct(defns) = info.data
          defns
        }
        val parentVtableEntries = parentInfo.toSeq.flatMap { info =>
          val Defn.Struct(defns) = info.vtable
          defns
        }
        val parentVtableValues = parentInfo.toSeq.flatMap { info =>
          val Defn.Constant(_, Lit.Struct(_, values)) = info.vtableConstant
          values
        }
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
            parentVtableEntries ++ methods.map {
              case Defn.Method(retty, params, _, _) =>
                Defn.Ptr(
                  Defn.Function(retty, params.map {
                    case Param(ty) => ty
                  })
                )
            },
            Name.Vtable(cls.name))
        val vtableConstant =
          Defn.Constant(
            vtable,
            Lit.Struct(vtable, parentVtableValues ++ funcs.values.toSeq),
            Name.VtableConstant(cls.name))
        val dataIndex = fields.zipWithIndex.toMap
        val data =
          Defn.Struct(
            parentDataEntries ++ cls.uses.collect {
              case Use(Defn.Field(ty, _)) => ty
            }.toSeq,
            Name.Data(cls.name))
        val typed =
          Defn.Struct(
            Seq(Defn.Ptr(vtable), data),
            cls.name)
        val ref =
          Defn.Ptr(Prim.I8,
            ClassInfo(typed, Defn.Ptr(typed),
                      data, dataIndex,
                      vtable, vtableConstant, funcs, vtableIndex))

        replaceAll(ref)
      }

    case meth @ Defn.Method.deps(_, _, _, cls) =>
      after(cls) {
        replaceAll(info(cls.dep).get.func(meth))
      }

    case ClassAlloc.deps(ef, cls) =>
      after(cls) {
        val clsinfo    = info(cls.dep).get
        val data       = clsinfo.data
        val vtableData = clsinfo.vtableConstant

        val instance = Alloc(clsinfo.typed)
        val elem = Elem(instance, Seq(Lit.I32(0), Lit.I32(0)))
        val store = Store(ef.dep, elem, clsinfo.vtableConstant)
        val cast = Bitcast(instance, Defn.Ptr(Prim.I8))

        replace {
          case u if u.isEf  => store
          case u if u.isVal => cast
          case _            => throw new Exception("unreachable")
        }
      }

    case MethodElem(ef, instance, meth @ Defn.Method.deps(_, _, _, cls)) =>
      after(cls) {
        val clsinfo = info(cls.dep).get
        val typed = Bitcast(instance, clsinfo.typedRef)
        val vtable_* = Load(Empty, Elem(typed, Seq(Lit.I32(0), Lit.I32(0))))
        val meth_** = Elem(vtable_*, Seq(Lit.I32(0), Lit.I32(clsinfo.vtableIndex(meth))))
        val meth_* = Load(Empty, meth_**)

        replace {
          case u if u.isEf => ef
          case _           => meth_*
        }
      }

    case FieldElem(ef, instance, field @ Defn.Field.deps(_, cls)) =>
      after(cls) {
        val clsinfo = info(cls.dep).get
        val typed = Bitcast(instance, clsinfo.typedRef)
        val field_* = Elem(typed, Seq(Lit.I32(0), Lit.I32(clsinfo.dataIndex(field) + 1)))

        replace {
          case u if u.isEf => ef
          case _           => field_*
        }
      }
  }
}
