package scala.scalanative.nscplugin

import dotty.tools.dotc.ast.tpd
import tpd._
import dotty.tools.dotc.core
import core.Symbols._
import core.Contexts._
import core.Types._

trait NirGenUtil(using Context) { self: NirCodeGen =>
  protected def genParamSyms(
      dd: DefDef,
      isStatic: Boolean
  ): Seq[Option[Symbol]] = {
    val params = for {
      paramList <- dd.paramss.take(1)
      param <- paramList
    } yield Some(param.symbol)

    if (isStatic) params
    else None +: params
  }

  protected def qualifierOf(fun: Tree): Tree = fun match {
    case fun: Ident =>
      fun.tpe match {
        case TermRef(prefix: TermRef, _)  => tpd.ref(prefix)
        case TermRef(prefix: ThisType, _) => tpd.This(prefix.cls)
      }
    case Select(qualifier, _) => qualifier
    case TypeApply(fun, _)    => qualifierOf(fun)
  }
}
