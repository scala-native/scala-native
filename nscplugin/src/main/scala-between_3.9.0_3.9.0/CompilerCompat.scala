package scala.scalanative.nscplugin

import dotty.tools.dotc.ast.tpd.Tree
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.util.ReadOnlyMap

object CompilerCompat {
  val SymUtils = dotty.tools.dotc.core.Symbols
  val SymbolExtensions = dotty.tools.backend.jvm.SymbolUtils.symExtensions

  abstract class ScalaPrimitives(ctx: Context)
      extends dotty.tools.backend.ScalaPrimitives(ctx) {
    private given Context = ctx
    protected def nirPrimitives: ReadOnlyMap[Symbol, Int]

    override def isPrimitive(tree: Tree): Boolean =
      nirPrimitives.contains(tree.symbol) || super.isPrimitive(tree)
  }

  val LazyValHandleName = Option(
    dotty.tools.dotc.core.NameKinds.LazyVarHandleName
  )
}
