package scala.scalanative
package nir
package parser

import fastparse.all._

object PassConv extends Base[nir.PassConv] {
  val Byval                            = P("byval[" ~ Type.parser ~ "]" map (nir.PassConv.Byval(_)))
  val Sret                             = P("sret[" ~ Type.parser ~ "]" map (nir.PassConv.Sret(_)))
  override val parser: P[nir.PassConv] = Byval | Sret
}
