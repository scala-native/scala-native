package scala.scalanative.nscplugin

object CompilerCompat {
  val SymUtils = dotty.tools.dotc.core.Symbols
  val SymbolExtensions = dotty.tools.backend.jvm.SymbolUtils.symExtensions
  
  type ScalaPrimitives = dotty.tools.backend.ScalaPrimitives

  val LazyValHandleName = Option(dotty.tools.dotc.core.NameKinds.LazyVarHandleName)
}
