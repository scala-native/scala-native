package scala.scalanative.nscplugin

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core.Contexts.{Context, ctx}
import dotty.tools.dotc.core.Flags._
import dotty.tools.dotc.core.Names._
import dotty.tools.dotc.core.Types._
import dotty.tools.dotc.core.{Definitions, Symbols}
import dotty.tools.dotc.plugins.PluginPhase

import scala.scalanative.nscplugin.CompilerCompat.SymUtils.*

import NirGenUtil.ContextCached

trait NativeInteropUtil { self: PluginPhase =>

  /** Returns the definitions in the current context. */
  protected def defn(using Context): Definitions = ctx.definitions

  /** Returns the Native IR definitions in the current context. */
  protected def defnNir(using Context): NirDefinitions = NirDefinitions.get

  protected class DealiasTypeMapper(using Context) extends TypeMap {
    override def apply(tp: Type): Type =
      val sym = tp.typeSymbol
      val dealiased =
        if sym.isOpaqueAlias then sym.opaqueAlias
        else tp
      dealiased.widenDealias match
        case AppliedType(tycon, args) =>
          AppliedType(this(tycon), args.map(this))
        case ty if ty != tp => this(ty)
        case ty             => ty
  }

  protected def hasLayout(tpe: Type)(using Context): Boolean = {
    // Pointer layout does not depend on its arguments; aggregates need every
    // component's exact layout. Abstract upper bounds are not sufficient.
    val sym = tpe.typeSymbol
    if sym == defnNir.PtrClass
        || sym == defnNir.RawPtrClass
        || tpe.derivesFrom(defnNir.CFuncPtrClass) then true
    else if tpe.isAny
        || tpe.isNothingType
        || tpe.isNullType
        || sym.is(DeferredType) then false
    else if sym == defnNir.CArrayClass then
      tpe.argInfos match {
        case List(element, length) => //
          hasLayout(element) && hasArrayLength(length)
        case _ => false
      }
    else if defnNir.CStructClasses.contains(sym) then
      tpe.argInfos.forall(hasLayout)
    else sym.isClass && !sym.is(Trait)
  }

  private def hasArrayLength(tpe: Type)(using Context): Boolean =
    defnNir.NatBaseClasses.contains(tpe.typeSymbol) ||
      (defnNir.NatDigitClasses.contains(tpe.typeSymbol) &&
      tpe.argInfos.forall(t => defnNir.NatBaseClasses.contains(t.typeSymbol)))

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
