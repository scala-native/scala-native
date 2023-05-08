package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools._
import dotc._
import dotc.ast.tpd._
import dotc.transform.SymUtils.setter
import core.Contexts._
import core.Definitions
import core.Names._
import core.Symbols._
import core.Types._
import core.StdNames._
import core.Constants.Constant
import NirGenUtil.ContextCached
import dotty.tools.dotc.core.Flags

/** This phase does:
 *    - handle TypeApply -> Apply conversion for intrinsic methods
 */
object PostInlineNativeInterop {
  val name = "scalanative-prepareInterop-postinline"
}

class PostInlineNativeInterop extends PluginPhase {
  override val runsAfter = Set(transform.Inlining.name, PrepNativeInterop.name)
  override val runsBefore = Set(transform.FirstTransform.name)
  val phaseName = PostInlineNativeInterop.name
  override def description: String = "prepare ASTs for Native interop"

  def defn(using Context): Definitions = ctx.definitions
  def defnNir(using Context): NirDefinitions = NirDefinitions.get

  private def isTopLevelExtern(dd: ValOrDefDef)(using Context) = {
    dd.rhs.symbol == defnNir.UnsafePackage_extern &&
    dd.symbol.isWrappedToplevelDef
  }

  private class DealiasTypeMapper(using Context) extends TypeMap {
    override def apply(tp: Type): Type =
      val sym = tp.typeSymbol
      val dealiased =
        if sym.isOpaqueAlias then sym.opaqueAlias
        else tp
      dealiased.widenDealias match
        case AppliedType(tycon, args) =>
          AppliedType(this(tycon), args.map(this))
        case ty => ty
  }

  override def transformApply(tree: Apply)(using Context): Tree = {
    val defnNir = this.defnNir
    def dealiasTypeMapper = DealiasTypeMapper()

    // Attach exact type information to the AST to preserve the type information
    // during the type erase phase and refer to it in the NIR generation phase.
    tree match
      case app @ Apply(TypeApply(fun, tArgs), _)
          if defnNir.CFuncPtr_fromScalaFunction.contains(fun.symbol) =>
        val tys = tArgs.map(t => dealiasTypeMapper(t.tpe))
        app.withAttachment(NirDefinitions.NonErasedTypes, tys)

      case Apply(fun, args) if defnNir.CFuncPtr_apply.contains(fun.symbol) =>
        val paramTypes =
          args.map(a => dealiasTypeMapper(a.tpe)) :+
            dealiasTypeMapper(tree.tpe.finalResultType)
        tree.withAttachment(NirDefinitions.NonErasedTypes, paramTypes)

      case _ => tree

  }

  override def transformTypeApply(tree: TypeApply)(using Context): Tree = {
    val TypeApply(fun, tArgs) = tree
    val defnNir = this.defnNir
    def dealiasTypeMapper = DealiasTypeMapper()

    // sizeOf[T] -> sizeOf(classOf[T])
    fun.symbol match
      case defnNir.Intrinsics_sizeOfType =>
        val tpe = dealiasTypeMapper(tArgs.head.tpe)
        cpy
          .Apply(tree)(
            ref(defnNir.Intrinsics_sizeOf),
            List(Literal(Constant(tpe)))
          )
          .withAttachment(NirDefinitions.NonErasedType, tpe)

      // alignmentOf[T] -> alignmentOf(classOf[T])
      case defnNir.Intrinsics_alignmentOfType =>
        val tpe = dealiasTypeMapper(tArgs.head.tpe)
        cpy
          .Apply(tree)(
            ref(defnNir.Intrinsics_alignmentOf),
            List(Literal(Constant(tpe)))
          )
          .withAttachment(NirDefinitions.NonErasedType, tpe)

      case _ => tree
  }

}
