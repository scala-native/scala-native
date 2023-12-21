package scala.scalanative.nscplugin

object CompilerCompat {

  private object SymUtilsCompatDef:
    val SymUtils = dotty.tools.dotc.core.Symbols

  private object SymUtilsCompatSelect:
    import SymUtilsCompatDef._
    object Inner {
      import dotty.tools.dotc.transform._
      val SymUtilsAlias = SymUtils
    }
  val SymUtilsCompat = SymUtilsCompatSelect.Inner.SymUtilsAlias
}
