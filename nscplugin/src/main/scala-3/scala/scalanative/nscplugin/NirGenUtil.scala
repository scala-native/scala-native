package scala.scalanative
package nscplugin

import dotty.tools.dotc.ast.tpd
import tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Types._
import scala.scalanative.util.ScopedVar
import scala.collection.mutable
import dotty.tools.dotc.core.Names.Name
import dotty.tools.dotc.report

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

  // Backend utils ported from Dotty JVM backend
  // https://github.com/lampepfl/dotty/blob/938d405f05e3b47eb18183a6d6330b6324505cdf/compiler/src/dotty/tools/backend/jvm/DottyBackendInterface.scala
  private val desugared = new java.util.IdentityHashMap[Type, tpd.Select]

  private def cachedDesugarIdent(i: Ident): Option[tpd.Select] = {
    var found = desugared.get(i.tpe)
    if (found == null) {
      tpd.desugarIdent(i) match {
        case sel: tpd.Select =>
          desugared.put(i.tpe, sel)
          found = sel
        case _ =>
      }
    }
    if (found == null) None else Some(found)
  }

  object DesugaredSelect extends DeconstructorCommon[tpd.Tree] {
    var desugared: tpd.Select = null

    override def isEmpty: Boolean =
      desugared eq null

    def _1: Tree = desugared.qualifier
    def _2: Name = desugared.name

    override def unapply(s: tpd.Tree): this.type = {
      s match {
        case t: tpd.Select => desugared = t
        case t: Ident =>
          cachedDesugarIdent(t) match {
            case Some(t) => desugared = t
            case None    => desugared = null
          }
        case _ => desugared = null
      }

      this
    }
  }

  abstract class DeconstructorCommon[T >: Null <: AnyRef] {
    var field: T = null
    def get: this.type = this
    def isEmpty: Boolean = field eq null
    def isDefined = !isEmpty
    def unapply(s: T): this.type = {
      field = s
      this
    }
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
