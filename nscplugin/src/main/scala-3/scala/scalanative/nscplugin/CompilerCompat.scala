package scala.scalanative.nscplugin

import dotty.tools.dotc.core.NameKinds
import dotty.tools.dotc.core.NameKinds.SuffixNameKind

object CompilerCompat {

  private object SymUtilsCompatDef:
    val SymUtils = dotty.tools.dotc.core.Symbols

  private object SymUtilsCompatSelect:
    import SymUtilsCompatDef.*
    object Inner {
      import dotty.tools.dotc.transform.*
      val SymUtilsAlias = SymUtils
    }
  val SymUtilsCompat = SymUtilsCompatSelect.Inner.SymUtilsAlias

  // NamesKinds.LazyVarHandleName, introduced in 3.8.0
  private object LazyValHandleNameCompatDef:
    val LazyVarHandleName = null.asInstanceOf[SuffixNameKind]
  private object LazyValHandleNameCompatSelect:
    import LazyValHandleNameCompatDef.*
    object Inner {
      // provides access to NameKind.LazyVarHandleName in 3.8.0 or uses dummy definition otherwise
      import NameKinds.*
      val LazyValHandleNameCompat = Option(LazyVarHandleName)
    }
  val LazyValHandleNameCompat =
    LazyValHandleNameCompatSelect.Inner.LazyValHandleNameCompat
}
