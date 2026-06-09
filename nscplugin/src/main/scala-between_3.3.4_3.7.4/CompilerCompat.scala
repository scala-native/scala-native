package scala.scalanative.nscplugin

object CompilerCompat {
  val SymUtils = dotty.tools.dotc.core.Symbols
  val SymbolExtensions = dotty.tools.backend.jvm.DottyBackendInterface.symExtensions
  
  type ScalaPrimitives = dotty.tools.backend.jvm.DottyPrimitives

  val LazyValHandleName = None
}
