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
import dotty.tools.dotc.transform.Erasure
import dotty.tools.dotc.ast.tpd

object UnsignedIntegersErasure:
  val name = "scalanative-unsigned-erasure"

class UnsignedIntegersErasure extends PluginPhase {
  override val runsAfter = Set(transform.Erasure.name, PrepNativeInterop.name)
  // override val runsAfter = Set(PostInlineNativeInterop.name)
  // override val runsBefore = Set(transform.FirstTransform.name)
  override val runsBefore = Set(GenNIR.name)
  val phaseName = UnsignedIntegersErasure.name
  override def description: String = "adapt unsigned integers to runtime model"

  def defn(using Context): Definitions = ctx.definitions
  def defnNir(using Context): NirDefinitions = NirDefinitions.get

  override def transformApply(tree: Apply)(using Context): Tree = {
    val params = tree.fun.symbol.paramInfo.stripPoly.paramInfoss.flatten
    assert(params.size == tree.args.size)

    if tree.args.exists(isUnsignedInteger)
    then
      cpy.Apply(tree)(
        fun = tree.fun,
        args = params.zip(tree.args).map { (paramTpe, arg) =>
          if (shouldBox(paramTpe, arg)) box(arg)
          else arg
        }
      )
    else tree
  }

  override def transformTyped(tree: tpd.Typed)(using Context): tpd.Tree = {
    val Typed(expr, tpt) = tree
    if shouldBox(tpt.tpe.widenDealias, expr) then
      cpy.Typed(tree)(expr = box(expr), tpt = tpt)
    else tree
  }

  override def transformTypeApply(
      tree: tpd.TypeApply
  )(using Context): tpd.Tree = {
    val defn = this.defn
    val TypeApply(fun, args) = tree
    def boxedUnsigedInteger = List(ref(defnNir.UnsignedIntClass))

    val tpe = args.headOption.map(_.tpe)
    if !tpe.exists(isUnsignedInteger) then tree
    else
      fun.symbol match {
        case defn.Any_isInstanceOf =>
          fun match
            case Select(arg, _) =>
              val argTpe = arg.tpe.widenDealias
              // Evaulate expression at compile-time, simillary 1.asInstanceOf[Int] always evaluates to true
              if isUnsignedInteger(argTpe)
              then cpy.Literal(tree)(Constant(tpe.contains(argTpe)))
              else cpy.TypeApply(tree)(fun, boxedUnsigedInteger)
            case _ => tree

        case defn.Any_asInstanceOf =>
          cpy.TypeApply(tree)(fun, boxedUnsigedInteger)

        case _ => tree
      }
  }

  override def transformValDef(tree: tpd.ValDef)(using Context): tpd.Tree = {
    val tpe = tree.tpt.tpe
    val rhs = tree.forceIfLazy

    if shouldBox(tpe, rhs) then
      cpy.ValDef(tree)(tpt = ref(defnNir.UnsignedIntClass), rhs = box(rhs))
    else if shouldUnbox(tpe, rhs) then
      cpy.ValDef(tree)(tpt = ref(defnNir.NewUIntClass), rhs = unbox(rhs))
    else if tpe != rhs.tpe &&
        isUnsignedInteger(tpe) && !isUnsignedInteger(rhs) then
      cpy.ValDef(tree)(rhs = unbox(rhs))
    else tree
  }

  override def transformIf(tree: tpd.If)(using Context): tpd.Tree = {
    val If(condt, thent, elset) = tree
    condt match
      case Apply(Select(ident, op), List(Literal(Constant(null))))
          if isUnsignedInteger(ident) =>
        if op == nme.ne then thent
        else if op == nme.eq then elset
        else tree
      case _ => tree
  }

  override def transformAssign(tree: tpd.Assign)(using Context): tpd.Tree = {
    val Assign(lhs, rhs) = tree
    if shouldBox(lhs.tpe, rhs) then //
      cpy.Assign(tree)(lhs = lhs, rhs = box(rhs))
    else if shouldUnbox(lhs.tpe, rhs) then
      cpy.Assign(tree)(lhs = lhs, rhs = unbox(rhs))
    else tree
  }

  private def isUnsignedInteger(tree: Tree)(using Context): Boolean =
    isUnsignedInteger(tree.tpe.widenDealias)

  private def isUnsignedInteger(tpe: Type)(using Context): Boolean =
    tpe.widenDealias.typeSymbol == defnNir.NewUIntClass

  private def shouldBox(expectedTpe: Type, value: Tree)(using
      Context
  ): Boolean =
    isUnsignedInteger(value) && defn.AnyRefType <:< expectedTpe.widenDealias

  private def shouldUnbox(expectedTpe: Type, value: Tree)(using
      Context
  ): Boolean =
    isUnsignedInteger(expectedTpe) && defn.AnyRefType <:< value.tpe.widenDealias

  private def box(value: Tree)(using Context) = cpy.Apply(value)(
    ref(defnNir.BoxUnsignedMethod(value.tpe.typeSymbol)),
    List(value)
  )

  private def unbox(value: Tree)(using Context) = cpy.Apply(value)(
    ref(defnNir.UnboxUnsignedMethod(value.tpe.typeSymbol)),
    List(value)
  )

}
