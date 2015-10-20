package salty.tools.compiler.reductions

import salty.ir._, Reduction._

/** Lowers modules into module classes with singleton
 *  instance stored in a global variable that is accessed
 *  through a dedicated accessor function.
 *
 *  For example a module with members:
 *
 *      object $name: $parent, .. $ifaces, $ctor
 *
 *  Translates to:
 *
 *      class $name: $parent, .. $ifaces
 *
 *      global $name.data: $name = null
 *
 *      define $name.accessor(): $name
 *        %prev = load $name.data
 *        if eq %prev, null
 *          %new = alloc $name
 *          call $ctor(%new)
 *          store $name.data, %new
 *          return %new
 *        else
 *          return %prev
 *
 *  Usages are rewritten as follows:
 *
 *  * Type usages become usages of module class
 *
 *  * Value dependencies are rewritten to calls to accessor.
 */
object ModuleLowering extends Reduction {
  def reduce = {
    case module @ Defn.Module(parent, ifaces, ctor) =>
      val cls = Defn.Class(parent.get, ifaces.nodes, module.name)
      val global = Defn.Global(cls, Zero(cls), Name.ModuleData(module.name))
      val accessor = {
        val prevVal     = Load(Empty, global)
        val ifPrevNull  = If(Empty, Eq(prevVal, Null()))
        val newVal      = ClassAlloc(cls)
        val ctorCall    = Call(prevVal, ctor.get, Seq(newVal))
        val storeNew    = Store(ctorCall, global, newVal)
        val retNew      = Return(CaseTrue(ifPrevNull), storeNew, newVal)
        val retExisting = Return(CaseFalse(ifPrevNull), prevVal, prevVal)
        val end         = End(Seq(retNew, retExisting))

        Defn.Define(cls, Seq(), end, Name.ModuleAccessor(module.name))
      }
      val accessorCall = Call(Empty, accessor, Seq())

      Replace {
        case s if s.schema == Schema.Ref => cls
        case s if s.schema == Schema.Val => accessorCall
        case _                           => throw new Exception("unreachable")
      }
  }
}

