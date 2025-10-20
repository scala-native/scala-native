package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Definitions
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Flags.*
import scala.scalanative.nscplugin.CompilerCompat.SymUtilsCompat.*
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Types.*
import dotty.tools.dotc.core.Flags.*
import NirGenUtil.ContextCached

trait NativeInteropUtil { self: PluginPhase =>

  /** Returns the definitions in the current context. */
  protected def defn(using Context): Definitions = ctx.definitions

  /** Returns the Native IR definitions in the current context. */
  protected def defnNir(using Context): NirDefinitions = NirDefinitions.get

  /** `true` iff `dd` is a toplevel declaration that is defined externally. */
  def isTopLevelExtern(dd: ValOrDefDef)(using Context) = {
    dd.rhs.symbol == defnNir.UnsafePackage_extern &&
    dd.symbol.isWrappedToplevelDef
  }

  extension (sym: Symbols.Symbol)
    /** `true` iff `sym` a trait or Java interface declaration. */
    def isTraitOrInterface(using Context): Boolean =
      sym.is(Trait) || sym.isAllOf(JavaInterface)

    /** `true` iff `sym` is a scala module. */
    def isScalaModule(using Context): Boolean =
      sym.is(ModuleClass, butNot = Lifted)

    /** `true` iff `sym` is a C-bridged type or a declaration defined
     *  externally.
     */
    def isExtern(using Context): Boolean = sym.exists && {
      sym.owner.isExternType ||
      sym.hasAnnotation(defnNir.ExternClass) ||
      (sym.is(Accessor) && sym.field.isExtern)
    }

    /** `true` iff `sym` is a C-bridged type (e.g., `unsafe.CSize`). */
    def isExternType(using Context): Boolean =
      (isScalaModule || sym.isTraitOrInterface) &&
        sym.hasAnnotation(defnNir.ExternClass)

    /** `true` iff `sym` is an exported definition. */
    def isExported(using Context) =
      sym.hasAnnotation(defnNir.ExportedClass) ||
        sym.hasAnnotation(defnNir.ExportAccessorsClass)

    /** `true` iff `sym` uses variadic arguments. */
    def usesVariadicArgs(using Context) = sym.paramInfo.stripPoly match {
      case MethodTpe(_, paramTypes, _) =>
        paramTypes.exists(param => param.isRepeatedParam)
      case t => t.isVarArgsMethod
    }
  end extension

}
