package scala.scalanative.nscplugin

import dotty.tools.dotc.ast.tpd
import tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Types._
import scala.scalanative.util.ScopedVar
import scalanative.nir.{Fresh, Local, LocalName}
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
      curFresh := Fresh(),
      curScopeId := scala.scalanative.nir.ScopeId.TopLevel
    ) {
      val buffer = new ExprBuffer(using curFresh)
      f(using buffer)
    }
  }

  protected def localNamesBuilder(): mutable.Map[Local, LocalName] =
    mutable.Map.empty[Local, LocalName]

  extension (fresh: Fresh)
    def namedId(name: LocalName): Local = {
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
