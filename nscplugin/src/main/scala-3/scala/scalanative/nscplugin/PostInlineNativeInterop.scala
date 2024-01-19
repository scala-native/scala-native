package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools._
import dotc._
import dotc.ast.tpd._
import scala.scalanative.nscplugin.CompilerCompat.SymUtilsCompat.setter
import core.Contexts._
import core.Definitions
import core.Names._
import core.Symbols._
import core.Types._
import core.Flags._
import core.StdNames._
import core.Constants.Constant
import NirGenUtil.ContextCached
import dotty.tools.dotc.transform.SeqLiterals

/** This phase does:
 *    - handle TypeApply -> Apply conversion for intrinsic methods
 */
object PostInlineNativeInterop {
  val name = "scalanative-prepareInterop-postinline"
}

class PostInlineNativeInterop extends PluginPhase with NativeInteropUtil {
  override val runsAfter = Set(transform.Inlining.name, PrepNativeInterop.name)
  override val runsBefore = Set(transform.FirstTransform.name)
  val phaseName = PostInlineNativeInterop.name
  override def description: String = "prepare ASTs for Native interop"

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
      case Apply(fun, args)
          if fun.symbol.isExtern && fun.symbol.usesVariadicArgs =>
        args
          .collectFirst {
            case SeqLiteral(args, _)           => args
            case Typed(SeqLiteral(args, _), _) => args
          }
          .toList
          .flatten
          .foreach { varArg =>
            varArg.pushAttachment(
              NirDefinitions.NonErasedType,
              varArg.typeOpt.widenDealias
            )
          }
        tree

      case _ => tree

  }

}
