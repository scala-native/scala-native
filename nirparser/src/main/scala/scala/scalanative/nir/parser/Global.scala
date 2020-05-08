package scala.scalanative
package nir
package parser

import fastparse._

object Global extends Base[nir.Global] {

  import Base.stringLit
  // added for 2.3.0
  import MultiLineWhitespace._

  override def parser[_: P]: P[nir.Global] =
    P("@" ~ stringLit).map(Unmangle.unmangleGlobal(_))
}
