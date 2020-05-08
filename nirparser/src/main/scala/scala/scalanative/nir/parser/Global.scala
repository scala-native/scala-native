package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._

object Global extends Base[nir.Global] {

  import Base._

  override def parser[_: P]: P[nir.Global] =
    P("@" ~ stringLit).map(Unmangle.unmangleGlobal(_))
}
