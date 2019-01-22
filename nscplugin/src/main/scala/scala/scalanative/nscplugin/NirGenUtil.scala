package scala.scalanative
package nscplugin

import scalanative.util.unsupported

trait NirGenUtil { self: NirGenPhase =>
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._
  import SimpleType.fromSymbol

  def genParamSyms(dd: DefDef, isStatic: Boolean): Seq[Option[Symbol]] = {
    val vp     = dd.vparamss
    val params = if (vp.isEmpty) Nil else vp.head.map(p => Some(p.symbol))
    if (isStatic) params else None +: params
  }

  object CVararg {
    def unapply(tree: Tree): Option[Tree] = tree match {
      case Apply(fun, Seq(argp)) if fun.symbol == CVarargMethod =>
        Some(argp)
      case _ =>
        None
    }
  }

  object MaybeAsInstanceOf {
    def unapply(tree: Tree): Some[Tree] = tree match {
      case Apply(TypeApply(asInstanceOf_? @ Select(base, _), _), _)
          if asInstanceOf_?.symbol == Object_asInstanceOf =>
        Some(base)
      case _ =>
        Some(tree)
    }
  }

  object MaybeBlock {
    def unapply(tree: Tree): Some[Tree] = tree match {
      case Block(Seq(), MaybeBlock(expr)) => Some(expr)
      case _                              => Some(tree)
    }
  }

  object WrapArray {
    lazy val isWrapArray: Set[Symbol] =
      Seq(
        nme.wrapRefArray,
        nme.wrapByteArray,
        nme.wrapShortArray,
        nme.wrapCharArray,
        nme.wrapIntArray,
        nme.wrapLongArray,
        nme.wrapFloatArray,
        nme.wrapDoubleArray,
        nme.wrapBooleanArray,
        nme.wrapUnitArray,
        nme.genericWrapArray
      ).map(getMemberMethod(PredefModule, _)).toSet

    def unapply(tree: Apply): Option[Tree] = tree match {
      case Apply(wrapArray_?, List(wrapped))
          if isWrapArray(wrapArray_?.symbol) =>
        Some(wrapped)
      case _ =>
        None
    }
  }

  object ExternForwarder {
    // format: OFF
    def unapply(tree: Tree): Option[Symbol] = tree match {
      case DefDef(_, _, _, Seq(params), _, Apply(sel @ Select(from, _), args))
          if from.symbol != null
          && from.symbol.isExternModule
          && params.length == args.length
          && params.zip(args).forall {
               case (param, arg: RefTree) => param.symbol == arg.symbol
               case _                     => false
             } =>
        Some(sel.symbol)
      case _ =>
        None
    }
    // format: ON
  }
}
