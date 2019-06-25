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
}
