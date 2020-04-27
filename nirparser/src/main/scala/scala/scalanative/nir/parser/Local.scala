package scala.scalanative
package nir
package parser

import fastparse.all._

object Local extends Base[nir.Local] {

  import Base._

  override val parser: P[nir.Local] =
    P("%" ~ int map { id => nir.Local(id) })
}
