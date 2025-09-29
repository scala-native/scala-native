package scala.scalanative
package nscplugin

import scala.collection.mutable
import scala.tools.nsc.Global

trait NirGenUtil[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import global._

  def genParamSyms(dd: DefDef, isStatic: Boolean): Seq[Option[Symbol]] = {
    val vp = dd.vparamss
    val params = if (vp.isEmpty) Nil else vp.head.map(p => Some(p.symbol))
    if (isStatic) params else None +: params
  }

  protected def localNamesBuilder(): mutable.Map[nir.Local, nir.LocalName] =
    mutable.Map.empty[nir.Local, nir.LocalName]

  def namedId(fresh: nir.Fresh)(name: nir.LocalName): nir.Local = {
    val id = fresh()
    curMethodLocalNames.get.update(id, name)
    id
  }

  protected def withFreshBlockScope[R](
      srcPosition: nir.SourcePosition
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

    util.ScopedVar.scoped(
      curScopeId := blockScope
    )(f(parentScope))
  }
}
