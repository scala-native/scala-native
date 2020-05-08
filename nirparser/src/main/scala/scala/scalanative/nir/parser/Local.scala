package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._

object Local extends Base[nir.Local] {

  import Base._

  override def parser[_: P]: P[nir.Local] =
    P("%" ~ int.map { id => nir.Local(id) })
}
