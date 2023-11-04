package scala.scalanative
package nscplugin

import dotty.tools.dotc.ast.tpd
import tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Types._
import scala.scalanative.util.ScopedVar
import scala.collection.mutable

trait NirGenUtil(using Context) { self: NirCodeGen =>

  protected def desugarTree(tree: Tree): Tree = {
    tree match {
      case ident: Ident => tpd.desugarIdent(ident)
      case _            => tree
    }
  }

  protected def qualifierOf(fun: Tree): Tree = {
    fun match {
      case fun: Ident =>
        fun.tpe match {
          case TermRef(prefix: TermRef, _)  => tpd.ref(prefix)
          case TermRef(prefix: ThisType, _) => tpd.This(prefix.cls)
        }
      case Select(qualifier, _) => qualifier
      case TypeApply(fun, _)    => qualifierOf(fun)
    }
  }

  protected def withFreshExprBuffer[R](f: ExprBuffer ?=> R): R = {
    ScopedVar.scoped(
      curFresh := nir.Fresh(),
      curScopeId := nir.ScopeId.TopLevel
    ) {
      val buffer = new ExprBuffer(using curFresh)
      f(using buffer)
    }
  }

  protected def withFreshBlockScope[R](
      srcPosition: nir.Position
  )(f: nir.ScopeId => R): R = {
    val blockScope = nir.ScopeId.of(curFreshScope.get())
    // Parent of top level points to itself
    val parentScope =
      if (blockScope.isTopLevel) blockScope
      else curScopeId.get

    curScopes.get += nir.Defn.Define.DebugInfo.LexicalScope(
      id = blockScope,
      parent = parentScope,
      srcPosition = srcPosition
    )

    ScopedVar.scoped(
      curScopeId := blockScope
    )(f(parentScope))
  }

  protected def localNamesBuilder(): mutable.Map[nir.Local, nir.LocalName] =
    mutable.Map.empty[nir.Local, nir.LocalName]

  extension (fresh: nir.Fresh)
    def namedId(name: nir.LocalName): nir.Local = {
      val id = fresh()
      curMethodLocalNames.get.update(id, name)
      id
    }
}

object NirGenUtil {
  class ContextCached[T](init: Context ?=> T) {
    private var lastContext: Context = _
    private var cached: T = _

    def get(using Context): T = {
      if (lastContext != ctx) {
        cached = init
        lastContext = ctx
      }
      cached
    }
  }
}
