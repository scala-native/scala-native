package scala.scalanative
package nir
package parser

import fastparse.all._

object Global extends Base[nir.Global] {

  import Base._

  override val parser: P[nir.Global] =
    P("@" ~ stringLit).map(Unmangle.unmangleGlobal(_))
}
