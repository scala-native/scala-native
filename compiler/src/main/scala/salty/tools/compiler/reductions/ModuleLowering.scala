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
      val cls      = Defn.Class(parent, ifaces, module.name)
      val global   = Defn.Global(cls, Lit.Zero(cls), Name.Data(module.name))
      val accessor = {
        val prevVal     = Load(Empty, global)
        val ifPrevNull  = If(Empty, Eq(prevVal, Lit.Null()))
        val newVal      = ClassAlloc(prevVal, cls)
        val ctorCall    = Call(newVal, ctor, Seq(newVal))
        val storeNew    = Store(ctorCall, global, newVal)
        val retNew      = Return(CaseTrue(ifPrevNull), storeNew, newVal)
        val retExisting = Return(CaseFalse(ifPrevNull), prevVal, prevVal)
        val end         = End(Seq(retNew, retExisting))

        Defn.Define(cls, Seq(), end, Name.Accessor(module.name))
      }
      val accessorCall = Call(Empty, accessor, Seq())

      replace {
        case use if use.isDefn => cls
        case use if use.isVal  => accessorCall
        case _                 => throw new Exception("unreachable")
      }
  }
}

