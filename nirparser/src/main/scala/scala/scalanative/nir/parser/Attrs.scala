package scala.scalanative
package nir
package parser

import fastparse.all._

object Attrs extends Base[nir.Attrs] {

  import Base.IgnoreWhitespace._

  override val parser: P[nir.Attrs] =
    P(Attr.parser.rep map (nir.Attrs.fromSeq(_)))
}
