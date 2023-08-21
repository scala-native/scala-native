package scala.scalanative
package nscplugin

import scala.tools.nsc.Global
import scala.collection.mutable
import scala.scalanative.nir.{Fresh, LocalName, Local}

trait NirGenUtil[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import global._

  def genParamSyms(dd: DefDef, isStatic: Boolean): Seq[Option[Symbol]] = {
    val vp = dd.vparamss
    val params = if (vp.isEmpty) Nil else vp.head.map(p => Some(p.symbol))
    if (isStatic) params else None +: params
  }

  protected def localNamesBuilder(): mutable.Map[Local, LocalName] =
    mutable.Map.empty[Local, LocalName]

  def namedId(fresh: Fresh)(name: LocalName): Local = {
    val id = fresh()
    curMethodLocalNames.get.update(id, name)
    id
  }
}
