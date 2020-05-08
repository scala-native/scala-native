package scala.scalanative
package nir
package parser

import fastparse._

object Local extends Base[nir.Local] {

  import Base.int
  // added for 2.3.0
  import MultiLineWhitespace._

  override def parser[_: P]: P[nir.Local] =
    P("%" ~ int.map { id => nir.Local(id) })
}
