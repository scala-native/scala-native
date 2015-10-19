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
 *      struct $name.ref = { $name.vtable*, $name.data* }
 *
 *      .. define $name::$mname(%this: $name.ref, .. %pname: $ptype): $ret = $end
 *      constant $name.vtable.data: $name.vtable = { .. $name::$mname }
 *
 *  Usages are rewritten as following:
 *
 *  * Type usages are lowered to $name.ref
 *
 *  * Allocations
 *
 *        alloc $name
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
 *        elem %instance, 0, 0, ${indexOf(vtable, method)}
 *
 *  * Field elems
 *
 *        field-elem %instance, $field
 *
 *    Lowered to:
 *
 *        elem %instance, 1, 0, ${indexOf(data, field)}
 */
object ClassLowering extends Reduction {
  def reduce = ???
}
