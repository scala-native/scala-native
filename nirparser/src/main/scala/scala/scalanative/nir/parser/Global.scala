package scala.scalanative
package nir
package parser

import fastparse.all._

object Global extends Base[nir.Global] {

  import Base._

  val Top = P("@" ~ mangledId.! map (nir.Global.Top(_)))
  val Member =
    P(Top ~ "::" ~ qualifiedId.! map { case (t, m) => t member m })
  override val parser: P[nir.Global] = Member | Top
}
